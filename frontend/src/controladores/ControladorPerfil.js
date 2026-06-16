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
        creditosDisponibles: usuario.creditosDisponibles !== undefined ? usuario.creditosDisponibles : 0, 
        creditosRetenidos: 0 // El DTO base no expone los retenidos actualmente
      };
    } catch (error) {
      return { creditosDisponibles: 0, creditosRetenidos: 0 };
    }
  }

  async obtenerSolicitudesEnviadas(idUsuario) {
    try {
      const publicaciones = await this.servicioPublicacion.obtenerPublicaciones();
      // Filtramos aquellas publicaciones donde el usuario actual es el solicitante
      return publicaciones.filter(pub => pub.idSolicitante === idUsuario);
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
