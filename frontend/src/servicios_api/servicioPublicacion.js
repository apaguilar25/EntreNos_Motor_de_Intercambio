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
}
