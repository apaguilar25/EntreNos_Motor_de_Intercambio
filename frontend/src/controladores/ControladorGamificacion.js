export class ControladorGamificacion {
  constructor(servicioGamificacion) {
    this.servicioGamificacion = servicioGamificacion;
  }

  async obtenerLogros(idUsuario) {
    try {
      return await this.servicioGamificacion.obtenerLogros(idUsuario);
    } catch (error) {
      console.error('Error en ControladorGamificacion obteniendo logros:', error);
      return [];
    }
  }

  async obtenerPodio() {
    try {
      return await this.servicioGamificacion.obtenerPodio();
    } catch (error) {
      console.error('Error en ControladorGamificacion obteniendo podio:', error);
      return [];
    }
  }
}
