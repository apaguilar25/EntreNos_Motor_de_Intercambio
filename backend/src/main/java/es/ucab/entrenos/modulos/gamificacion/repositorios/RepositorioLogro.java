package es.ucab.entrenos.modulos.gamificacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.gamificacion.modelos.Logro;
import es.ucab.entrenos.modulos.gamificacion.modelos.TipoCriterioLogro;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioLogro implements IRepositorioLogro {

    private static final String RUTA_ARCHIVO = "data/logros.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioLogro() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        inicializarArchivo();
    }

    private void inicializarArchivo() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
                List<Logro> semilla = new ArrayList<>();
                semilla.add(new Logro("INICIADOR_RED", "Iniciador de Red",
                        "Por completar el primer intercambio. ¡Rompe la inercia y da el primer paso!",
                        TipoCriterioLogro.PRIMERA_TRANSACCION, 10));
                semilla.add(new Logro("MAESTRO_CONFIANZA", "Maestro de Confianza",
                        "Por mantener 5 estrellas en 5 tratos con usuarios distintos. La calidad es tu sello.",
                        TipoCriterioLogro.MAESTRO_CONFIANZA, 25));
                semilla.add(new Logro("POLIMATA", "Polímata Comunitario",
                        "Por registrar y prestar servicios en 5 categorías de habilidades diferentes. ¡Todo un experto multidisciplinario!",
                        TipoCriterioLogro.POLIMATA, 50));
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                    gson.toJson(semilla, writer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar logros.json", e);
        }
    }

    @Override
    public List<Logro> obtenerTodos() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Logro>>() {}.getType();
            List<Logro> logros = gson.fromJson(reader, tipoLista);
            return logros != null ? logros : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer logros.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Logro> obtenerPorId(String idLogro) {
        return obtenerTodos().stream()
                .filter(l -> l.getIdLogro().equalsIgnoreCase(idLogro))
                .findFirst();
    }

    @Override
    public void guardar(Logro logro) {
        lock.writeLock().lock();
        try {
            List<Logro> todos = obtenerTodos();
            boolean existe = false;
            for (int i = 0; i < todos.size(); i++) {
                if (todos.get(i).getIdLogro().equalsIgnoreCase(logro.getIdLogro())) {
                    todos.set(i, logro);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                todos.add(logro);
            }
            escribirArchivo(todos);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Logro> logros) {
        lock.writeLock().lock();
        try {
            escribirArchivo(logros);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<Logro> logros) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(logros, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en logros.json", e);
        }
    }
}
