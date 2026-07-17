package es.ucab.entrenos.modulos.gamificacion.servicios;

import es.ucab.entrenos.modulos.gamificacion.dtos.PodioResponseDTO;
import es.ucab.entrenos.modulos.gamificacion.modelos.EntradaPodio;
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

@Service
public class ServicioPodio {

    private static final int UMBRAL_MINIMO_TRANSACCIONES = 1;

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

        podio.setProveedorElite(topNProveedores(semanales, 3));
        podio.setMotorEconomia(topNDemandantes(semanales, 3));
        podio.setEmbajadorCalidad(topNCalidad(semanales, 3));

        repositorioPodio.guardar(podio);

        marcarVecinosDestacados(podio);

        return podio;
    }

    public boolean estaEnPodioSemanal(String idUsuario) {
        Optional<PodioSemanal> podioOpt = repositorioPodio.obtenerActual();

        if (podioOpt.isEmpty()) {
            return false; // Si no hay podio calculado, nadie está en él
        }

        PodioSemanal podio = podioOpt.get();

        // Verificamos presencia en las 3 categorías
        boolean esProveedor = podio.getProveedorElite() != null &&
                podio.getProveedorElite().stream().anyMatch(e -> e.getIdUsuario().equals(idUsuario));

        boolean esMotor = podio.getMotorEconomia() != null &&
                podio.getMotorEconomia().stream().anyMatch(e -> e.getIdUsuario().equals(idUsuario));

        boolean esEmbajador = podio.getEmbajadorCalidad() != null &&
                podio.getEmbajadorCalidad().stream().anyMatch(e -> e.getIdUsuario().equals(idUsuario));

        return esProveedor || esMotor || esEmbajador;
    }

    private List<EntradaPodio> topNProveedores(List<Transaccion> semanales, int n) {
        Map<String, Long> conteo = semanales.stream()
                .collect(Collectors.groupingBy(Transaccion::getIdOfertante, Collectors.counting()));
        return topN(conteo, n);
    }

    private List<EntradaPodio> topNDemandantes(List<Transaccion> semanales, int n) {
        Map<String, Long> conteo = semanales.stream()
                .collect(Collectors.groupingBy(Transaccion::getIdDemandante, Collectors.counting()));
        return topN(conteo, n);
    }

    private List<EntradaPodio> topN(Map<String, Long> conteo, int n) {
        return conteo.entrySet().stream()
                .filter(e -> e.getValue() >= UMBRAL_MINIMO_TRANSACCIONES)
                .sorted((a, b) -> {
                    int cmp = Long.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return Double.compare(obtenerReputacionHistorica(b.getKey()),
                            obtenerReputacionHistorica(a.getKey()));
                })
                .limit(n)
                .map(e -> new EntradaPodio(
                        e.getKey(), obtenerNombreUsuario(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<EntradaPodio> topNCalidad(List<Transaccion> semanales, int n) {
        Map<String, Double> promedios = semanales.stream()
                .filter(t -> t.getCalificacion() != null)
                .collect(Collectors.groupingBy(
                        Transaccion::getIdOfertante,
                        Collectors.averagingInt(t -> t.getCalificacion())));

        Map<String, Long> conteoOfertante = semanales.stream()
                .collect(Collectors.groupingBy(Transaccion::getIdOfertante, Collectors.counting()));

        return promedios.entrySet().stream()
                .filter(e -> conteoOfertante.getOrDefault(e.getKey(), 0L) >= UMBRAL_MINIMO_TRANSACCIONES)
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return Double.compare(obtenerReputacionHistorica(b.getKey()),
                            obtenerReputacionHistorica(a.getKey()));
                })
                .limit(n)
                .map(e -> new EntradaPodio(
                        e.getKey(), obtenerNombreUsuario(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
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
        if (podio.getProveedorElite() != null && !podio.getProveedorElite().isEmpty())
            idsDestacados.add(podio.getProveedorElite().get(0).getIdUsuario());
        if (podio.getMotorEconomia() != null && !podio.getMotorEconomia().isEmpty())
            idsDestacados.add(podio.getMotorEconomia().get(0).getIdUsuario());
        if (podio.getEmbajadorCalidad() != null && !podio.getEmbajadorCalidad().isEmpty())
            idsDestacados.add(podio.getEmbajadorCalidad().get(0).getIdUsuario());

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
        Optional<PodioSemanal> almacenado = repositorioPodio.obtenerActual();
        if (almacenado.isPresent() && listaTieneDatos(almacenado.get().getProveedorElite())) {
            return convertirADTO(almacenado.get());
        }

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

        dto.setProveedorElite(topNProveedores(semanales, 3));
        dto.setMotorEconomia(topNDemandantes(semanales, 3));
        dto.setEmbajadorCalidad(topNCalidad(semanales, 3));

        return dto;
    }

    private PodioResponseDTO convertirADTO(PodioSemanal podio) {
        PodioResponseDTO dto = new PodioResponseDTO();
        dto.setIdPodio(podio.getIdPodio());
        dto.setFechaInicioSemana(podio.getFechaInicioSemana());
        dto.setFechaFinSemana(podio.getFechaFinSemana());
        dto.setFechaCalculo(podio.getFechaCalculo());
        dto.setProveedorElite(podio.getProveedorElite());
        dto.setMotorEconomia(podio.getMotorEconomia());
        dto.setEmbajadorCalidad(podio.getEmbajadorCalidad());
        return dto;
    }

    private boolean listaTieneDatos(List<EntradaPodio> lista) {
        return lista != null && !lista.isEmpty();
    }
}
