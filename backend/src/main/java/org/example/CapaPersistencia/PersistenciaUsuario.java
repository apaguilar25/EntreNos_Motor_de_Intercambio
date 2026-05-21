package org.example.CapaPersistencia;

import java.util.List;

public class PersistenciaUsuario implements Persistencia {

    private static PersistenciaUsuario instancia;

    // Constructor privado
    private PersistenciaUsuario() {
        // Nada que agregar de momento
    }

    // Metod.o público y estático para instancia unica
    public static PersistenciaUsuario getInstancia() {
        if (instancia == null) {
            instancia = new PersistenciaUsuario(); // Solo si no existe, se crea
        }
        return instancia;
    }

    @Override
    public List<Object> cargar() {
        return List.of();
    }

    @Override
    public Void guardar(List<Object> datosGuardar) {
        return null;
    }




}