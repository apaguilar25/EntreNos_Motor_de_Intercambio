package app.service;
import org.springframework.stereotype.Service;

@Service
public class TransaccionService {

    public void iniciarTransaccion(String idDemandante, String idOfertante, double costoServicio) {
        // 1. Validar saldo suficiente (Criterio de Aceptación HU2)
        double saldoDemandante = 150.0; // Esto vendría de leer tu JSON
        if (saldoDemandante < costoServicio) {
            throw new IllegalStateException("Saldo insuficiente para solicitar este servicio.");
        }

        // 2. Retener fondos (Regla de Negocio HU3)
        double nuevoSaldoDisponible = saldoDemandante - costoServicio;
        double fondosRetenidos = costoServicio;

        System.out.println("Transacción iniciada. Fondos retenidos: " + fondosRetenidos);
    }

    public void finalizarTransaccion(String idOfertante, double fondosRetenidos, int calificacionEstrellas) {
        // 1. Validar rango de estrellas [1, 5] (Restricción HU3)
        if (calificacionEstrellas < 1 || calificacionEstrellas > 5) {
            throw new IllegalArgumentException("La calificación debe ser entre 1 y 5 estrellas.");
        }

        // 2. Liberar fondos al ofertante
        double saldoOfertante = 0.0; // Saldo actual del ofertante
        saldoOfertante += fondosRetenidos;

        // 3. Actualizar promedio de reputación
        System.out.println("Servicio completado. Saldo transferido y calificado con " + calificacionEstrellas + " estrellas.");
    }
}