package es.ucab.entrenos.modulos.gamificacion.servicios;

import es.ucab.entrenos.modulos.gamificacion.dtos.PodioResponseDTO;
import es.ucab.entrenos.modulos.gamificacion.modelos.PodioSemanal;
import es.ucab.entrenos.modulos.gamificacion.repositorios.IRepositorioPodio;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class ServicioPodio {

    private static final int UMBRAL_MINIMO_TRANSACCIONES = 5;

    private final IRepositorioPodio repositorioPodio;
    private final IRepositorioTransaccion repositorioTransaccion;
    private final ServicioUsuario servicioUsuario;

    public ServicioPodio(IRepositorioPodio repositorioPodio,
                         IRepositorioTransaccion repositorioTransaccion,
                         ServicioUsuario servicioUsuario) {
        this.repositorioPodio = repositorioPodio;
        this.repositorioTransaccion = repositorioTransaccion;
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
        List<Usuario> todos = servicioUsuario.obtenerTodos();
        boolean cambio = false;
        for (Usuario u : todos) {
            if (u.isEsVecinoDestacado()) {
                u.setEsVecinoDestacado(false);
                cambio = true;
            }
        }
        if (cambio) {
            todos.forEach(u -> servicioUsuario.guardar(u));
        }
    }

    private void marcarVecinosDestacados(PodioSemanal podio) {
        Set<String> idsDestacados = new HashSet<>();
        if (podio.getProveedorEliteId() != null) idsDestacados.add(podio.getProveedorEliteId());
        if (podio.getMotorEconomiaId() != null) idsDestacados.add(podio.getMotorEconomiaId());
        if (podio.getEmbajadorCalidadId() != null) idsDestacados.add(podio.getEmbajadorCalidadId());

        if (idsDestacados.isEmpty()) return;

        servicioUsuario.obtenerTodos().stream()
                .filter(u -> idsDestacados.contains(u.getId()))
                .forEach(u -> {
                    u.setEsVecinoDestacado(true);
                    servicioUsuario.guardar(u);
                });
    }

    public Optional<PodioSemanal> obtenerPodioActual() {
        return repositorioPodio.obtenerActual();
    }

    public PodioResponseDTO obtenerTop3() {
        long ahora = System.currentTimeMillis();
        long inicioSemana = ahora - 7L * 24 * 60 * 60 * 1000;

        List<Transaccion> semanales = repositorioTransaccion.obtenerTodas().stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.FINALIZADA)
                .filter(t -> t.getFechaCreacion() >= inicioSemana)
                .collect(Collectors.toList());

        PodioResponseDTO dto = new PodioResponseDTO();
        dto.setIdPodio("TOP3-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        dto.setFechaInicioSemana(inicioSemana);
        dto.setFechaFinSemana(ahora);
        dto.setFechaCalculo(ahora);

        Map<String, Long> proveedores = semanales.stream()
                .collect(Collectors.groupingBy(Transaccion::getIdOfertante, Collectors.counting()));
        dto.setProveedorElite(topN(proveedores, 3));

        Map<String, Long> demandantes = semanales.stream()
                .collect(Collectors.groupingBy(Transaccion::getIdDemandante, Collectors.counting()));
        dto.setMotorEconomia(topN(demandantes, 3));

        Map<String, Double> promedios = semanales.stream()
                .filter(t -> t.getCalificacionOfertante() != null)
                .collect(Collectors.groupingBy(
                        Transaccion::getIdOfertante,
                        Collectors.averagingInt(t -> t.getCalificacionOfertante())));
        Map<String, Long> conteoOfertante = semanales.stream()
                .collect(Collectors.groupingBy(Transaccion::getIdOfertante, Collectors.counting()));
        dto.setEmbajadorCalidad(promedios.entrySet().stream()
                .filter(e -> conteoOfertante.getOrDefault(e.getKey(), 0L) >= UMBRAL_MINIMO_TRANSACCIONES)
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return Double.compare(obtenerReputacionHistorica(b.getKey()),
                            obtenerReputacionHistorica(a.getKey()));
                })
                .limit(3)
                .map(e -> new PodioResponseDTO.EntradaPodio(
                        e.getKey(), obtenerNombreUsuario(e.getKey()), e.getValue()))
                .collect(Collectors.toList()));

        return dto;
    }

    private List<PodioResponseDTO.EntradaPodio> topN(Map<String, Long> conteo, int n) {
        return conteo.entrySet().stream()
                .filter(e -> e.getValue() >= UMBRAL_MINIMO_TRANSACCIONES)
                .sorted((a, b) -> {
                    int cmp = Long.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return Double.compare(obtenerReputacionHistorica(b.getKey()),
                            obtenerReputacionHistorica(a.getKey()));
                })
                .limit(n)
                .map(e -> new PodioResponseDTO.EntradaPodio(
                        e.getKey(), obtenerNombreUsuario(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }
}
