export class ControladorSubasta {
  constructor(servicioSubasta) {
    this.servicioSubasta = servicioSubasta;
  }

  async obtenerSubastasActivas() {
    try {
      return await this.servicioSubasta.obtenerSubastasActivas();
    } catch (error) {
      console.error('Error en ControladorSubasta obteniendo subastas:', error);
      return []; // Devolvemos array vacío para no romper la UI
    }
  }
}
