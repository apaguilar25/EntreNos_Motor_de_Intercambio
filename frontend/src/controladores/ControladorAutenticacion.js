export class ControladorAutenticacion {
  constructor(servicioUsuario, setContextState) {
    this.servicioUsuario = servicioUsuario;
    this.setContextState = setContextState; // Callback para actualizar el estado global de React
  }

  async iniciarSesion(correoElectronico) {
    if (!correoElectronico) {
      throw new Error('Por favor, completa el campo de correo.');
    }

    const domain = correoElectronico.split('@')[1];
    if (domain !== 'alameda.com') {
      throw new Error('El correo debe pertenecer al dominio oficial de la comunidad (alameda.com).');
    }

    const prefix = correoElectronico.split('@')[0].toLowerCase();
    let assignedId = 'USR-1001';
    if (prefix === 'carlos') assignedId = 'USR-1002';
    else if (prefix === 'luis') assignedId = 'USR-1003';

    // Setear información inicial
    const userInfo = { id: assignedId, name: prefix.charAt(0).toUpperCase() + prefix.slice(1), email: correoElectronico };
    this.setContextState('user', userInfo);

    try {
      const data = await this.servicioUsuario.obtenerUsuario(assignedId);
      const hasCat = (data.habilidades && data.habilidades.length > 0) || (data.necesidades && data.necesidades.length > 0);
      
      this.setContextState('hasCatalog', hasCat);
      if (data.monedero) {
        this.setContextState('balance', data.monedero.creditosDisponibles);
      }
      
      return hasCat ? '/' : '/onboarding';
    } catch (err) {
      return '/onboarding';
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
      const hasCat = (data.habilidades && data.habilidades.length > 0) || (data.necesidades && data.necesidades.length > 0);
      
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
