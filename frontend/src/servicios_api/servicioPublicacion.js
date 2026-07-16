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

  async solicitar(idPublicacion, idUsuario, precioOfertado) {
    try {
      const body = { idUsuario };
      if (precioOfertado !== undefined && precioOfertado !== null) {
        body.precioOfertado = precioOfertado;
      }
      return await this.clienteHttp.post(`/publicaciones/${idPublicacion}/solicitar`, body);
    } catch (error) {
      console.error('Error enviando solicitud:', error);
      throw error;
    }
  }

  async responderSolicitud(idSolicitud, idUsuario, aceptar) {
    try {
      return await this.clienteHttp.post(`/solicitudes/${idSolicitud}/responder`, {
        idUsuario,
        aceptar
      });
    } catch (error) {
      console.error('Error respondiendo solicitud:', error);
      throw error;
    }
  }

  async obtenerSolicitudesEnviadas(idUsuario) {
    try {
      return await this.clienteHttp.get(`/solicitudes/usuario/${idUsuario}`);
    } catch (error) {
      console.error('Error obteniendo solicitudes enviadas:', error);
      throw error;
    }
  }

  async cancelarSolicitud(idSolicitud, idUsuario) {
    try {
      return await this.clienteHttp.post(`/solicitudes/${idSolicitud}/cancelar`, {
        idUsuario
      });
    } catch (error) {
      console.error('Error cancelando solicitud:', error);
      throw error;
    }
  }

  async confirmarOfertante(idTransaccion) {
    return await this.clienteHttp.post(`/transacciones/${idTransaccion}/confirmar-ofertante`);
  }

  async confirmarDemandante(idTransaccion) {
    return await this.clienteHttp.post(`/transacciones/${idTransaccion}/confirmar-demandante`);
  }
}
