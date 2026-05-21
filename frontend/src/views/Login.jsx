import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { setUser, setBalance } = useContext(AppContext);
  const navigate = useNavigate();

  const handleLogin = (e) => {
    e.preventDefault();
    // Requisito: Restricción de Dominio (Seguridad) -> dominio oficial de la comunidad
    const emailRegex = /^[a-zA-Z0-9._%+-]+@plazaalameda\.com$/;
    
    if (!emailRegex.test(email)) {
      setError('El correo debe pertenecer al dominio oficial @plazaalameda.com');
      return;
    }

    if (password.length < 6) {
      setError('Contraseña inválida.');
      return;
    }

    // Mock successful login
    setUser({ name: email.split('@')[0], email, role: 'Vecino' });
    setBalance(100); // Capital Semilla
    navigate('/');
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
      <div className="card animate-in" style={{ width: '100%', maxWidth: '400px' }}>
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h1 style={{ color: 'var(--accent-primary)', marginBottom: '0.5rem' }}>entreNos</h1>
          <p style={{ color: 'var(--text-tertiary)' }}>Bienvenido a Plaza Alameda</p>
        </div>

        {error && (
          <div style={{
            backgroundColor: '#fee2e2',
            color: '#dc2626',
            padding: '0.75rem',
            borderRadius: '0.5rem',
            marginBottom: '1.5rem',
            fontSize: '0.875rem'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Correo Electrónico</label>
            <input 
              type="email" 
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="vecino@plazaalameda.com"
              style={{
                width: '100%',
                padding: '0.75rem',
                borderRadius: '0.5rem',
                border: '1px solid var(--border-color)',
                backgroundColor: 'var(--bg-primary)',
                color: 'var(--text-primary)',
                outline: 'none'
              }}
              required
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Contraseña</label>
            <input 
              type="password" 
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="••••••••"
              style={{
                width: '100%',
                padding: '0.75rem',
                borderRadius: '0.5rem',
                border: '1px solid var(--border-color)',
                backgroundColor: 'var(--bg-primary)',
                color: 'var(--text-primary)',
                outline: 'none'
              }}
              required
            />
          </div>
          <button type="submit" className="btn-primary" style={{ marginTop: '1rem', width: '100%' }}>
            Ingresar
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;
