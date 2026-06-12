package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.ArrayList;

public class Usuario {

    private String id;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private boolean catalogoCompletado;
    private String contrasenaHash;
    private int version;

    private Monedero monedero;
    private ArrayList<HabilidadOfrecida> habilidadesOfrecidas;
    private ArrayList<NecesidadRegistrada> necesidadesRegistradas;

    // --- * Control de Acceso y Estado Único * ---
    private EstadoCuenta estado;
    private int intentosFallidos;
    private long tiempoDesbloqueoMillis; // Sirve tanto para las 24h de Login como para las 72h de Subasta
    private long primerIntentoFallidoMillis;
    private int reportesFraudeValidados; // Requisito ERS: Máximo 2

    // --- * Perfil y Reputación * ---
    private String urlFotoPerfil;
    private float promedioCalificacion;
    private int cantidadCalificaciones;

    private RolUsuario rol;

    /*==================== ==================== ====================
     * Constructores
     * ==================== ==================== ====================
     */

    public Usuario() {
        this.estado = EstadoCuenta.ACTIVO;
        this.rol = RolUsuario.MIEMBRO_COMUNIDAD;
        this.habilidadesOfrecidas = new ArrayList<>();
        this.necesidadesRegistradas = new ArrayList<>();
        this.intentosFallidos = 0;
        this.reportesFraudeValidados = 0;
    }

    public Usuario(String id, String nombre, String correoElectronico, String telefono, String descripcionPersonal, String contrasenaHash) {
        this.id = id;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.descripcionPersonal = descripcionPersonal;
        this.contrasenaHash = contrasenaHash;
        this.estado = EstadoCuenta.ACTIVO;
        this.rol = RolUsuario.MIEMBRO_COMUNIDAD;
        this.monedero = new Monedero(0.0f);
        this.habilidadesOfrecidas = new ArrayList<>();
        this.necesidadesRegistradas = new ArrayList<>();
        this.urlFotoPerfil = "default.png";
        this.intentosFallidos = 0;
        this.reportesFraudeValidados = 0;
    }

    /** ==================== ==================== ====================
     * Catálogo, Habilidades y Capital Semilla
     * ==================== ==================== ====================
     */

    public void finalizarConfiguracionCatalogo(float capitalSemilla) {
        if (this.catalogoCompletado) {
            throw new IllegalStateException("El usuario ya completó su catálogo y reclamó su capital semilla.");
        }
        this.catalogoCompletado = true;
        this.monedero.acreditar(capitalSemilla);
    }

    public void agregarHabilidadOfrecida(HabilidadOfrecida nuevaHabilidad) {
        if (nuevaHabilidad == null) throw new IllegalArgumentException("La habilidad no puede ser nula.");

        boolean duplicada = this.habilidadesOfrecidas.stream()
                .anyMatch(h -> h.getHabilidadBase().getId().equals(nuevaHabilidad.getHabilidadBase().getId())
                        && h.getDescripcionServicio().equalsIgnoreCase(nuevaHabilidad.getDescripcionServicio()));

        if (duplicada) {
            throw new IllegalStateException("Ya tienes una oferta para esta habilidad con la misma descripción.");
        }
        this.habilidadesOfrecidas.add(nuevaHabilidad);
    }

