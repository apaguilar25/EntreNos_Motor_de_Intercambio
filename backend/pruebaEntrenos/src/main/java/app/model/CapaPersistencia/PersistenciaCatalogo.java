package app.model.CapaPersistencia;

import java.util.List;

public class PersistenciaCatalogo implements Persistencia{

    private static PersistenciaCatalogo instancia;


    private PersistenciaCatalogo() {
    }

    public static PersistenciaCatalogo getInstancia(){
        if (instancia == null) {
            instancia = new PersistenciaCatalogo(); // Solo si no existe, se crea
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
