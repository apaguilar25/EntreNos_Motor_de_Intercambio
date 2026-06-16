export class ControladorNotificacion {
  constructor(servicioNotificacion, servicioPublicacion, servicioSubasta) {
    this.servicioNotificacion = servicioNotificacion;
    this.servicioPublicacion = servicioPublicacion;
    this.servicioSubasta = servicioSubasta;
  }

  async obtenerNotificaciones(idDestinatario) {
    try {
      return await this.servicioNotificacion.obtenerNotificaciones(idDestinatario);
    } catch (error) {
      console.error('Error obteniendo notificaciones:', error);
      return [];
    }
  }

  async eliminarNotificacion(idNotificacion) {
    try {
      await this.servicioNotificacion.eliminarNotificacion(idNotificacion);
      return true;
    } catch (error) {
      console.error('Error eliminando notificacion:', error);
      return false;
    }
  }

  async responderSolicitud(idPublicacion, idUsuario, aceptar) {
    try {
      return await this.servicioPublicacion.responderSolicitud(idPublicacion, idUsuario, aceptar);
    } catch (error) {
      console.error('Error respondiendo solicitud:', error);
      throw error;
    }
  }
}
