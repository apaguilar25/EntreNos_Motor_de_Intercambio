import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { Image as ImageIcon } from 'lucide-react';

const Login = () => {
  const [isLogin, setIsLogin] = useState(true);
  
  // Login fields
  const [correoElectronico, setcorreoElectronico] = useState('');
  
  // Register fields
  const [nombre, setnombre] = useState('');
  const [telefono, settelefono] = useState('');
  const [descripcion, setdescripcion] = useState('');
  const [fotoPerfil, setfotoPerfil] = useState(null);

  const [error, setError] = useState('');

  const { setUser, setBalance, setHasCatalog } = useContext(AppContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validación común de dominio para ambos casos
    if (correoElectronico) {
      const domain = correoElectronico.split('@')[1];
      if (domain !== 'alameda.com') {
        setError('El correo debe pertenecer al dominio oficial de la comunidad (alameda.com).');
        return;
      }
    }

    if (isLogin) {
      if (!correoElectronico) {
        setError('Por favor, completa el campo de correo.');
        return;
      }
      
      const domain = correoElectronico.split('@')[1];
      if (domain !== 'alameda.com') {
        setError('El correo debe pertenecer al dominio oficial de la comunidad (alameda.com).');
        return;
      }
      const prefix = correoElectronico.split('@')[0].toLowerCase();
      let assignedId = 'USR-1001';
      if (prefix === 'carlos') assignedId = 'USR-1002';
      else if (prefix === 'luis') assignedId = 'USR-1003';
      
      setUser({ id: assignedId, name: prefix.charAt(0).toUpperCase() + prefix.slice(1), email: correoElectronico });
      
      try {
        const response = await fetch(`http://localhost:8080/api/usuarios/${assignedId}`);
        if (response.ok) {
           const data = await response.json();
           const hasCat = (data.habilidades && data.habilidades.length > 0) || (data.necesidades && data.necesidades.length > 0);
           setHasCatalog(hasCat);
           if (data.monedero) setBalance(data.monedero.creditosDisponibles);
           
           if (hasCat) {
              navigate('/');
           } else {
              navigate('/onboarding');
           }
        } else {
           navigate('/onboarding');
        }
      } catch (err) {
        navigate('/onboarding');
      }
    } else {
      // --- REGISTRO REAL CON JAVA ---
      if (!nombre || !correoElectronico || !telefono || !descripcion) {
        setError('Por favor, completa todos los campos obligatorios.');
        return;
      }

      const prefix = correoElectronico.split('@')[0].toLowerCase();
      let assignedId = 'USR-1001';
      if (prefix === 'carlos') assignedId = 'USR-1002';
      else if (prefix === 'luis') assignedId = 'USR-1003';

      // Registro simulado
      setUser({ id: assignedId, name, email: correoElectronico, phone: telefono, description: descripcion });
      
      try {
        const response = await fetch(`http://localhost:8080/api/usuarios/${assignedId}`);
        if (response.ok) {
           const data = await response.json();
           const hasCat = (data.habilidades && data.habilidades.length > 0) || (data.necesidades && data.necesidades.length > 0);
           setHasCatalog(hasCat);
           if (data.monedero) setBalance(data.monedero.creditosDisponibles);
           
           if (hasCat) {
              navigate('/');
           } else {
              navigate('/onboarding');
           }
        } else {
           navigate('/onboarding');
        }
      } catch (err) {
        navigate('/onboarding');
      }
    }
  };


  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      backgroundColor: 'var(--bg-primary)',
      padding: '1rem'
    }}>
      <div classnombre="card animate-in" style={{ width: '100%', maxWidth: '400px' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{ marginBottom: '0.5rem', color: 'var(--accent-primary)' }}>entreNos</h1>
          <h2 style={{ fontSize: '1.25rem', color: 'var(--text-secondary)' }}>
            {isLogin ? 'Inicia sesión en tu cuenta' : 'Crea tu cuenta'}
          </h2>
        </div>

        {error && (
          <div style={{ backgroundColor: 'var(--bg-warning-soft)', color: 'var(--text-on-warning-soft)', padding: '0.75rem', borderRadius: '0.5rem', marginBottom: '1rem', fontSize: '0.875rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {!isLogin && (
            <>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Nombre Completo</label>
                <input 
                  type="text" 
                  value={nombre}
                  onChange={(e) => setnombre(e.target.value)}
                  placeholder="Ej: María Pérez"
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Teléfono</label>
                <input 
                  type="tel" 
                  value={telefono}
                  onChange={(e) => settelefono(e.target.value)}
                  placeholder="Ej: +58 412 1234567"
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Descripción Personal</label>
                <textarea 
                  value={descripcion}
                  onChange={(e) => setdescripcion(e.target.value)}
                  placeholder="Cuenta un poco sobre ti..."
                  rows={2}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none', resize: 'vertical' }}
                />
              </div>
            </>
          )}

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Correo Comunitario</label>
            <input 
              type="correoElectronico" 
              value={correoElectronico}
              onChange={(e) => setcorreoElectronico(e.target.value)}
              placeholder="usuario@alameda.com"
              style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
            />
          </div>

          {!isLogin && (
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Foto de Perfil</label>
              <div style={{ border: '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '1rem', textAlign: 'center', color: 'var(--text-tertiary)', cursor: 'pointer' }}>
                <ImageIcon size={24} style={{ margin: '0 auto 0.5rem' }} />
                <p style={{ fontSize: '0.875rem' }}>Subir foto</p>
              </div>
            </div>
          )}

          <button type="submit" classnombre="btn-primary" style={{ marginTop: '1rem', padding: '0.75rem' }}>
            {isLogin ? 'Ingresar' : 'Registrarse'}
          </button>
        </form>

        <div style={{ marginTop: '2rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          {isLogin ? (
            <>
              ¿No tienes una cuenta?{' '}
              <button onClick={() => { setIsLogin(false); setError(''); }} style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', fontWeight: 'bold', cursor: 'pointer' }}>
                Regístrate aquí
              </button>
            </>
          ) : (
            <>
              ¿Ya tienes una cuenta?{' '}
              <button onClick={() => { setIsLogin(true); setError(''); }} style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', fontWeight: 'bold', cursor: 'pointer' }}>
                Inicia sesión
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Login;
