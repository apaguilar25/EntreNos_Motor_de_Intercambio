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

    public void agregarHabilidadNecesitada(Habilidad habilidadNecesitada) {


        this.habilidadesNecesitadas.add(habilidadNecesitada);
    }

    public Monedero getMonedero() {
        return monedero;
    }

    public void setMonedero(Monedero monedero) {
        this.monedero = monedero;
    }

    public ArrayList<Habilidad> getHabilidadesOfrecidas() {
        return habilidadesOfrecidas;
    }

    public void setHabilidadesOfrecidas(ArrayList<Habilidad> habilidadesOfrecidas) {
        this.habilidadesOfrecidas = habilidadesOfrecidas;
    }

    public ArrayList<Habilidad> getHabilidadesNecesitadas() {
        return habilidadesNecesitadas;
    }

    public void setHabilidadesNecesitadas(ArrayList<Habilidad> habilidadesNecesitadas) {
        this.habilidadesNecesitadas = habilidadesNecesitadas;
    }

    public String getId() {
            return id;
        }
    
        public void setId(String id) {
            this.id = id;
        }
    
        public String getNombre() {
            return nombre;
        }
    
        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    
        public String getCorreoElectronico() {
            return correoElectronico;
        }
    
        public void setCorreoElectronico(String correoElectronico) {
            this.correoElectronico = correoElectronico;
        }
    
        public String getContrasena() {
            return contrasena;
        }
    
        public void setContrasena(String contrasena) {
            this.contrasena = contrasena;
        }


}
