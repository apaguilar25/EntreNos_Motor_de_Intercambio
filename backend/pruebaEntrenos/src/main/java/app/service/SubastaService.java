package app.service;
import org.springframework.stereotype.Service;

@Service
public class SubastaService {

    public void crearSubasta(String idUsuario, String nombreActivo, String estadoFisico, boolean tieneImagen, boolean tieneSancionActiva) {
        // 1. Validar sanción activa (Restricción HU4)
        if (tieneSancionActiva) {
            throw new IllegalStateException("No puedes crear subastas. Tienes una sanción activa de 72 horas.");
        }

        // 2. Validar campos obligatorios
        if (nombreActivo.isEmpty() || !tieneImagen) {
            throw new IllegalArgumentException("Nombre y fotografía son obligatorios.");
        }

        // 3. Validar estado físico estandarizado
        if (!estadoFisico.equals("Nuevo") && !estadoFisico.equals("Usado") && !estadoFisico.equals("Reparado")) {
            throw new IllegalArgumentException("El estado físico debe ser Nuevo, Usado o Reparado.");
        }

        System.out.println("Subasta publicada con éxito.");
    }

    public void registrarOfertaConsumo(String idPostor, String[] bienes, int[] cantidades) {
        // 1. Validar que la oferta tenga al menos un producto (Restricción HU5)
        if (bienes.length == 0) {
            throw new IllegalArgumentException("Debe ofrecer al menos un producto.");
        }

        // 2. Validar que las cantidades sean mayores a cero
        for (int cantidad : cantidades) {
            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a cero.");
            }
        }

        System.out.println("Propuesta de trueque registrada exitosamente.");
    }

    public void auditarVencimientoSubastas(int diasSinRespuesta) {
        // Esta función simula la revisión de tiempo (Regla de Negocio HU4)
        if (diasSinRespuesta >= 5) {
            System.out.println("Subasta cancelada. Sanción de 3 días aplicada al subastador por inactividad.");
        }
    }
}
