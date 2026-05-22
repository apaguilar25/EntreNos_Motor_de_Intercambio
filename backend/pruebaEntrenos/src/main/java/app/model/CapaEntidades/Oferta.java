package app.model.CapaEntidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Oferta {
    private String idSubasta;
    private String idOfertante;
    private Date fechaDePublicacion;
    private Boolean esGanadora;
    private List<LineaBienConsumo> lineas = new ArrayList<>(); // Composición 1..*

    public Oferta() {}

    public Oferta(Date fechaDePublicacion) {
        this.fechaDePublicacion = fechaDePublicacion;
        this.esGanadora = false;
    }

    // Método del UML
    public void agregarLinea(BienConsumo bien, int cantidad) {
        LineaBienConsumo linea = new LineaBienConsumo(cantidad);
        linea.setBienConsumo(bien);
        this.lineas.add(linea);
    }

    // Getters y Setters
    public String getIdSubasta() { return idSubasta; }
    public void setIdSubasta(String idSubasta) { this.idSubasta = idSubasta; }

    public String getIdOfertante() { return idOfertante; }
    public void setIdOfertante(String idOfertante) { this.idOfertante = idOfertante; }

    public Date getFechaDePublicacion() { return fechaDePublicacion; }
    public void setFechaDePublicacion(Date fechaDePublicacion) { this.fechaDePublicacion = fechaDePublicacion; }

    public Boolean getEsGanadora() { return esGanadora; }
    public void setEsGanadora(Boolean esGanadora) { this.esGanadora = esGanadora; }

    public List<LineaBienConsumo> getLineas() { return lineas; }
    public void setLineas(List<LineaBienConsumo> lineas) { this.lineas = lineas; }
}