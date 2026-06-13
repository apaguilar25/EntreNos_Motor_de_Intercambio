package es.ucab.entrenos.modulos.gamificacion.servicios;

import es.ucab.entrenos.modulos.gamificacion.modelos.PodioSemanal;
import es.ucab.entrenos.modulos.gamificacion.repositorios.IRepositorioPodio;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioPublicacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioPodio {

    private static final int UMBRAL_MINIMO_TRANSACCIONES = 5;

    private final IRepositorioPodio repositorioPodio;
    private final IRepositorioTransaccion repositorioTransaccion;
    private final IRepositorioPublicacion repositorioPublicacion;
    private final ServicioUsuario servicioUsuario;

    public ServicioPodio(IRepositorioPodio repositorioPodio,
                         IRepositorioTransaccion repositorioTransaccion,
                         IRepositorioPublicacion repositorioPublicacion,
                         ServicioUsuario servicioUsuario) {
        this.repositorioPodio = repositorioPodio;
        this.repositorioTransaccion = repositorioTransaccion;
        this.repositorioPublicacion = repositorioPublicacion;
        this.servicioUsuario = servicioUsuario;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void calcularPodioSemanalAutomatico() {
        calcularPodioSemanal();
    }

    public PodioSemanal calcularPodioSemanal() {
        long ahora = System.currentTimeMillis();
        long inicioSemana = ahora - 7L * 24 * 60 * 60 * 1000;

        List<Transaccion> semanales = repositorioTransaccion.obtenerTodas().stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.FINALIZADA)
                .filter(t -> t.getFechaCreacion() >= inicioSemana)
                .collect(Collectors.toList());

        limpiarVecinosDestacados();

        PodioSemanal podio = new PodioSemanal(inicioSemana, ahora);

        asignarProveedorElite(podio, semanales);
        asignarMotorEconomia(podio, semanales);
        asignarEmbajadorCalidad(podio, semanales);

        repositorioPodio.guardar(podio);

        marcarVecinosDestacados(podio);

        return podio;
    }

    private void asignarProveedorElite(PodioSemanal podio, List<Transaccion> semanales) {
        Map<String, Long> conteo = semanales.stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.FINALIZADA)
                .collect(Collectors.groupingBy(Transaccion::getIdOfertante, Collectors.counting()));

        elegirGanador(podio, conteo, true);
    }

    private void asignarMotorEconomia(PodioSemanal podio, List<Transaccion> semanales) {
        Map<String, Long> conteo = semanales.stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.FINALIZADA)
                .collect(Collectors.groupingBy(Transaccion::getIdDemandante, Collectors.counting()));

        elegirGanador(podio, conteo, false);
    }

    private void elegirGanador(PodioSemanal podio, Map<String, Long> conteo, boolean esProveedor) {
        List<Map.Entry<String, Long>> elegibles = conteo.entrySet().stream()
                .filter(e -> e.getValue() >= UMBRAL_MINIMO_TRANSACCIONES)
                .sorted((a, b) -> {
                    int cmp = Long.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    double repA = obtenerReputacionHistorica(a.getKey());
                    double repB = obtenerReputacionHistorica(b.getKey());
                    return Double.compare(repB, repA);
                })
                .collect(Collectors.toList());

        if (!elegibles.isEmpty()) {
            Map.Entry<String, Long> ganador = elegibles.get(0);
            String nombre = obtenerNombreUsuario(ganador.getKey());
            if (esProveedor) {
                podio.setProveedorEliteId(ganador.getKey());
                podio.setProveedorEliteNombre(nombre);
                podio.setProveedorEliteServicios(ganador.getValue().intValue());
            } else {
                podio.setMotorEconomiaId(ganador.getKey());
                podio.setMotorEconomiaNombre(nombre);
                podio.setMotorEconomiaTransacciones(ganador.getValue().intValue());
            }
        }
    }

    private void asignarEmbajadorCalidad(PodioSemanal podio, List<Transaccion> semanales) {
        Map<String, Double> promedios = semanales.stream()
                .filter(t -> t.getCalificacionOfertante() != null)
                .collect(Collectors.groupingBy(
                        Transaccion::getIdOfertante,
                        Collectors.averagingInt(t -> t.getCalificacionOfertante())
                ));

        Map<String, Long> conteoOfertante = semanales.stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.FINALIZADA)
                .collect(Collectors.groupingBy(Transaccion::getIdOfertante, Collectors.counting()));

        List<Map.Entry<String, Double>> elegibles = promedios.entrySet().stream()
                .filter(e -> {
                    long total = conteoOfertante.getOrDefault(e.getKey(), 0L);
                    return total >= UMBRAL_MINIMO_TRANSACCIONES && e.getValue() > 0;
                })
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    double repA = obtenerReputacionHistorica(a.getKey());
                    double repB = obtenerReputacionHistorica(b.getKey());
                    return Double.compare(repB, repA);
                })
                .collect(Collectors.toList());

        if (!elegibles.isEmpty()) {
            Map.Entry<String, Double> ganador = elegibles.get(0);
            podio.setEmbajadorCalidadId(ganador.getKey());
            podio.setEmbajadorCalidadNombre(obtenerNombreUsuario(ganador.getKey()));
            podio.setEmbajadorCalidadPromedio(ganador.getValue());
        }
    }

    private double obtenerReputacionHistorica(String idUsuario) {
        return servicioUsuario.buscarPorId(idUsuario)
                .map(u -> (double) u.getPromedioCalificacion())
                .orElse(0.0);
    }

    private String obtenerNombreUsuario(String idUsuario) {
        return servicioUsuario.buscarPorId(idUsuario)
                .map(Usuario::getNombre)
                .orElse("Usuario Desconocido");
    }

    private void limpiarVecinosDestacados() {
        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        boolean cambio = false;
        for (Publicacion p : todas) {
            if (p.isEsVecinoDestacado()) {
                p.setEsVecinoDestacado(false);
                cambio = true;
            }
        }
        if (cambio) {
            repositorioPublicacion.guardarTodas(todas);
        }
    }

    private void marcarVecinosDestacados(PodioSemanal podio) {
        Set<String> idsDestacados = new HashSet<>();
        if (podio.getProveedorEliteId() != null) idsDestacados.add(podio.getProveedorEliteId());
        if (podio.getMotorEconomiaId() != null) idsDestacados.add(podio.getMotorEconomiaId());
        if (podio.getEmbajadorCalidadId() != null) idsDestacados.add(podio.getEmbajadorCalidadId());

        if (idsDestacados.isEmpty()) return;

        List<Publicacion> todas = repositorioPublicacion.obtenerTodas();
        for (Publicacion p : todas) {
            if (idsDestacados.contains(p.getIdUsuario())) {
                p.setEsVecinoDestacado(true);
            }
        }
        repositorioPublicacion.guardarTodas(todas);
    }

    public Optional<PodioSemanal> obtenerPodioActual() {
        return repositorioPodio.obtenerActual();
    }
}
