package es.ucab.entrenos.modulos.utility;

import java.text.Normalizer;

public class UtilidadTexto {

    // Constructor privado para evitar que alguien instancie la clase con "new UtilidadTexto()"
    // Ya que todos sus métodos serán estáticos (globales).
    private UtilidadTexto() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Limpia el texto de espacios extra a los lados y en el medio.
     * Ej: "  Hola    Mundo  " -> "Hola Mundo"
     */
    public static String limpiarEspacios(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("\\s+", " ");
    }

    /**
     * Quita acentos y tildes de un texto.
     * Ej: "Carpintería" -> "Carpinteria"
     */
    public static String quitarAcentos(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "");
    }

    /**
     * Compara dos strings ignorando mayúsculas, minúsculas, acentos y espacios extra.
     */
    public static boolean sonIgualesEstrictos(String texto1, String texto2) {
        if (texto1 == null || texto2 == null) return false;

        String t1 = quitarAcentos(limpiarEspacios(texto1));
        String t2 = quitarAcentos(limpiarEspacios(texto2));

        return t1.equalsIgnoreCase(t2);
    }
}