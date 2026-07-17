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
        sessionStorage.setItem('entreNosToken', response.token);
      }
      
      const usuario = response.usuario || response; // Depende del formato del DTO devuelto
      
      const userInfo = { 
        id: usuario.id, 
        name: usuario.nombre, 
        email: usuario.correoElectronico,
        rol: usuario.rol
      };
      
      this.setContextState('user', userInfo);

      const hasCat = Boolean(usuario.catalogoCompletado);
      
      this.setContextState('hasCatalog', hasCat);
      if (usuario.monedero) {
        this.setContextState('balance', usuario.monedero.saldoDisponible !== undefined ? usuario.monedero.saldoDisponible : usuario.monedero.creditosDisponibles);
      } else if (usuario.saldoDisponible !== undefined) {
        this.setContextState('balance', usuario.saldoDisponible);
      } else if (usuario.creditosDisponibles !== undefined) {
        this.setContextState('balance', usuario.creditosDisponibles);
      }
      
      return hasCat ? '/' : '/onboarding';
    } catch (err) {
      const txt = err.message || '';
      const idx = txt.indexOf(': ');
      if (idx !== -1) {
        try {
          const parsed = JSON.parse(txt.substring(idx + 2));
          if (parsed.error) throw new Error(parsed.error);
        } catch (e) {
          if (e instanceof SyntaxError) { /* not JSON, use generic */ }
          else throw e;
        }
      }
      throw new Error('Credenciales inválidas o error de conexión. Verifica tu correo y contraseña.');
    }
  }

  async registrarUsuario(datosUsuario) {
    const { nombre, correoElectronico, telefono, descripcionPersonal, contrasena } = datosUsuario;
    
    if (!nombre || !correoElectronico || !telefono || !descripcionPersonal || !contrasena) {
      throw new Error('Por favor, completa todos los campos obligatorios.');
    }

    try {
      const response = await this.servicioUsuario.registrarUsuario(datosUsuario);
      
      const usuario = response.usuario || response; // Depende de la envoltura
      
      const userInfo = { 
        id: usuario.id, 
        name: usuario.nombre, 
        email: usuario.correoElectronico,
        rol: usuario.rol
      };
      
      this.setContextState('user', userInfo);
      this.setContextState('hasCatalog', false);
      if (usuario.saldoDisponible !== undefined) {
        this.setContextState('balance', usuario.saldoDisponible);
      } else if (usuario.creditosDisponibles !== undefined) {
        this.setContextState('balance', usuario.creditosDisponibles);
      }
      
      // Intentamos también hacer login automático si el endpoint no nos da token,
      // pero por simplicidad de este flujo podemos asumir que el frontend
      // obligará a loguear si no hay token o pedirá iniciar sesión explícitamente,
      // pero asumiendo que lo manejamos, redirigimos a onboarding.
      return '/onboarding';
    } catch (err) {
      let msg = err.message || 'Error en el registro';
      if (msg.includes('Error POST')) {
         msg = msg.split(': ').slice(1).join(': ');
      }
      throw new Error(msg);
    }
  }
}
