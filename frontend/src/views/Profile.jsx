import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { Star, ShieldCheck, XCircle } from 'lucide-react';

const Profile = () => {
  const { user } = useContext(AppContext);
  const navigate = useNavigate();
  
  const [bids, setBids] = useState([
    { id: 1, auctionTitle: 'Cámara Fotográfica', myOffer: '4 Paquetes de Harina Pan', status: 'Activa' }
  ]);

  const handleRetractBid = (id) => {
    if(window.confirm('¿Estás seguro que deseas retirar tu oferta de esta subasta?')) {
      setBids(bids.filter(b => b.id !== id));
      alert('Oferta retirada con éxito.');
    }
  };

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Mi Perfil y Catálogo</h2>
        <button 
          className="btn-primary" 
          onClick={() => navigate('/onboarding')}
        >
          Editar Catálogo
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
            <h3 style={{ fontSize: '1.25rem' }}>Mis Ofertas</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div 
              className="interactive-card" 
              style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}
              onClick={() => navigate('/post/1')}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ fontWeight: '600' }}>Asesoría Legal Básica</div>
                <div style={{ background: 'var(--accent-warning)', color: '#fff', padding: '0.15rem 0.5rem', borderRadius: '1rem', fontSize: '0.7rem', fontWeight: 'bold' }}>1 Solicitud</div>
              </div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Revisión de contratos y documentos.</div>
              <div style={{ color: 'var(--accent-primary)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>40 cr</div>
            </div>
            <div 
              className="interactive-card" 
              style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}
              onClick={() => navigate('/post/2')}
            >
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
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div 
              className="interactive-card" 
              style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}
              onClick={() => navigate('/post/3')}
            >
              <div style={{ fontWeight: '600' }}>Mantenimiento de Aire Acondicionado</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Limpieza de filtros y revisión de gas.</div>
              <div style={{ color: 'var(--color-orange-600)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>Hasta 60 cr</div>
            </div>
            <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
              No hay más necesidades registradas.
            </div>
          </div>
        </div>

        {/* Catálogo de Subastas */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Subastas</h3>
            <button className="btn-primary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem', backgroundColor: 'var(--accent-warning)', color: '#fff' }} onClick={() => navigate('/create-auction')}>Añadir</button>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div 
              className="interactive-card" 
              style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}
              onClick={() => navigate('/post/4')}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ fontWeight: '600' }}>Bicicleta Montañera</div>
                <div style={{ background: 'var(--accent-warning)', color: '#fff', padding: '0.15rem 0.5rem', borderRadius: '1rem', fontSize: '0.7rem', fontWeight: 'bold' }}>3 Pujas</div>
              </div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Cambio por alimentos no perecederos.</div>
              <div style={{ color: 'var(--accent-primary)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>Mejor: 2 Harinas + 1 Arroz</div>
            </div>
          </div>
        </div>

        {/* Historial de Ofertas Enviadas (HU5) */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Ofertas Enviadas</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {bids.length === 0 ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No tienes ofertas activas.
              </div>
            ) : (
              bids.map(bid => (
                <div key={bid.id} style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}>
                  <div style={{ fontWeight: '600', marginBottom: '0.25rem' }}>Subasta: {bid.auctionTitle}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Mi oferta: {bid.myOffer}</div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.5rem' }}>
                    <span style={{ fontSize: '0.75rem', backgroundColor: 'var(--color-green-100)', color: 'var(--color-green-700)', padding: '0.15rem 0.5rem', borderRadius: '1rem', fontWeight: 'bold' }}>
                      {bid.status}
                    </span>
                    <button 
                      style={{ background: 'transparent', border: 'none', color: 'var(--color-red-600)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.75rem', fontWeight: 'bold' }}
                      onClick={() => handleRetractBid(bid.id)}
                    >
                      <XCircle size={14} /> Retirar
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
