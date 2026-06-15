export class ServicioUsuario {
  constructor(clienteHttp) {
    this.clienteHttp = clienteHttp;
  }

  async obtenerUsuario(idUsuario) {
    try {
      return await this.clienteHttp.get(`/usuarios/${idUsuario}`);
    } catch (error) {
      // No imprimimos error aquí porque un 404 es un escenario normal (usuario no registrado)
      throw error;
    }
  }

  // Agrega aquí métodos como registrarUsuario, actualizarPerfil, etc.
}
