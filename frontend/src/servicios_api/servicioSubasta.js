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

  async obtenerMisSubastas() {
    try {
      return await this.clienteHttp.get('/subastas/mis-subastas');
    } catch (error) {
      console.error('Error obteniendo mis subastas:', error);
      throw error;
    }
  }

  async crearSubasta(payload) {
    try {
      return await this.clienteHttp.post('/subastas', payload);
    } catch (error) {
      console.error('Error creando subasta:', error);
      throw error;
    }
  }

  async hacerOferta(idSubasta, propuesta) {
    try {
      return await this.clienteHttp.post(`/subastas/${idSubasta}/ofertar`, propuesta);
    } catch (error) {
      console.error('Error haciendo oferta en subasta:', error);
      throw error;
    }
  }

  async adjudicarGanador(idSubasta, idPropuesta) {
    try {
      return await this.clienteHttp.post(`/subastas/${idSubasta}/ganador/${idPropuesta}`);
    } catch (error) {
      console.error('Error adjudicando ganador:', error);
      throw error;
    }
  }
}
