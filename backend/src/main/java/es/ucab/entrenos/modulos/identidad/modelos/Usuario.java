package es.ucab.entrenos.modulos.identidad.modelos;

import es.ucab.entrenos.modulos.identidad.excepciones.HabilidadDuplicadaException;

import java.util.ArrayList;

public class Usuario {

        private String id;
        private RolUsuario rol;
        private String nombre;
        private String correoElectronico;
        private String telefono;
        private String descripcionPersonal;
        private boolean catalogoCompletado; // Controla la inyección del Capital Semilla
        private String contrasenaHash;

        private Monedero monedero;
        private ArrayList<HabilidadOfrecida> habilidadesOfrecidas;
        private ArrayList<NecesidadRegistrada> necesidadesRegistradas;

        private int intentosFallidos;
        private boolean cuentaBloqueada;
        private long tiempoDesbloqueoMillis; // Para contar las 24 horas
        private long primerIntentoFallidoMillis; // Guarda la hora del primer intento fallido
        private int reportesFraudeValidados;
        private boolean baneadoPermanentemente;
        private int version; // Variable para controlar sincronizacion en persistencia y evitar condiciones de carrera

        // --- * Perfil y Reputación * ---
        private String urlFotoPerfil;
        private float promedioCalificacion;
        private int cantidadCalificaciones; // Ej: 15 (Para saber entre cuántos se dividió)

        // --- * Sanciones Administrativas (Diferente al bloqueo de seguridad por Login) * ---
        private boolean cuentaSuspendida;
        private long finSuspensionMillis; // Cuando termina la suspensión administrativa


    // --- * Constructores * ---

    public Usuario() {
    }

    public Usuario(String id, String nombre, String correoElectronico, String telefono, String descripcionPersonal, String contrasenaHash) {
        this.id = id;
        this.nombre = nombre;
        this.rol = RolUsuario.MIEMBRO_COMUNIDAD;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.descripcionPersonal = descripcionPersonal;
        this.contrasenaHash = contrasenaHash;

        this.monedero = new Monedero();
        this.habilidadesOfrecidas = new ArrayList<>();
        this.necesidadesRegistradas = new ArrayList<>();

        this.intentosFallidos = 0;
        this.cuentaBloqueada = false;
        this.cuentaSuspendida = false;
        this.reportesFraudeValidados = 0;
        this.baneadoPermanentemente = false;

        this.catalogoCompletado = false; // Nace en false hasta que configure sus habilidades

        this.urlFotoPerfil = "default.png";
        this.promedioCalificacion = 0.0f;
        this.cantidadCalificaciones = 0;
    }

    // --- * Metodos * ---

    // - * Metodos de Catálogo y Capital Semilla * -

