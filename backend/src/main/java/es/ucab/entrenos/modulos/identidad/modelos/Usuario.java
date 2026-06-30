package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.ArrayList;

public class Usuario {

    private String id;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private String contrasenaHash;
    private int version;

    private Monedero monedero;
    // El catálogo se vuelve privado y oculto para el mundo exterior
    private CatalogoUsuario catalogo;

    private EstadoCuenta estado;
    private int intentosFallidos;
    private long tiempoDesbloqueoMillis;
    private long primerIntentoFallidoMillis;
    private int reportesFraudeValidados;

    private String urlFotoPerfil;
    private float promedioCalificacion;
    private int cantidadCalificaciones;
    private RolUsuario rol;
    private boolean esVecinoDestacado;

    public Usuario() {
        this.estado = EstadoCuenta.ACTIVO;
        this.rol = RolUsuario.MIEMBRO_COMUNIDAD;
        this.catalogo = new CatalogoUsuario();
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
        this.catalogo = new CatalogoUsuario();
        this.urlFotoPerfil = "default.png";
        this.intentosFallidos = 0;
        this.reportesFraudeValidados = 0;
    }

    /** ==================== ==================== ====================
     * Interacción con el Catálogo y Capital Semilla
     * ==================== ==================== ====================
     */
    // Tu propuesta exacta: El servicio pide las habilidades al Usuario, no al catálogo
    public ArrayList<HabilidadOfrecida> getHabilidadesOfrecidas() {
        return this.catalogo.getHabilidadesOfrecidas();
    }

    public ArrayList<NecesidadRegistrada> getNecesidadesRegistradas() {
        return this.catalogo.getNecesidadesRegistradas();
    }

    public boolean isCatalogoCompletado() {
        return this.catalogo.isCompletado();
    }

    /**
     * El Guardián: Centraliza la validación de estado antes de mutar el catálogo.
     */
    private void asegurarUsuarioActivo() {
        if (this.getEstado() != EstadoCuenta.ACTIVO) {
            throw new IllegalStateException("Operación denegada: La cuenta del usuario no se encuentra activa debido a una sanción o bloqueo de seguridad.");
        }
    }

    public void finalizarConfiguracionCatalogo(float capitalSemilla) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.catalogo.marcarComoCompletado();
        this.monedero.acreditar(capitalSemilla);
    }

    // Habilidad Ofrecida
    public void agregarHabilidadOfrecida(HabilidadOfrecida nuevaHabilidad) {
        asegurarUsuarioActivo(); // Si está bloqueado, explota aquí y protege el catálogo
        incrementarVersion();
        this.catalogo.agregarHabilidadOfrecida(nuevaHabilidad);
    }

    public void actualizarHabilidadOfrecida(String idInstancia, int nuevoPrecio, String nuevaDescripcion) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.catalogo.actualizarHabilidadOfrecida(idInstancia, nuevoPrecio, nuevaDescripcion);
    }

    public void eliminarHabilidadOfrecida(String idInstancia) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.catalogo.eliminarHabilidadOfrecida(idInstancia);
    }

    // Necesidad Registrada
    public void agregarNecesidad(NecesidadRegistrada nuevaNecesidad) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.catalogo.agregarNecesidad(nuevaNecesidad);
    }

    public void actualizarNecesidadRegistrada(String idInstancia, String nuevaDescripcion) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.catalogo.actualizarNecesidadRegistrada(idInstancia, nuevaDescripcion);
    }

    public void eliminarNecesidadRegistrada(String idInstancia) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.catalogo.eliminarNecesidadRegistrada(idInstancia);
    }

    /** ==================== ==================== ====================
     *          MÉTODOS DE NEGOCIO: SEGURIDAD Y SANCIONES
     * ==================== ==================== ====================
     */

    public void registrarIntentoFallido() {
        if (this.estado != EstadoCuenta.ACTIVO) return;
        incrementarVersion();

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
//        incrementarVersion();
        this.intentosFallidos = 0;
        this.primerIntentoFallidoMillis = 0;
    }

    // Refactorizado: Aplica la sanción usando el Enum y la misma variable de tiempo
    public void aplicarSancionPorInactividadSubasta() {
        if (this.estado == EstadoCuenta.SUSPENDIDO_FRAUDE) return; // Fraude pesa más
        incrementarVersion();

        this.estado = EstadoCuenta.SUSPENDIDO_SUBASTA;
        this.tiempoDesbloqueoMillis = System.currentTimeMillis() + (72L * 60 * 60 * 1000);
    }

    // Procesa la sanción del administrador. Al llegar a 2 se suspende permanentemente.
    public void registrarReporteFraudeValidado() {
        if (this.estado == EstadoCuenta.SUSPENDIDO_FRAUDE) return;
        incrementarVersion();

        this.reportesFraudeValidados++;
        if (this.reportesFraudeValidados >= 2) {
            this.estado = EstadoCuenta.SUSPENDIDO_FRAUDE;
        }
    }

    public void perdonarFaltas() {
        incrementarVersion();
        this.reportesFraudeValidados = 0;
        if (this.estado == EstadoCuenta.SUSPENDIDO_FRAUDE) {
            this.estado = EstadoCuenta.ACTIVO;
        }
    }

    public void incrementarVersion(){
        this.version++;
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

    public void agregarCalificacion(int calificacion) {
        incrementarVersion();
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5.");
        }
        float sumaActual = this.promedioCalificacion * this.cantidadCalificaciones;
        this.cantidadCalificaciones++;
        this.promedioCalificacion = Math.round(((sumaActual + calificacion) / this.cantidadCalificaciones) * 10.0f) / 10.0f;
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
    public int getReportesFraudeValidados() { return reportesFraudeValidados; }
    public int getVersion() {return version;}
    public String getUrlFotoPerfil() { return urlFotoPerfil; }
    public float getPromedioCalificacion() { return promedioCalificacion; }
    public int getCantidadCalificaciones() { return cantidadCalificaciones; }
    public boolean isEsVecinoDestacado() { return esVecinoDestacado; }
    public void setEsVecinoDestacado(boolean esVecinoDestacado) { this.esVecinoDestacado = esVecinoDestacado; }


    // Setters
    public void setUrlFotoPerfil(String urlFotoPerfil) {
        asegurarUsuarioActivo();
        incrementarVersion();
        this.urlFotoPerfil = urlFotoPerfil;
    }
    public void setPrimerIntentoFallidoMillis(long primerIntentoFallidoMillis) { this.primerIntentoFallidoMillis = primerIntentoFallidoMillis; }
    public void setRol(RolUsuario rol) {
        incrementarVersion();
        this.rol = rol;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}