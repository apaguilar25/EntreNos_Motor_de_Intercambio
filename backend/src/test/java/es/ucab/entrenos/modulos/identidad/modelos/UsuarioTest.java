package es.ucab.entrenos.modulos.identidad.modelos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void debeAgregarHabilidadCorrectamente() {
        Usuario usuario = new Usuario();
        Habilidad habilidadBase = new Habilidad("HAB-001", "Plomería");
        HabilidadOfrecida nuevaOferta = new HabilidadOfrecida(habilidadBase, 50, "Reparación de tuberías");

        usuario.agregarHabilidadOfrecida(nuevaOferta);

        // ❌ Mensaje de error si falla
        assertEquals(1, usuario.getHabilidadesOfrecidas().size(), "❌ Error: La habilidad no se guardó en el catálogo del usuario.");

        // ✅ Mensaje de éxito si llega hasta aquí
        System.out.println("✅ Éxito: Habilidad guardada correctamente en el catálogo.");
    }

    @Test
    void debeBloquearHabilidadDuplicada() {
        Usuario usuario = new Usuario();
        Habilidad habilidadBase = new Habilidad("HAB-002", "Electricidad");

        HabilidadOfrecida oferta1 = new HabilidadOfrecida(habilidadBase, 40, "Instalación de enchufes");
        HabilidadOfrecida oferta2 = new HabilidadOfrecida(habilidadBase, 40, "Instalación de enchufes"); // Exactamente igual

        usuario.agregarHabilidadOfrecida(oferta1);

        IllegalStateException excepcion = assertThrows(IllegalStateException.class, () -> {
            usuario.agregarHabilidadOfrecida(oferta2);
        }, "❌ Error: El sistema permitió agregar una habilidad duplicada.");

        assertTrue(excepcion.getMessage().contains("ya ofreces esta habilidad"), "❌ Error: El mensaje de excepción no es el esperado.");

        System.out.println("✅ Éxito: El sistema bloqueó correctamente la habilidad duplicada.");
    }
}