    /**
     * ERS: Al finalizar la creación del catálogo, el sistema debe añadir
     * el capital semilla al monedero del usuario automáticamente.
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

        // Validamos que no tenga otra oferta con la misma habilidad Y la misma descripción
        boolean duplicada = this.habilidadesOfrecidas.stream()
                .anyMatch(h -> h.getHabilidadBase().getId().equals(nuevaHabilidad.getHabilidadBase().getId())
                        && h.getDescripcionServicio().equalsIgnoreCase(nuevaHabilidad.getDescripcionServicio()));

        if (duplicada) {
            throw new IllegalStateException("Ya tienes una oferta para esta habilidad con la misma descripción.");
        }

        this.habilidadesOfrecidas.add(nuevaHabilidad);
    }

    public void actualizarHabilidadOfrecida(String idInstancia, int nuevoPrecio, String nuevaDescripcion) {
        // 1. Buscamos la oferta específica por su ID de INSTANCIA
        HabilidadOfrecida existente = this.habilidadesOfrecidas.stream()
                .filter(h -> h.getIdInstancia().equals(idInstancia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la oferta específica en el perfil del usuario."));

        // 2. Si hay una nueva descripción válida, verificamos que no choque con otra existente
        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty() && !nuevaDescripcion.equals(existente.getDescripcionServicio())) {

            // Validamos que no exista otra oferta de la MISMA categoría con esa MISMA descripción
            boolean conflicto = this.habilidadesOfrecidas.stream()
                    .anyMatch(h -> !h.getIdInstancia().equals(idInstancia)
                            && h.getHabilidadBase().getId().equals(existente.getHabilidadBase().getId())
                            && h.getDescripcionServicio().equalsIgnoreCase(nuevaDescripcion.trim()));

            if (conflicto) {
                throw new IllegalStateException("Ya tienes otra oferta de esta misma categoría con esa descripción.");
            }
            existente.setDescripcionServicio(nuevaDescripcion.trim());
        }

        // 3. Actualizamos el precio de forma inteligente
        if (nuevoPrecio > 0) {
            existente.setPrecioCreditos(nuevoPrecio);
        }
    }

    public void actualizarNecesidadRegistrada(String idInstancia, String nuevaDescripcion) {
        // 1. Buscamos la necesidad específica por su ID de INSTANCIA
        NecesidadRegistrada existente = this.necesidadesRegistradas.stream()
                .filter(n -> n.getIdInstancia().equals(idInstancia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la necesidad específica en el perfil del usuario."));

        // 2. Si hay una nueva descripción válida, verificamos que no choque con otra existente
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

        // Validamos que no tenga otra necesidad con la misma categoría Y las mismas condiciones
        boolean duplicada = this.necesidadesRegistradas.stream()
                .anyMatch(n -> n.getNecesidadBase().getId().equals(nuevaNecesidad.getNecesidadBase().getId())
                        && n.getDescripcionCondiciones().equalsIgnoreCase(nuevaNecesidad.getDescripcionCondiciones()));

        if (duplicada) {
            throw new IllegalStateException("Error: Ya tienes esta necesidad registrada con esas mismas condiciones.");
        }

        this.necesidadesRegistradas.add(nuevaNecesidad);
    }

    public void eliminarHabilidadOfrecida(String idInstancia) {
        // Intentamos remover el objeto cuyo idInstancia coincida
        boolean eliminado = this.habilidadesOfrecidas.removeIf(h -> h.getIdInstancia().equals(idInstancia));

        if (!eliminado) {
            throw new IllegalArgumentException("No se encontró la oferta específica para eliminar.");
        }
    }
    public void eliminarNecesidadRegistrada(String idInstancia) {
        // Intentamos remover el objeto cuyo idInstancia coincida
        boolean eliminado = this.necesidadesRegistradas.removeIf(n -> n.getIdInstancia().equals(idInstancia));

        if (!eliminado) {
            throw new IllegalArgumentException("No se encontró la necesidad específica para eliminar.");
        }
    }

    // - * Monedero * -

    // Delega la responsabilidad de descontar créditos a la Billetera.

    public void pagarServicio(float montoCreditos) {
        this.monedero.descontar(montoCreditos);
    }

    public void recibirCreditos(float montoCreditos) {
        this.monedero.acreditar(montoCreditos);
    }


    // =========================================================================
    // - * MÉTODOS DE NEGOCIO: SEGURIDAD Y CONTROL DE ACCESO * -
    // =========================================================================
    /**
     * Registra un fallo por datos erroneos en el login
     * aplica un bloqueo estricto de 24 horas si alcanza los 5 intentos fallidos
     * en menos de 3 minutos.
     */
    public void registrarIntentoFallido(long ventanaMs, int maxIntentos, long duracionBloqueoMs) {
        long ahora = System.currentTimeMillis();

        // 1. Validar si la ventana de tiempo del ataque de fuerza bruta ya caducó
        if (this.intentosFallidos > 0 && (ahora - this.primerIntentoFallidoMillis > ventanaMs)) {
            this.intentosFallidos = 0;
            this.primerIntentoFallidoMillis = 0;
        }

        // 2. Registrar el error actual
        if (this.intentosFallidos == 0) {
            this.primerIntentoFallidoMillis = ahora; // Iniciamos el cronómetro de la ráfaga
        }
        this.intentosFallidos++;

        // 3. Aplicar sanción inmediata si se llegó al límite de intentos permitidos
        if (this.intentosFallidos >= maxIntentos) {
            this.cuentaBloqueada = true;
            this.tiempoDesbloqueoMillis = ahora + duracionBloqueoMs;
        }
    }

