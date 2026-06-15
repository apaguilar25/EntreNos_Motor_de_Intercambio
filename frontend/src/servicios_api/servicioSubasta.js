export class ServicioSubasta {
  constructor(clienteHttp) {
    this.clienteHttp = clienteHttp;
  }

  async obtenerSubastasActivas() {
    try {
      return await this.clienteHttp.get('/subastas/activas');
    } catch (error) {
      console.error('Error obteniendo subastas activas:', error);
      throw error;
    }
  }
}
