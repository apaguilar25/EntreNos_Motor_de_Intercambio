package es.ucab.entrenos.modulos.identidad.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ucab.entrenos.modulos.identidad.modelos.CorreoPermitido;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioCorreoPermitido implements IRepositorioCorreoPermitido {

    private static final String RUTA_ARCHIVO = "data/correos_permitidos.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private List<CorreoPermitido> correosPermitidos = new ArrayList<>();
    private final IRepositorioUsuario repositorioUsuario;

    public RepositorioCorreoPermitido(IRepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        cargarDatos();
    }

    private void cargarDatos() {
        File archivo = new File(RUTA_ARCHIVO);
        if (archivo.exists()) {
            try {
                correosPermitidos = mapper.readValue(archivo, new TypeReference<List<CorreoPermitido>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Inicializar con todos los usuarios existentes + admin
            repositorioUsuario.listarUsuarios().forEach(u -> {
                correosPermitidos.add(new CorreoPermitido(u.getCorreoElectronico()));
            });
            if (correosPermitidos.stream().noneMatch(c -> c.getCorreo().equalsIgnoreCase("admin@alameda.com"))) {
                correosPermitidos.add(new CorreoPermitido("admin@alameda.com"));
            }
            guardarDatos();
        }
    }

    private void guardarDatos() {
        File directorio = new File("data");
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_ARCHIVO), correosPermitidos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CorreoPermitido> obtenerTodos() {
        return new ArrayList<>(correosPermitidos);
    }

    @Override
    public Optional<CorreoPermitido> obtenerPorCorreo(String correo) {
        return correosPermitidos.stream()
                .filter(c -> c.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public void guardar(CorreoPermitido correoPermitido) {
        Optional<CorreoPermitido> existente = obtenerPorCorreo(correoPermitido.getCorreo());
        if (existente.isEmpty()) {
            correosPermitidos.add(correoPermitido);
            guardarDatos();
        }
    }

    @Override
    public void eliminar(String correo) {
        correosPermitidos.removeIf(c -> c.getCorreo().equalsIgnoreCase(correo));
        guardarDatos();
    }
}
