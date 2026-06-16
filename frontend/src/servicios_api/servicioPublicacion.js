export class ServicioPublicacion {
  constructor(clienteHttp) {
    this.clienteHttp = clienteHttp;
  }

  async obtenerPublicaciones() {
    try {
      return await this.clienteHttp.get('/publicaciones');
    } catch (error) {
      console.error('Error obteniendo publicaciones:', error);
      throw error;
    }
  }

  async obtenerRecomendadas(idUsuario) {
    try {
      return await this.clienteHttp.get(`/publicaciones/recomendadas/${idUsuario}`);
    } catch (error) {
      console.error('Error obteniendo publicaciones recomendadas:', error);
      throw error;
    }
  }

  async responderSolicitud(idPublicacion, idUsuario, aceptar) {
    try {
      return await this.clienteHttp.post(`/publicaciones/${idPublicacion}/responder`, {
        idUsuario,
        aceptar
      });
    } catch (error) {
      console.error('Error respondiendo solicitud:', error);
      throw error;
    }
  }
}
