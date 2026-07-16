import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, Hammer } from 'lucide-react';
import { AppContext } from '../App';

const Auctions = () => {
  const navigate = useNavigate();
  const { user, controladorSubasta, controladorPerfil } = useContext(AppContext);
  const [auctions, setAuctions] = useState([]);
  const [usersMap, setUsersMap] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchAuctions = async () => {
      try {
        const data = await controladorSubasta.obtenerSubastasActivas();
        // Solo mostrar las que no son mías y que estén ACTIVA
        const publicAuctions = data.filter(s => s.idPropietario !== user?.id && s.estado === 'ACTIVA');
        setAuctions(publicAuctions);

        const uniqueIds = [...new Set(publicAuctions.map(a => a.idPropietario))];
        const uMap = {};
        for (const id of uniqueIds) {
          if (id) {
            const p = await controladorPerfil.obtenerDatosPerfil(id);
            uMap[id] = p.nombre || id;
          }
        }
        setUsersMap(uMap);
      } catch (err) {
        console.error("Error al cargar subastas", err);
      } finally {
        setLoading(false);
      }
    };
    fetchAuctions();
  }, [user, controladorSubasta, controladorPerfil]);

  const getMejorOferta = (propuestas) => {
    if (!propuestas || propuestas.length === 0) return '-';
    let maxBienes = 0;
    propuestas.forEach(p => {
      const total = p.bienesOfrecidos?.reduce((sum, b) => sum + (b.cantidad || 0), 0) || 0;
      if (total > maxBienes) maxBienes = total;
    });
    return maxBienes > 0 ? `${maxBienes} Bienes` : '-';
  };

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Motor de Subastas de Activos</h2>
          <button className="btn-primary" style={{ backgroundColor: 'var(--accent-warning)', color: '#1f2937' }} onClick={() => navigate('/create-auction')}>Crear Subasta</button>      
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {loading ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando subastas...</p>
        ) : auctions.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>No hay subastas disponibles en este momento.</p>
        ) : auctions.map((auction, index) => (
          <div key={auction.id} className="card interactive-card" style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', animationDelay: `${index * 0.1}s` }}>
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
                    SUBASTA {auction.estadoFisico}
                </span>
                <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>{auction.nombreActivo}</h3>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '0.875rem' }}>{auction.descripcion}</p>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>Subastador: {usersMap[auction.idPropietario] || auction.idPropietario}</div>
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
                      <Hammer size={16} /> {getMejorOferta(auction.propuestas)}
                    </span>
                  </div>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' }}>
                  {auction.propuestas?.some(p => p.idPostor === user?.id) ? (
                    <button 
                      className="btn-primary" 
                      style={{ backgroundColor: 'var(--text-tertiary)', color: '#fff', padding: '0.5rem 1rem', fontSize: '0.875rem', cursor: 'not-allowed' }}
                      disabled
                    >
                      ¡Ya Pujaste!
                    </button>
                  ) : (
                    <button 
                      className="btn-primary" 
                      style={{ backgroundColor: 'var(--color-green-700)', color: '#fff', padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                      onClick={() => navigate(`/request/${auction.id}?type=subasta&title=${encodeURIComponent(auction.nombreActivo)}&desc=${encodeURIComponent(auction.descripcion)}&owner=${encodeURIComponent(usersMap[auction.idPropietario] || auction.idPropietario)}`)}
                    >
                      Pujar con Bienes
                    </button>
                  )}
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{auction.propuestas?.length || 0} pujas activas</span>
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
