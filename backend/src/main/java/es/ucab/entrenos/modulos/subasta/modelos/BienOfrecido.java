package es.ucab.entrenos.modulos.subasta.modelos;

public class BienOfrecido {
    private String nombre;
    private int cantidad;

    public BienOfrecido(String nombre, int cantidad) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del bien no puede estar vacío.");
        }
        // REGLA ERS: "cantidad debe ser un número entero mayor a cero"
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad para '" + nombre + "' debe ser mayor a cero.");
        }
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
}