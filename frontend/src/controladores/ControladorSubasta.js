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

  async obtenerMisSubastas() {
    try {
      return await this.servicioSubasta.obtenerMisSubastas();
    } catch (error) {
      console.error('Error en ControladorSubasta obteniendo mis subastas:', error);
      return [];
    }
  }

  async crearSubasta(payload) {
    try {
      return await this.servicioSubasta.crearSubasta(payload);
    } catch (error) {
      console.error('Error en ControladorSubasta creando subasta:', error);
      throw error;
    }
  }

  async hacerOferta(idSubasta, propuesta) {
    try {
      return await this.servicioSubasta.hacerOferta(idSubasta, propuesta);
    } catch (error) {
      console.error('Error en ControladorSubasta haciendo oferta:', error);
      throw error;
    }
  }

  async adjudicarGanador(idSubasta, idPropuesta) {
    try {
      return await this.servicioSubasta.adjudicarGanador(idSubasta, idPropuesta);
    } catch (error) {
      console.error('Error en ControladorSubasta adjudicando ganador:', error);
      throw error;
    }
  }

  async retirarPropuesta(idSubasta, idPropuesta) {
    try {
      return await this.servicioSubasta.retirarPropuesta(idSubasta, idPropuesta);
    } catch (error) {
      console.error('Error en ControladorSubasta retirando propuesta:', error);
      throw error;
    }
  }

  async obtenerHistorialPropuestas() {
    try {
      return await this.servicioSubasta.obtenerHistorialPropuestas();
    } catch (error) {
      console.error('Error en ControladorSubasta obteniendo historial:', error);
      return [];
    }
  }
}
