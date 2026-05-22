import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, Hammer } from 'lucide-react';
import { AppContext } from '../App';

const Auctions = () => {
  const navigate = useNavigate();
  const { user } = useContext(AppContext);
  const [auctions, setAuctions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAuctions = async () => {
      try {
        const res = await fetch('http://localhost:8080/api/subastas');
        if (res.ok) {
          const data = await res.json();
          // Solo mostrar las que no son mías y que estén ACTIVA
          const publicAuctions = data.filter(s => s.idSubastador !== user?.id && s.estado === 'ACTIVA');
          setAuctions(publicAuctions);
        }
      } catch (err) {
        console.error("Error al cargar subastas", err);
      } finally {
        setLoading(false);
      }
    };
    fetchAuctions();
  }, [user]);

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Motor de Subastas de Activos</h2>
        <button className="btn-primary" style={{ backgroundColor: 'var(--accent-warning)', color: '#fff' }} onClick={() => navigate('/create-auction')}>Crear Subasta</button>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {loading ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando subastas...</p>
        ) : auctions.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>No hay subastas disponibles en este momento.</p>
        ) : auctions.map((auction, index) => (
          <div key={auction.idSubasta} className="card interactive-card" style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', animationDelay: `${index * 0.1}s` }}>
            <div style={{ 
              width: '150px', 
              height: '150px', 
              backgroundColor: 'var(--bg-tertiary)', 
              borderRadius: '0.5rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-tertiary)'
            }}>
              [Imagen Activo]
            </div>
            
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
                <span style={{ 
                    padding: '0.25rem 0.5rem', 
                    borderRadius: '1rem', 
                    fontSize: '0.75rem', 
                    fontWeight: 'bold', 
                    backgroundColor: 'var(--color-yellow-100)', 
                    color: 'var(--color-orange-600)',
                    marginBottom: '0.5rem',
                    display: 'inline-block'
                }}>
                    SUBASTA {auction.activoFisico?.estadoFisico}
                </span>
                <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>{auction.activoFisico?.nombreActivo}</h3>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '0.875rem' }}>{auction.descripcion}</p>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>Subastador: {auction.idSubastador}</div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '2rem' }}>
                  <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: 'bold' }}>Tiempo Restante</span>
                    <span style={{ color: 'var(--color-orange-600)', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <Clock size={16} /> 5 Días
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: 'bold' }}>Mejor Oferta (Bienes)</span>
                    <span style={{ color: 'var(--color-green-700)', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <Hammer size={16} /> -
                    </span>
                  </div>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' }}>
                  <button 
                    className="btn-primary" 
                    style={{ backgroundColor: 'var(--color-green-700)', color: '#fff', padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                    onClick={() => navigate(`/request/${auction.idSubasta}?type=subasta&title=${encodeURIComponent(auction.activoFisico?.nombreActivo)}&desc=${encodeURIComponent(auction.descripcion)}&owner=${auction.idSubastador}`)}
                  >
                    Pujar con Bienes
                  </button>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{auction.ofertas?.length || 0} pujas activas</span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Auctions;
