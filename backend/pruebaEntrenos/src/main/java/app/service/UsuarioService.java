package app.service;

import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    public Object registrarPerfil(String nombre, String correo, String telefono) {
        // 1. Validar correo comunitario (Regla de Negocio)
        if (!correo.matches("^[A-Za-z0-9._%+-]+@comunidad\\.com$")) {
            throw new IllegalArgumentException("El correo debe ser del dominio @comunidad.com");
        }

        // 2. Crear estructura temporal del usuario
        // Aquí instanciarías tu clase Usuario cuando la tengas
        double capitalSemilla = 100.0; // Saldo base asignado automáticamente

        // 3. Retornar el usuario listo para guardar en el JSON
        return "Usuario " + nombre + " registrado con " + capitalSemilla + " créditos.";
    }
}