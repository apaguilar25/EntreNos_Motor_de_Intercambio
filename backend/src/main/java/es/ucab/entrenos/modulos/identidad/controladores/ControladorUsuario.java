package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.dtos.ConfiguracionCatalogoRequestDTO;
import es.ucab.entrenos.modulos.identidad.dtos.PerfilUsuarioResponseDTO;
import es.ucab.entrenos.modulos.identidad.dtos.RegistroUsuarioRequestDTO;
import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController // Indica que esta clase expone URLs en formato JSON
@RequestMapping("/api/usuarios") // Todas las URLs de esta clase empezarán con esto
public class ControladorUsuario {

    private final ServicioUsuario servicioUsuario;

    // Inyección de dependencias por constructor
    public ControladorUsuario(ServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    /**
     * Endpoint 1: Registro inicial del usuario
     * URL: POST http://localhost:8080/api/usuarios/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroUsuarioRequestDTO request) {
        try {
            // Pasamos los datos del DTO a nuestro Director de Orquesta (el Servicio)
            Usuario nuevoUsuario = servicioUsuario.registrarUsuario(
                    request.getNombre(),
                    request.getCorreoElectronico(),
                    request.getTelefono(),
                    request.getDescripcionPersonal(),
                    request.getContrasena()
            );

            // Envolvemos al usuario creado en un DTO seguro (sin la contraseña)
            PerfilUsuarioResponseDTO response = new PerfilUsuarioResponseDTO(nuevoUsuario);

            // Retornamos Código 201 (Created) y el perfil limpio
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // Si el correo ya existe o falta un dato, atrapamos el error
            // y devolvemos un 400 (Bad Request) con el mensaje exacto para React
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Endpoint 2: Configuración del Catálogo y Capital Semilla
     * URL: POST http://localhost:8080/api/usuarios/{id}/catalogo
     */
    @PostMapping("/{id}/catalogo")
    public ResponseEntity<?> configurarCatalogo(@PathVariable String id, @RequestBody ConfiguracionCatalogoRequestDTO request) {
        try {
            // 1. Mapear DTOs a las clases de Dominio
            List<HabilidadOfrecida> ofertas = new ArrayList<>();
            if (request.getOfertas() != null) {
                for (ConfiguracionCatalogoRequestDTO.OfertaDTO o : request.getOfertas()) {
                    // Creamos la habilidad base (el equals funciona con el ID, así que esto es seguro)
                    Habilidad base = new Habilidad(o.getIdHabilidadCategoria(), "");
                    ofertas.add(new HabilidadOfrecida(base, o.getPrecioCreditos(), o.getDescripcionServicio()));
                }
            }

            List<NecesidadRegistrada> necesidades = new ArrayList<>();
            if (request.getNecesidades() != null) {
                for (ConfiguracionCatalogoRequestDTO.NecesidadDTO n : request.getNecesidades()) {
                    Habilidad base = new Habilidad(n.getIdHabilidadCategoria(), "");
                    necesidades.add(new NecesidadRegistrada(base, n.getDescripcionCondiciones()));
                }
            }

            // 2. Enviar los datos mapeados al Servicio
            servicioUsuario.configurarCatalogo(id, ofertas, necesidades);

            // 3. Responder éxito (Código 200 OK)
            return ResponseEntity.ok().body("Catálogo configurado y Capital Semilla asignado con éxito.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Atrapa errores como "El catálogo ya fue completado" o "Precio negativo"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // TODO crear para editar catalogo y otros datos

}