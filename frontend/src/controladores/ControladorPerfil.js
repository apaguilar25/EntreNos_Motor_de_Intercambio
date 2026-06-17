export class ControladorPerfil {
  constructor(servicioUsuario, servicioPublicacion) {
    this.servicioUsuario = servicioUsuario;
    this.servicioPublicacion = servicioPublicacion;
  }

  async obtenerDatosPerfil(idUsuario) {
    try {
      return await this.servicioUsuario.obtenerUsuario(idUsuario);
    } catch (error) {
      // Retornar un mock base para que la interfaz de usuario no se rompa por falta de datos
      return {
        id: idUsuario,
        nombre: 'Usuario Desconocido',
        correoElectronico: '',
        reputacion: 0,
        habilidades: [],
        necesidades: []
      };
    }
  }

  async obtenerSaldo(idUsuario) {
    try {
      const usuario = await this.servicioUsuario.obtenerUsuario(idUsuario);
      return { 
        creditosDisponibles: usuario.saldoDisponible !== undefined ? usuario.saldoDisponible : (usuario.creditosDisponibles || 0), 
        creditosRetenidos: usuario.creditosComprometidos || 0 
      };
    } catch (error) {
      return { creditosDisponibles: 0, creditosRetenidos: 0 };
    }
  }

  async obtenerSolicitudesEnviadas(idUsuario) {
    try {
      return await this.servicioPublicacion.obtenerSolicitudesEnviadas(idUsuario);
    } catch (error) {
      return [];
    }
  }

  async actualizarCatalogo(idUsuario, catalogo) {
    try {
      return await this.servicioUsuario.actualizarCatalogo(idUsuario, catalogo);
    } catch (error) {
      console.error('Error actualizando catálogo:', error);
      throw error;
    }
  }
}
