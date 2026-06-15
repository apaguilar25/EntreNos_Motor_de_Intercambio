export class ControladorAutenticacion {
  constructor(servicioUsuario, setContextState) {
    this.servicioUsuario = servicioUsuario;
    this.setContextState = setContextState; // Callback para actualizar el estado global de React
  }

  async iniciarSesion(correoElectronico, contrasena) {
    if (!correoElectronico || !contrasena) {
      throw new Error('Por favor, completa correo y contraseña.');
    }

    try {
      const response = await this.servicioUsuario.login(correoElectronico, contrasena);
      // Asumiendo que el backend devuelve un JWT y datos del usuario
      if (response.token) {
        localStorage.setItem('entreNosToken', response.token);
      }
      
      const usuario = response.usuario || response; // Depende del formato del DTO devuelto
      
      const userInfo = { 
        id: usuario.id, 
        name: usuario.nombre, 
        email: usuario.correoElectronico 
      };
      
      this.setContextState('user', userInfo);

      const hasCat = Boolean(usuario.catalogo && ((usuario.catalogo.habilidadesOfrecidas && usuario.catalogo.habilidadesOfrecidas.length > 0) || (usuario.catalogo.necesidadesRegistradas && usuario.catalogo.necesidadesRegistradas.length > 0)));
      
      this.setContextState('hasCatalog', hasCat);
      if (usuario.monedero) {
        this.setContextState('balance', usuario.monedero.creditosDisponibles);
      }
      
      return hasCat ? '/' : '/onboarding';
    } catch (err) {
      throw new Error('Credenciales inválidas o error de conexión. Verifica tu correo y contraseña.');
    }
  }

  async registrarUsuario(datosUsuario) {
    const { nombre, correoElectronico, telefono, descripcionPersonal } = datosUsuario;
    
    if (!nombre || !correoElectronico || !telefono || !descripcionPersonal) {
      throw new Error('Por favor, completa todos los campos obligatorios.');
    }

    const domain = correoElectronico.split('@')[1];
    if (domain !== 'alameda.com') {
      throw new Error('El correo debe pertenecer al dominio oficial de la comunidad (alameda.com).');
    }

    const prefix = correoElectronico.split('@')[0].toLowerCase();
    let assignedId = 'USR-1001';
    if (prefix === 'carlos') assignedId = 'USR-1002';
    else if (prefix === 'luis') assignedId = 'USR-1003';

    // Mock registro
    const userInfo = { id: assignedId, nombre, correoElectronico, telefono, descripcionPersonal };
    this.setContextState('user', userInfo);

    try {
      const data = await this.servicioUsuario.obtenerUsuario(assignedId);
      const hasCat = Boolean((data.habilidades && data.habilidades.length > 0) || (data.necesidades && data.necesidades.length > 0));
      
      this.setContextState('hasCatalog', hasCat);
      if (data.monedero) {
        this.setContextState('balance', data.monedero.creditosDisponibles);
      }
      
      return hasCat ? '/' : '/onboarding';
    } catch (err) {
      return '/onboarding';
    }
  }
}
