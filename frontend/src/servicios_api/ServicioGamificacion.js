export class ServicioGamificacion {
  constructor(clienteHttp) {
    this.clienteHttp = clienteHttp;
  }

  async obtenerLogros(idUsuario) {
    try {
      return await this.clienteHttp.get(`/logros/usuario/${idUsuario}/desbloqueados`);
    } catch (error) {
      console.error('Error obteniendo logros:', error);
      throw error;
    }
  }

  async obtenerPodio() {
    try {
      return await this.clienteHttp.get('/podio');
    } catch (error) {
      console.error('Error obteniendo podio:', error);
      throw error;
    }
  }
}