    public void registrarInicioSesionExitoso() {
        this.intentosFallidos = 0;
        this.primerIntentoFallidoMillis = 0;
    }

    public boolean evaluarBloqueoTemporal(long ahora) {
        if (this.cuentaBloqueada) {
            if (ahora >= this.tiempoDesbloqueoMillis) {
                // El castigo terminó: se libera el estado internamente
                this.cuentaBloqueada = false;
                this.tiempoDesbloqueoMillis = 0;
                this.intentosFallidos = 0;
                this.primerIntentoFallidoMillis = 0;
            } else {
                return true; // Sigue bloqueado
            }
        }
        return false;
    }

    public void aplicarSancionPorInactividadSubasta() {
        this.cuentaSuspendida = true;
        // 72 horas convertidas a milisegundos
        this.finSuspensionMillis = System.currentTimeMillis() + (72L * 60 * 60 * 1000);
    }

    public boolean tieneSancionActiva() {
        // Si estaba suspendido pero el tiempo ya pasó, le quitamos la sanción
        if (this.cuentaSuspendida && System.currentTimeMillis() > this.finSuspensionMillis) {
            this.cuentaSuspendida = false;
            this.finSuspensionMillis = 0;
        }
        return this.cuentaSuspendida;
    }


    public void resetearIntentosFallidos(){
        this.intentosFallidos = 0;
    }

    public void setPrimerIntentoFallidoMillis(long primerIntentoFallidoMillis) {
        this.primerIntentoFallidoMillis = primerIntentoFallidoMillis;
    }

    // =========================================================================
    // - * MÉTODOS DE NEGOCIO: SEGURIDAD Y CONTROL DE ACCESO * -
    // =========================================================================
    /**
     * Registro de Fraude (Lleva a Baneo)
     */    public void registrarReporteFraudeValidado() {
        this.reportesFraudeValidados++;
        // Si alcanza 2 reportes, se bloquea el acceso de por vida
        if (this.reportesFraudeValidados >= 2) {
            this.baneadoPermanentemente = true;
        }
    }

    public boolean isBaneadoPermanentemente() {
        return baneadoPermanentemente;
    }

    public boolean isCuentaBloqueada() {
        return cuentaBloqueada;
    }

    // --- * Getters y Setters Básicos * ---
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getTelefono() { return telefono; }
    public String getDescripcionPersonal() { return descripcionPersonal; }
    public String getContrasenaHash() { return contrasenaHash; }
    public Monedero getMonedero() { return monedero; }
    public boolean isCatalogoCompletado() { return catalogoCompletado; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public long getPrimerIntentoFallidoMillis() { return primerIntentoFallidoMillis; }
    public long getTiempoDesbloqueoMillis() { return tiempoDesbloqueoMillis; }

    // Getters inmutables (Devuelven copias de las listas envolventes)
    public ArrayList<HabilidadOfrecida> getHabilidadesOfrecidas() {
        return new ArrayList<>(this.habilidadesOfrecidas);
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public float getPromedioCalificacion() {
        return promedioCalificacion;
    }

    public int getCantidadCalificaciones() {
        return cantidadCalificaciones;
    }

    public boolean isAdministrador() {
        return this.rol == RolUsuario.ADMINISTRADOR;
    }
        // Todo, borrar isCuentaSuspendida porque ya existe y se utilizar isCuentaBloqueada (?
    public boolean isCuentaSuspendida() {
        return cuentaSuspendida;
    }

    public ArrayList<NecesidadRegistrada> getNecesidadesRegistradas() {
        return new ArrayList<>(this.necesidadesRegistradas);
    }

    public RolUsuario getRol() { return rol; }

    // Setters
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDescripcionPersonal(String descripcionPersonal) { this.descripcionPersonal = descripcionPersonal; }
    public void setUrlFotoPerfil(String urlFotoPerfil) { this.urlFotoPerfil = urlFotoPerfil; }

    public int getVersion() {return version;}

    public void setVersion(int version) {this.version = version;}

    public void setRol(RolUsuario rol) { this.rol = rol; }



}

