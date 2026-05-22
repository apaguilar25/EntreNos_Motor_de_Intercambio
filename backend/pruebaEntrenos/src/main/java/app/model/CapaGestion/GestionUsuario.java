package app.model.CapaGestion;

import app.model.CapaEntidades.Habilidad;
import app.model.CapaEntidades.Necesidad;
import app.model.CapaEntidades.Usuario;
import app.model.CapaPersistencia.PersistenciaUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GestionUsuario {

    @Autowired
    private PersistenciaUsuario persistenciaUsuario;

    // 1. Registrar Usuario
    public void registrarUsuario(String nombre, String correo, String telefono, String foto) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();

        Usuario nuevoUsuario = new Usuario();
        // Generamos un ID único, ej: USR-A1B2C
        nuevoUsuario.setIdUsuario("USR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setCorreoElectronico(correo);
        nuevoUsuario.setTelefono(telefono);
        nuevoUsuario.setReputacionHistorica(0.0); // Nace con 0 de reputación
        nuevoUsuario.getSancion().setSancionActiva(false); // Nace sin sanciones


        usuarios.add(nuevoUsuario);
        persistenciaUsuario.guardar(usuarios);
        System.out.println("[SISTEMA] Usuario registrado con éxito: " + nuevoUsuario.getNombre());
    }

    // Para usuario registrado desde el frontend
    public void registrarUsuario(Usuario usuario) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();

        Usuario nuevoUsuario = new Usuario();
        // Generamos un ID único, ej: USR-A1B2C
        nuevoUsuario.setIdUsuario("USR-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        nuevoUsuario.setReputacionHistorica(0.0); // Nace con 0 de reputación
        nuevoUsuario.getSancion().setSancionActiva(false); // Nace sin sanciones


        usuarios.add(nuevoUsuario);
        persistenciaUsuario.guardar(usuarios);
        System.out.println("[SISTEMA] Usuario registrado con éxito: " + nuevoUsuario.getNombre());
    }

    // 2. Buscar Usuario
    public Usuario buscarUsuario(String idUsuario) {
        return persistenciaUsuario.cargar().stream()
                .filter(u -> u.getIdUsuario().equals(idUsuario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario));
    }

    // 3. Modificar Datos Usuario (Adaptado para recibir los datos nuevos)
    public void modificarDatosUsuario(String idUsuario, String nuevoNombre, String nuevoTelefono, String nuevaDescripcion) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();
        boolean encontrado = false;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(idUsuario)) {
                if (nuevoNombre != null && !nuevoNombre.isEmpty()) u.setNombre(nuevoNombre);
                if (nuevoTelefono != null && !nuevoTelefono.isEmpty()) u.setTelefono(nuevoTelefono);
                if (nuevaDescripcion != null && !nuevaDescripcion.isEmpty()) u.setDescripcionPersonal(nuevaDescripcion);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            throw new IllegalArgumentException("No se pudo modificar. Usuario no encontrado: " + idUsuario);
        }

        persistenciaUsuario.guardar(usuarios);
        System.out.println("[SISTEMA] Datos actualizados para el usuario: " + idUsuario);
    }

    // 4. Actualizar Catálogo de Usuario (Habilidades y Necesidades)
    public void actualizarCatalogo(String idUsuario, List<Habilidad> habilidades, List<Necesidad> necesidades) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();
        boolean encontrado = false;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(idUsuario)) {
                u.setHabilidades(new java.util.ArrayList<>(habilidades));
                u.setNecesidades(new java.util.ArrayList<>(necesidades));
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            throw new IllegalArgumentException("No se pudo actualizar el catálogo. Usuario no encontrado: " + idUsuario);
        }

        persistenciaUsuario.guardar(usuarios);
        System.out.println("[SISTEMA] Catálogo actualizado para el usuario: " + idUsuario);
    }

    // 5. Comprometer Créditos del Monedero
    public void comprometerCreditos(String idUsuario, int monto) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();
        boolean encontrado = false;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(idUsuario)) {
                if (u.getMonedero() == null) {
                    app.model.CapaEntidades.Monedero nuevoMonedero = new app.model.CapaEntidades.Monedero();
                    nuevoMonedero.setCreditosDisponibles(20);
                    u.setMonedero(nuevoMonedero);
                }
                u.getMonedero().calcularCreditosComprometidos(monto);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            throw new IllegalArgumentException("No se encontró el usuario para comprometer créditos: " + idUsuario);
        }

        persistenciaUsuario.guardar(usuarios);
        System.out.println("[SISTEMA] Créditos comprometidos (" + monto + ") para el usuario: " + idUsuario);
    }

    // 5.5 Revertir Créditos Comprometidos
    public void revertirCreditosComprometidos(String idUsuario, int monto) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();
        boolean encontrado = false;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(idUsuario)) {
                if (u.getMonedero() != null) {
                    u.getMonedero().revertirCreditosComprometidos(monto);
                }
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            throw new IllegalArgumentException("No se encontró el usuario para revertir créditos: " + idUsuario);
        }

        persistenciaUsuario.guardar(usuarios);
        System.out.println("[SISTEMA] Créditos revertidos (" + monto + ") para el usuario: " + idUsuario);
    }

    // 4. Calcular Reputación Histórica
    public void calcularReputacionHistorica(String idUsuario) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();
        boolean encontrado = false;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(idUsuario)) {
                System.out.println("[SISTEMA] Reputación histórica consolidada para " + idUsuario + ": " + u.getReputacionHistorica() + " estrellas.");
                encontrado = true;
                break;
            }
        }

        if (!encontrado) throw new IllegalArgumentException("Usuario no encontrado: " + idUsuario);

        persistenciaUsuario.guardar(usuarios);
    }

    // 5. Calcular Reputación Servicio
    public void calcularReputacionServicio(String idUsuario, String nombreServicio, int calificacion) {
        List<Usuario> usuarios = persistenciaUsuario.cargar();
        boolean encontrado = false;

        for (Usuario u : usuarios) {
            if (u.getIdUsuario().equals(idUsuario)) {
                System.out.println("[SISTEMA] Servicio '" + nombreServicio + "' calificado con " + calificacion + " estrellas.");

                double reputacionActual = (u.getReputacionHistorica() != null) ? u.getReputacionHistorica() : 0.0;
                double nuevaReputacion = (reputacionActual == 0.0) ? calificacion : (reputacionActual + calificacion) / 2.0;

                nuevaReputacion = Math.round(nuevaReputacion * 10.0) / 10.0;
                u.setReputacionHistorica(nuevaReputacion);

                encontrado = true;
                break;
            }
        }
        if (!encontrado) throw new IllegalArgumentException("Usuario no encontrado para calificar: " + idUsuario);
        persistenciaUsuario.guardar(usuarios);
    }
}