package org.example.CapaGestion;

import org.example.CapaEntidades.Necesidad;
import org.example.CapaEntidades.Usuario;

public class GestionUsuario {


    public void registrarUsuario(String nombre, String correoElectronico, String telefono, String rutaFoto){

    }

    public void calcularReputacionHistorica(String idUsuario){

    }

    // En lugar de pasar nombreServicio, no seria mejor pasar id para tener uniformidad y manejar tod.o por ids
    public void calcularReputacionServicio(String idUsuarioOfertante, int idServicio, int calificacionServicio){

    }

    public Usuario buscarUsuario(String idUsuario) {

        // PLACEHOLDER PARA QUE NO MARQUE ERROR
        return new Usuario("abc", "abc", "abc", "abc");
    }

    // Modificar Datos Usuario deberia tener en parametros lo que se le va a modificar. O no??
    public void modificarDatosUsuario(String idUsuario, String nombre, String correoElectronico, String descripcionPersonal){

    }

    public GestionUsuario() {
    }
}
