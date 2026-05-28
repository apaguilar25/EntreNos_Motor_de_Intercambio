package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.ArrayList;

public class Usuario {

        private String id;
        private String nombre;
        private String correoElectronico;
        private String contrasena;

        private Monedero monedero;
        private ArrayList<Habilidad> habilidadesOfrecidas;
        private ArrayList<Habilidad> habilidadesNecesitadas;

        private int intentosFallidos;
        private boolean cuentaBloqueada;
        private long tiempoDesbloqueoMillis; // Para contar las 24 horas

    // --- * Constructores * ---

    public Usuario() {
    }

    // Siempre se crea habilidades aunque este vacia
    // Contrasenia se toma en cuenta para sprint 2, por eso no esta en este constructor todavia
    public Usuario(String id, String nombre, String correoElectronico, Monedero monedero) {
        this.id = id;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.monedero = monedero;
        this.monedero = new Monedero();
        this.habilidadesOfrecidas = new ArrayList<>();
        this.habilidadesNecesitadas = new ArrayList<>();
    }

    // --- * Metodos * ---

    // - * Habilidades * -

    public void agregarHabilidadOfrecida(Habilidad nuevaHabilidad) {
        if (nuevaHabilidad == null) {
            throw new IllegalArgumentException("La habilidad no puede estar vacía.");
        }

        // Se verifica con el equals() de la clase Habilidad,
        // .contains() compara los IDs.
        if (this.habilidadesOfrecidas.contains(nuevaHabilidad)) {
            throw new IllegalStateException("Error: Ya ofreces esta habilidad en tu perfil.");
        }

        this.habilidadesOfrecidas.add(nuevaHabilidad);
    }

    /**
     * Agrega un servicio que el usuario está buscando.
     */
    public void agregarNecesidad(Habilidad nuevaNecesidad) {
        if (nuevaNecesidad == null) {
            throw new IllegalArgumentException("La necesidad no puede estar vacía.");
        }

        // Se verifica duplicidad
        if (this.habilidadesNecesitadas.contains(nuevaNecesidad)) {
            throw new IllegalStateException("Error: Ya tienes esta necesidad registrada.");
        }

        this.habilidadesNecesitadas.add(nuevaNecesidad);
    }

    /**
     * Elimina una habilidad del perfil del usuario.
     */
    public void removerHabilidadOfrecida(Habilidad habilidadRemover) {
        if (!this.habilidadesOfrecidas.contains(habilidadRemover)) {
            throw new IllegalStateException("El usuario no posee esta habilidad en su perfil.");
        }
        this.habilidadesOfrecidas.remove(habilidadRemover);
    }

    /**
     * Elimina una habilidad necesitada del perfil del usuario.
     */
    public void removerHabilidadNecesitada(Habilidad habilidadRemover) {
        if (!this.habilidadesNecesitadas.contains(habilidadRemover)) {
            throw new IllegalStateException("El usuario no posee esta habilidad en su perfil.");
        }
        this.habilidadesNecesitadas.remove(habilidadRemover);
    }

    // - * Monedero * -
    /**
     * Delega la responsabilidad de descontar créditos a la Billetera.
     */
    public void pagarServicio(float montoCreditos) {
        // La Billetera internamente validará que no quede en saldo negativo
        this.monedero.descontar(montoCreditos);
    }

    public void recibirCreditos(float montoCreditos) {
        this.monedero.acreditar(montoCreditos);
    }



    // - * Seguridad * -

    // Pendiente agregar funciones referentes al estado de bloqueo.
    // La logica como tal va en la carpeta Seguridad pero lo que guarda el estado va aca


    // --- * Getters * ---

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correoElectronico; }

    public Monedero getMonedero() {return monedero;}

    // Getters inmutables: Se devuelven copias para proteger los datos
    public ArrayList<Habilidad> getHabilidadesOfrecidas() {
        return new ArrayList<>(this.habilidadesOfrecidas);
    }

    public ArrayList<Habilidad> getNecesidadesBusqueda() {
        return new ArrayList<>(this.habilidadesNecesitadas);
    }

}
