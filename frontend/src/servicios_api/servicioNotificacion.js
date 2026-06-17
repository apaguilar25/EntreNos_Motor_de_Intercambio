export class ServicioNotificacion {
  constructor(clienteHttp) {
    this.clienteHttp = clienteHttp;
  }

  async obtenerNotificaciones(idDestinatario) {
    try {
      return await this.clienteHttp.get(`/notificaciones/${idDestinatario}`);
    } catch (error) {
      console.error('Error obteniendo notificaciones:', error);
      throw error;
    }
  }

  async eliminarNotificacion(idNotificacion) {
    try {
      return await this.clienteHttp.delete(`/notificaciones/${idNotificacion}`);
    } catch (error) {
      console.error('Error eliminando notificacion:', error);
      throw error;
    }
  }
}
