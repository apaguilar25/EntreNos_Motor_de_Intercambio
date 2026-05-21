import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { Star, Settings, ShieldCheck } from 'lucide-react';

const Profile = () => {
  const { user } = useContext(AppContext);
  const navigate = useNavigate();

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Mi Perfil y Catálogo</h2>
        <button 
          className="btn-primary" 
          style={{ backgroundColor: 'transparent', color: 'var(--text-secondary)', border: '1px solid var(--border-color)' }}
          onClick={() => navigate('/settings/password')}
        >
          <Settings size={18} />
        </button>
      </div>

      {/* Tarjeta de Identidad */}
      <div className="card" style={{ display: 'flex', gap: '2rem', alignItems: 'center', marginBottom: '2rem' }}>
        <div style={{ 
          width: '100px', 
          height: '100px', 
          borderRadius: '50%', 
          backgroundColor: 'var(--accent-primary)',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '2.5rem',
          fontWeight: 'bold'
        }}>
          {user?.name?.charAt(0).toUpperCase() || 'U'}
        </div>
        
        <div style={{ flex: 1 }}>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            {user?.name || 'Usuario'}
            <ShieldCheck size={20} color="var(--accent-primary)" />
          </h3>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>{user?.email || 'correo@plazaalameda.com'}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontWeight: 'bold' }}>
              <Star size={16} fill="currentColor" /> 5.0
            </span>
            <span style={{ color: 'var(--text-tertiary)', fontSize: '0.875rem' }}>12 transacciones exitosas</span>
          </div>
        </div>
      </div>

      <div className="responsive-grid">
        {/* Catálogo de Ofertas */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Ofertas (Talento)</h3>
            <button className="btn-primary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }} onClick={() => navigate('/publish?type=oferta')}>Añadir Oferta</button>
          </div>
          <div className="card" style={{ padding: '1rem' }}>
            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem', marginBottom: '0.75rem' }}>
              <div style={{ fontWeight: '600' }}>Asesoría Legal Básica</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Revisión de contratos y documentos.</div>
              <div style={{ color: 'var(--accent-primary)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>40 cr</div>
            </div>
            <div>
              <div style={{ fontWeight: '600' }}>Traducción de Textos</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Inglés a Español (por página).</div>
              <div style={{ color: 'var(--accent-primary)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>10 cr</div>
            </div>
          </div>
        </div>

        {/* Catálogo de Necesidades */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Necesidades</h3>
            <button className="btn-primary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem', backgroundColor: 'var(--color-yellow-100)', color: 'var(--color-orange-600)' }} onClick={() => navigate('/publish?type=demanda')}>Añadir Necesidad</button>
          </div>
          <div className="card" style={{ padding: '1rem' }}>
            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem', marginBottom: '0.75rem' }}>
              <div style={{ fontWeight: '600' }}>Mantenimiento de Aire Acondicionado</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Limpieza de filtros y revisión de gas.</div>
              <div style={{ color: 'var(--accent-warning)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>Presupuesto: Hasta 60 cr</div>
            </div>
            <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
              No hay más necesidades registradas.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