    public void actualizarHabilidadOfrecida(String idInstancia, int nuevoPrecio, String nuevaDescripcion) {
        HabilidadOfrecida existente = this.habilidadesOfrecidas.stream()
                .filter(h -> h.getIdInstancia().equals(idInstancia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la oferta específica en el perfil del usuario."));

        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty() && !nuevaDescripcion.equals(existente.getDescripcionServicio())) {
            boolean conflicto = this.habilidadesOfrecidas.stream()
                    .anyMatch(h -> !h.getIdInstancia().equals(idInstancia)
                            && h.getHabilidadBase().getId().equals(existente.getHabilidadBase().getId())
                            && h.getDescripcionServicio().equalsIgnoreCase(nuevaDescripcion.trim()));

            if (conflicto) {
                throw new IllegalStateException("Ya tienes otra oferta de esta misma categoría con esa descripción.");
            }
            existente.setDescripcionServicio(nuevaDescripcion.trim());
        }

        if (nuevoPrecio > 0) {
            existente.setPrecioCreditos(nuevoPrecio);
        }
    }

    public void actualizarNecesidadRegistrada(String idInstancia, String nuevaDescripcion) {
        NecesidadRegistrada existente = this.necesidadesRegistradas.stream()
                .filter(n -> n.getIdInstancia().equals(idInstancia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la necesidad específica."));

        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty() && !nuevaDescripcion.equals(existente.getDescripcionCondiciones())) {
            boolean conflicto = this.necesidadesRegistradas.stream()
                    .anyMatch(n -> !n.getIdInstancia().equals(idInstancia)
                            && n.getNecesidadBase().getId().equals(existente.getNecesidadBase().getId())
                            && n.getDescripcionCondiciones().equalsIgnoreCase(nuevaDescripcion.trim()));

            if (conflicto) {
                throw new IllegalStateException("Ya tienes otra necesidad de esta misma categoría con esas condiciones exactas.");
            }
            existente.setDescripcionCondiciones(nuevaDescripcion.trim());
        }
    }

    public void agregarNecesidad(NecesidadRegistrada nuevaNecesidad) {
        if (nuevaNecesidad == null) throw new IllegalArgumentException("La necesidad no puede estar vacía.");

        boolean duplicada = this.necesidadesRegistradas.stream()
                .anyMatch(n -> n.getNecesidadBase().getId().equals(nuevaNecesidad.getNecesidadBase().getId())
                        && n.getDescripcionCondiciones().equalsIgnoreCase(nuevaNecesidad.getDescripcionCondiciones()));

        if (duplicada) {
            throw new IllegalStateException("Error: Ya tienes esta necesidad registrada con esas mismas condiciones.");
        }
        this.necesidadesRegistradas.add(nuevaNecesidad);
    }

    public void eliminarHabilidadOfrecida(String idInstancia) {
        boolean eliminado = this.habilidadesOfrecidas.removeIf(h -> h.getIdInstancia().equals(idInstancia));
        if (!eliminado) throw new IllegalArgumentException("No se encontró la oferta específica para eliminar.");
    }

    public void eliminarNecesidadRegistrada(String idInstancia) {
        boolean eliminado = this.necesidadesRegistradas.removeIf(n -> n.getIdInstancia().equals(idInstancia));
        if (!eliminado) throw new IllegalArgumentException("No se encontró la necesidad específica para eliminar.");
    }

    public boolean isCatalogoCompletado() { return catalogoCompletado; }

    /** ==================== ==================== ====================
     *                          Monedero
     * ==================== ==================== ====================
     */

    public void pagarServicio(float montoCreditos) {
        this.monedero.descontar(montoCreditos);
    }

    public void recibirCreditos(float montoCreditos) {
        this.monedero.acreditar(montoCreditos);
    }

    /** ==================== ==================== ====================
     *          MÉTODOS DE NEGOCIO: SEGURIDAD Y SANCIONES
     * ==================== ==================== ====================
     */

    public void registrarIntentoFallido() {
        if (this.estado != EstadoCuenta.ACTIVO) return;

        long ahora = System.currentTimeMillis();

        if (ahora - this.primerIntentoFallidoMillis > 3 * 60 * 1000) {
            this.primerIntentoFallidoMillis = ahora;
            this.intentosFallidos = 1;
        } else {
            this.intentosFallidos++;
        }

        if (this.intentosFallidos >= 5) {
            this.estado = EstadoCuenta.BLOQUEADO_SEGURIDAD;
            this.tiempoDesbloqueoMillis = ahora + (24 * 60 * 60 * 1000);
        }
    }

    public void registrarInicioSesionExitoso() {
        this.intentosFallidos = 0;
        this.primerIntentoFallidoMillis = 0;
    }

    // Refactorizado: Aplica la sanción usando el Enum y la misma variable de tiempo
    public void aplicarSancionPorInactividadSubasta() {
        if (this.estado == EstadoCuenta.SUSPENDIDO_FRAUDE) return; // Fraude pesa más

        this.estado = EstadoCuenta.SUSPENDIDO_SUBASTA;
        this.tiempoDesbloqueoMillis = System.currentTimeMillis() + (72L * 60 * 60 * 1000);
    }

    // Procesa la sanción del administrador. Al llegar a 2 se suspende permanentemente.
    public void registrarReporteFraudeValidado() {
        if (this.estado == EstadoCuenta.SUSPENDIDO_FRAUDE) return;

        this.reportesFraudeValidados++;
        if (this.reportesFraudeValidados >= 2) {
            this.estado = EstadoCuenta.SUSPENDIDO_FRAUDE;
        }
    }

    /** ==================== ==================== ====================
     * Getters Inteligentes y Básicos
     * ==================== ==================== ====================
     */

    // Verifica dinámicamente si cualquier castigo temporal ya expiró
    public EstadoCuenta getEstado() {
        if (this.estado == EstadoCuenta.BLOQUEADO_SEGURIDAD || this.estado == EstadoCuenta.SUSPENDIDO_SUBASTA) {
            if (System.currentTimeMillis() >= this.tiempoDesbloqueoMillis) {
                EstadoCuenta estadoAnterior = this.estado;

                this.estado = EstadoCuenta.ACTIVO;
                this.tiempoDesbloqueoMillis = 0;

                // Si venía de un bloqueo de login, limpiamos su historial de errores
                if (estadoAnterior == EstadoCuenta.BLOQUEADO_SEGURIDAD) {
                    this.intentosFallidos = 0;
                    this.primerIntentoFallidoMillis = 0;
                }
            }
        }
        return this.estado;
    }

    // Métodos de conveniencia para tu código antiguo (Leen desde el Enum)
    public boolean isCuentaBloqueada() {
        return getEstado() == EstadoCuenta.BLOQUEADO_SEGURIDAD;
    }

    public boolean isCuentaSuspendida() {
        return getEstado() == EstadoCuenta.SUSPENDIDO_FRAUDE || getEstado() == EstadoCuenta.SUSPENDIDO_SUBASTA;
    }

    public boolean tieneSancionActiva() {
        return getEstado() == EstadoCuenta.SUSPENDIDO_SUBASTA;
    }
    public void agregarCalificacion(int calificacion) {
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5.");
        }
        float sumaActual = this.promedioCalificacion * this.cantidadCalificaciones;
        this.cantidadCalificaciones++;
        this.promedioCalificacion = (sumaActual + calificacion) / this.cantidadCalificaciones;
    }

    public boolean isAdministrador() {
        return this.rol == RolUsuario.ADMINISTRADOR;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getTelefono() { return telefono; }
    public String getDescripcionPersonal() { return descripcionPersonal; }
    public String getContrasenaHash() { return contrasenaHash; }
    public Monedero getMonedero() { return monedero; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public long getPrimerIntentoFallidoMillis() { return primerIntentoFallidoMillis; }
    public long getTiempoDesbloqueoMillis() { return tiempoDesbloqueoMillis; }
    public RolUsuario getRol() { return rol; }
    public int getVersion() {return version;}
    public String getUrlFotoPerfil() { return urlFotoPerfil; }
    public float getPromedioCalificacion() { return promedioCalificacion; }
    public int getCantidadCalificaciones() { return cantidadCalificaciones; }

    public ArrayList<HabilidadOfrecida> getHabilidadesOfrecidas() { return new ArrayList<>(this.habilidadesOfrecidas); }
    public ArrayList<NecesidadRegistrada> getNecesidadesRegistradas() { return new ArrayList<>(this.necesidadesRegistradas); }

    // Setters
    public void setCatalogoCompletado(boolean c) { this.catalogoCompletado = c; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDescripcionPersonal(String descripcionPersonal) { this.descripcionPersonal = descripcionPersonal; }
    public void setUrlFotoPerfil(String urlFotoPerfil) { this.urlFotoPerfil = urlFotoPerfil; }
    public void setPrimerIntentoFallidoMillis(long primerIntentoFallidoMillis) { this.primerIntentoFallidoMillis = primerIntentoFallidoMillis; }
    public void setVersion(int version) {this.version = version;}
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public void setEstado(EstadoCuenta estado) {this.estado = estado; }
}