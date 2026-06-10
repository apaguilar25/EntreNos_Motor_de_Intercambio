package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.excepciones.CorreoDuplicadoException;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Activa Mockito para esta prueba
class ServicioUsuarioTest {

    @Mock
    private IRepositorioUsuario repositorioUsuarioFalso; // Nuestro JSON falso

    @InjectMocks
    private ServicioUsuario servicioUsuario; // El servicio real que vamos a probar

    @Test
    void debeRechazarCorreoConDominioInvalido() {
        // Actuar y Comprobar a la vez: Esperamos que lance un IllegalArgumentException
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            servicioUsuario.registrarUsuario("Juan", "juan@gmail.com", "04141234567", "Un tipo genial", "12345");
        }, "❌ Error: El sistema permitió registrar un usuario con un dominio de correo inválido (@gmail.com).");

        // Comprobamos que el mensaje de error sea el correcto según el ERS
        assertTrue(excepcion.getMessage().contains("@alameda.com"),
                "❌ Error: El mensaje de excepción devuelto no hace mención al dominio oficial @alameda.com.");

        // Verificamos que NUNCA se haya intentado guardar en el repositorio
        verify(repositorioUsuarioFalso, never()).guardar(any());

        // Mensaje de éxito si todo lo anterior pasa
        System.out.println("✅ Éxito: El sistema bloqueó correctamente el registro por dominio de correo inválido.");
    }

    @Test
    void debeRechazarCorreoDuplicado() {
        // Preparar: Le decimos a nuestro repositorio falso que cuando alguien busque "pedro@alameda.com",
        // finja que ya existe un usuario con ese correo.
        when(repositorioUsuarioFalso.buscarPorCorreo("pedro@alameda.com"))
                .thenReturn(Optional.of(new Usuario()));

        // Actuar y Comprobar
        assertThrows(CorreoDuplicadoException.class, () -> {
            servicioUsuario.registrarUsuario("Pedro", "pedro@alameda.com", "04120000000", "Desc", "12345");
        }, "❌ Error: El sistema permitió registrar un usuario con un correo electrónico que ya existía en la base de datos.");

        // Mensaje de éxito si la excepción se lanza correctamente
        System.out.println("✅ Éxito: El sistema bloqueó correctamente el registro por correo electrónico duplicado.");
    }

    @Test
    void debeRechazarTelefonoDuplicado() {
        // Preparar: Simulamos que ya existe un usuario con este teléfono
        when(repositorioUsuarioFalso.buscarPorTelefono("04141234567"))
                .thenReturn(Optional.of(new Usuario()));

        // Actuar y Comprobar
        assertThrows(RuntimeException.class, () -> {
            // Nota: Aquí el correo está bien (@alameda.com), pero el teléfono es el que causará el fallo
            servicioUsuario.registrarUsuario("Ana", "ana@alameda.com", "04141234567", "Diseñadora", "12345");
        }, "❌ Error: El sistema permitió registrar un usuario con un teléfono que ya existía.");

        // Mensaje de éxito
        System.out.println("✅ Éxito: El sistema bloqueó correctamente el registro por teléfono duplicado.");
    }
}