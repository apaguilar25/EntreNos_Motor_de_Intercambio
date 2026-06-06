package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.ArrayList;

public class Usuario {

        private String id;
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
        private long primerIntentoFallidoMillis; // Guarda la hora del primer error

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
        if (nuevaHabilidad == null) throw new IllegalArgumentException("La habilidad ofrecida no puede estar vacía.");
        if (this.habilidadesOfrecidas.contains(nuevaHabilidad)) {
            throw new IllegalStateException("Error: Ya ofreces esta habilidad en tu perfil.");
        }
        this.habilidadesOfrecidas.add(nuevaHabilidad);
    }

    public void agregarNecesidad(NecesidadRegistrada nuevaNecesidad) {
        if (nuevaNecesidad == null) throw new IllegalArgumentException("La necesidad no puede estar vacía.");
        if (this.necesidadesRegistradas.contains(nuevaNecesidad)) {
            throw new IllegalStateException("Error: Ya tienes esta necesidad registrada.");
        }
        this.necesidadesRegistradas.add(nuevaNecesidad);
    }

    public void removerHabilidadOfrecida(HabilidadOfrecida habilidadRemover) {
        if (!this.habilidadesOfrecidas.contains(habilidadRemover)) {
            throw new IllegalStateException("El usuario no posee esta habilidad en su perfil.");
        }
        this.habilidadesOfrecidas.remove(habilidadRemover);
    }

    public void removerNecesidad(NecesidadRegistrada necesidadRemover) {
        if (!this.necesidadesRegistradas.contains(necesidadRemover)) {
            throw new IllegalStateException("El usuario no posee esta necesidad en su perfil.");
        }
        this.necesidadesRegistradas.remove(necesidadRemover);
    }


    // - * Monedero * -

    // Delega la responsabilidad de descontar créditos a la Billetera.

    public void pagarServicio(float montoCreditos) {
        this.monedero.descontar(montoCreditos);
    }

    public void recibirCreditos(float montoCreditos) {
        this.monedero.acreditar(montoCreditos);
    }


    // - * Seguridad (Login) * -

    public void incrementarIntentosFallidos(){
        this.intentosFallidos++;
    }

    public boolean isCuentaBloqueada(){
        return this.cuentaBloqueada;
    }

    public void bloquearCuenta(long duracionMillis){
        this.cuentaBloqueada = true;

        // Indica a que hora se desbloqueara la cuenta, sumando el tiempo actual con la duracion del bloqueo
        this.tiempoDesbloqueoMillis = System.currentTimeMillis() + duracionMillis;
    }

    public void resetearIntentosFallidos(){
        this.intentosFallidos = 0;
    }

    public void setPrimerIntentoFallidoMillis(long primerIntentoFallidoMillis) {
        this.primerIntentoFallidoMillis = primerIntentoFallidoMillis;
    }

    public void desbloquearCuenta(){
        this.cuentaBloqueada = false;
        this.tiempoDesbloqueoMillis = 0;
        this.intentosFallidos = 0;
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

    public boolean isCuentaSuspendida() {
        return cuentaSuspendida;
    }

    public long getFinSuspensionMillis() {
        return finSuspensionMillis;
    }

    public ArrayList<NecesidadRegistrada> getNecesidadesRegistradas() {
        return new ArrayList<>(this.necesidadesRegistradas);
    }

    // Setters
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDescripcionPersonal(String descripcionPersonal) { this.descripcionPersonal = descripcionPersonal; }
    public void setUrlFotoPerfil(String urlFotoPerfil) { this.urlFotoPerfil = urlFotoPerfil; }
}
