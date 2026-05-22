package app.model.ModuloSubastas;

import java.util.Date;

public class Subasta {

    private String idSubasta;
    private String idSubatador;
    private String tituloSubasta;
    private String descripcionSubasta;
    private Date fechaCreacion;
    private Date fechaFinalizacion;
    private EstadoSubasta estadoSubasta;
    private boolean plazoResolucionExpirado;

    // Relaciones
    private ActivoFisico activoFisico;


}
