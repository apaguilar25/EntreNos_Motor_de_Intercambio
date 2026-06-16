export class ServicioUsuario {
  constructor(clienteHttp) {
    this.clienteHttp = clienteHttp;
  }

  async obtenerUsuario(idUsuario) {
    try {
      return await this.clienteHttp.get(`/usuarios/${idUsuario}`);
    } catch (error) {
      throw error;
    }
  }

  async login(correo, contrasena) {
    try {
      return await this.clienteHttp.post('/auth/login', { correoElectronico: correo, contrasena });
    } catch (error) {
      throw error;
    }
  }

  // Agrega aquí métodos como registrarUsuario, actualizarPerfil, etc.
  async actualizarCatalogo(idUsuario, catalogo) {
    try {
      return await this.clienteHttp.post(`/usuarios/${idUsuario}/catalogo`, catalogo);
    } catch (error) {
      throw error;
    }
  }
}
