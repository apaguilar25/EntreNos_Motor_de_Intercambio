package es.ucab.entrenos.modulos.subasta.utilidades;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ManejadorCandados {
    // Mapa seguro para guardar un candado por cada ID de subasta
    private static final ConcurrentHashMap<String, ReentrantReadWriteLock> candados = new ConcurrentHashMap<>();

    /**
     * Devuelve el candado asociado a una subasta. Si no existe, lo crea.
     */
    public static ReentrantReadWriteLock obtenerCandado(String idSubasta) {
        return candados.computeIfAbsent(idSubasta, k -> new ReentrantReadWriteLock(true)); // 'true' asegura equidad (Fairness)
    }
}