import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, Hammer, Trophy, Sparkles, Medal } from 'lucide-react';
import { AppContext } from '../App';
import Pagination from '../componentes/ui/Pagination';

const Auctions = () => {
  const navigate = useNavigate();
  const { user, controladorSubasta, controladorGamificacion } = useContext(AppContext);
  const [auctions, setAuctions] = useState([]);
  const [usersMap, setUsersMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [podio, setPodio] = useState(null);
  const [selectedTopInfo, setSelectedTopInfo] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    const fetchAuctions = async () => {
      try {
        const data = await controladorSubasta.obtenerSubastasActivas();
        // Solo mostrar las que no son mías y que estén ACTIVA
        const publicAuctions = data.filter(s => s.idPropietario !== user?.id && s.estado === 'ACTIVA');
        setAuctions(publicAuctions);

        // Construir mapa de nombres desde /api/publicaciones (ya incluye nombreUsuario)
        // Evita llamadas a /api/usuarios/{id} que generan 404 para usuarios eliminados
        const resPubs = await fetch('http://localhost:8080/api/publicaciones');
        if (resPubs.ok) {
          const pubsData = await resPubs.json();
          const uMap = {};
          pubsData.forEach(p => {
            if (p.idUsuario && p.nombreUsuario) uMap[p.idUsuario] = p.nombreUsuario;
          });
          setUsersMap(uMap);
        }
        
        if (controladorGamificacion) {
          const podioData = await controladorGamificacion.obtenerPodio();
          setPodio(podioData);
        }
      } catch (err) {
        console.error("Error al cargar subastas", err);
      } finally {
        setLoading(false);
      }
    };
    fetchAuctions();
  }, [user, controladorSubasta]);

  const getMejorOferta = (propuestas) => {
    if (!propuestas || propuestas.length === 0) return '-';
    let maxBienes = 0;
    propuestas.forEach(p => {
      const total = p.bienesOfrecidos?.reduce((sum, b) => sum + (b.cantidad || 0), 0) || 0;
      if (total > maxBienes) maxBienes = total;
    });
    return maxBienes > 0 ? `${maxBienes} Bienes` : '-';
  };

  const getUserTopInfo = (userId) => {
    if (!podio || Array.isArray(podio)) return null;
    if (podio.proveedorElite?.some(u => u.idUsuario === userId)) {
      return { 
        name: 'Proveedor Elite', 
        desc: 'Este ranking premia la proactividad y la capacidad de trabajo. Ser el #1 (o #2 o #3) en esta categoría le da al usuario un "Sello de Disponibilidad", lo que genera que más personas quieran contratarlo por su historial de cumplimiento.',
        icon: <Trophy size={14} color="#F59E0B" />
      };
    }
    if (podio.motorEconomia?.some(u => u.idUsuario === userId)) {
      return {
        name: 'Motor de la Economía',
        desc: 'Es vital para el sistema, ya que premia a quienes hacen circular los créditos. Este top incentiva a los usuarios a "gastar" su saldo en lugar de acumularlo, manteniendo la economía circular activa.',
        icon: <Sparkles size={14} color="#10B981" />
      };
    }
    if (podio.embajadorCalidad?.some(u => u.idUsuario === userId)) {
      return {
        name: 'Embajador de Calidad',
        desc: 'El "Embajador" asegura que el estándar de excelencia de la plataforma se mantenga alto, inspirando confianza en los nuevos usuarios.',
        icon: <Medal size={14} color="#3B82F6" />
      };
    }
    return null;
  };

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Motor de Subastas de Activos</h2>
        <button className="btn-primary" style={{ backgroundColor: 'var(--accent-warning)', color: 'var(--text-on-warning)' }} onClick={() => navigate('/create-auction')}>Crear Subasta</button>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {loading ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando subastas...</p>
        ) : auctions.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>No hay subastas disponibles en este momento.</p>
        ) : (
          <>
            <Pagination 
              currentPage={currentPage} 
              totalItems={auctions.length} 
              pageSize={5} 
              onPageChange={setCurrentPage} 
            />
            {auctions.slice((currentPage - 1) * 5, currentPage * 5).map((auction, index) => (
              <div key={auction.id} className="card interactive-card" style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', animationDelay: `${index * 0.1}s` }}>
                <div style={{ 
              width: '150px', 
              height: '150px', 
              backgroundColor: 'var(--bg-tertiary)', 
              borderRadius: '0.5rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-tertiary)',
              overflow: 'hidden'
            }}>
              {auction.imagenesUrls && auction.imagenesUrls.length > 0 && auction.imagenesUrls[0] ? (
                <img src={auction.imagenesUrls[0]} alt={auction.nombreActivo} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              ) : (
                'Sin imagen'
              )}
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
                <div style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span>Subastador:</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span
                      onClick={() => navigate(`/user/${auction.idPropietario}`)}
                      style={{ color: 'var(--accent-primary)', cursor: 'pointer', textDecoration: 'underline', textDecorationColor: 'transparent', transition: 'text-decoration-color 0.15s' }}
                      onMouseEnter={e => e.currentTarget.style.textDecorationColor = 'var(--accent-primary)'}
                      onMouseLeave={e => e.currentTarget.style.textDecorationColor = 'transparent'}
                    >
                      {usersMap[auction.idPropietario] || auction.idPropietario}
                    </span>
                    {getUserTopInfo(auction.idPropietario) && (
                      <div 
                        onClick={() => setSelectedTopInfo(getUserTopInfo(auction.idPropietario))}
                        style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', backgroundColor: 'var(--color-yellow-100)', color: 'var(--color-orange-600)', padding: '0.1rem 0.4rem', borderRadius: '1rem', fontSize: '0.65rem', fontWeight: 'bold', cursor: 'pointer' }}
                        title="Ver detalle del Top"
                      >
                        {getUserTopInfo(auction.idPropietario).icon}
                        Top {getUserTopInfo(auction.idPropietario).name.split(' ')[0]}
                      </div>
                    )}
                  </div>
                </div>
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
      </>
        )}
      </div>
      
      {/* Modal Información Top */}
      {selectedTopInfo && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999 }} onClick={() => setSelectedTopInfo(null)}>
          <div className="card animate-in" style={{ maxWidth: '400px', margin: '2rem', backgroundColor: 'var(--bg-primary)', position: 'relative' }} onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                {selectedTopInfo.icon}
              </div>
              <h3 style={{ margin: 0, fontSize: '1.25rem' }}>{selectedTopInfo.name}</h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '0.875rem' }}>
              {selectedTopInfo.desc}
            </p>
            <button className="btn-primary" style={{ width: '100%', marginTop: '1.5rem', padding: '0.75rem' }} onClick={() => setSelectedTopInfo(null)}>
              Entendido
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Auctions;
