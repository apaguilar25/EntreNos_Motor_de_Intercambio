package app.model.CapaPersistencia;

import java.util.List;

public interface Persistencia {

    List<Object> cargar();
    Void guardar(List<Object> datosGuardar);

}
