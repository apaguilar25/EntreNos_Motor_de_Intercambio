package org.example.ModuloSubastas;

import java.util.ArrayList;
import java.util.Date;

public class Oferta {

    private String idOferta;
    private String idOfertante;
    private Date fechaInicio;
    private EstadoOferta estadoOferta;
    private boolean esGanadora;

    // Relaciones
    private ArrayList<LineaBienConsumo> lineaBienConsumo;

}
