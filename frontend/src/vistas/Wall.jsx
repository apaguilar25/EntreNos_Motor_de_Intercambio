import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Star, ChevronDown, Trophy, Sparkles, Medal, AlertCircle } from 'lucide-react';
import { AppContext } from '../App';

const Wall = () => {
  const navigate = useNavigate();
  const { user, balance, hasCatalog, controladorMuro, controladorGamificacion, controladorPerfil } = useContext(AppContext);
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [tab, setTab] = useState('explorar'); // 'explorar' o 'parati'
  const [filterType, setFilterType] = useState('all'); // 'all', 'oferta', 'necesidad'
  const [posts, setPosts] = useState([]);
  const [podio, setPodio] = useState([]);
  const [showPodio, setShowPodio] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sentRequests, setSentRequests] = useState(new Set());

  const fetchPosts = React.useCallback(async () => {
    try {
      setLoading(true);
      let mappedPosts = [];
      
      if (tab === 'explorar') {
        const data = await controladorMuro.obtenerPublicaciones(searchTerm);
        mappedPosts = data.map((item, index) => ({
          id: item.idPublicacion || index,
          type: item.tipoPublicacion ? item.tipoPublicacion.toLowerCase() : 'oferta',
          user: item.nombreUsuario || 'Usuario Desconocido',
          userId: item.idUsuario,
          reputation: item.reputacionUsuario || 0,
          title: item.nombreServicio || 'Sin título',
          description: item.descripcion || '',
          price: item.precioCreditos || 0,
          timeAgo: 'reciente'
        }));
      } else if (tab === 'parati') {
        const userId = user?.id || 'USR-1001';
        const data = await controladorMuro.obtenerRecomendadas(userId);
        
        mappedPosts = data.map((item, index) => {
          const pub = item.publicacion;
          return {
            id: pub.idPublicacion || index,
            type: pub.tipoPublicacion ? pub.tipoPublicacion.toLowerCase() : 'oferta',
            user: pub.nombreUsuario || 'Usuario Desconocido',
            userId: pub.idUsuario,
            reputation: pub.reputacionUsuario || 0,
            title: pub.nombreServicio || 'Sin título',
            description: pub.descripcion || '',
            price: pub.precioCreditos || 0,
            timeAgo: 'reciente',
            isMatch: true,
            matchReason: item.tipoCoincidencia
          };
        });
      }
      
      mappedPosts = mappedPosts.filter(post => post.userId !== (user?.id || 'USR-1001'));
      setPosts(mappedPosts);
      
      if (controladorGamificacion) {
        const podioData = await controladorGamificacion.obtenerPodio();
        let podiumList = [];
        if (podioData && !Array.isArray(podioData)) {
          podiumList = podioData.proveedorElite || [];
        } else if (Array.isArray(podioData)) {
          podiumList = podioData;
        }
        setPodio(podiumList.slice(0, 3));
      }

      if (controladorPerfil && user) {
        const sols = await controladorPerfil.obtenerSolicitudesEnviadas(user.id);
        setSentRequests(new Set(sols.map(s => s.idPublicacion)));
      }

      setError(null);
    } catch (err) {
      console.error(err);
      setError('No se pudo conectar con el servidor.');
    } finally {
      setLoading(false);
    }
  }, [searchTerm, controladorMuro, user, tab, controladorGamificacion, controladorPerfil]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  // SSE: suscripción a cambios del muro en tiempo real
  useEffect(() => {
    const eventSource = new EventSource('http://localhost:8080/api/publicaciones/stream');

    eventSource.addEventListener('actualizar', () => {
      fetchPosts();
    });

    eventSource.onerror = () => {
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, [fetchPosts]);

  // Refresh al volver a la pestaña (por si hubo cambios mientras no se recibía SSE)
  useEffect(() => {
    const onVisible = () => {
      if (document.visibilityState === 'visible') fetchPosts();
    };
    document.addEventListener('visibilitychange', onVisible);
    return () => document.removeEventListener('visibilitychange', onVisible);
  }, [fetchPosts]);

  const filteredPosts = posts.filter(post => {
    if (filterType === 'all') return true;
    const isOferta = post.type === 'oferta' || post.type === 'habilidad';
    if (filterType === 'oferta') return isOferta;
    if (filterType === 'necesidad') return post.type === 'necesidad' || post.type === 'demanda';
    return true;
  });

  return (
    <div className="animate-in">
      {!hasCatalog && (
        <div className="card" style={{ backgroundColor: 'var(--color-yellow-100)', borderColor: 'var(--color-orange-600)', marginBottom: '2rem', display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
          <AlertCircle color="var(--color-orange-600)" />
          <div style={{ flex: 1 }}>
            <h3 style={{ color: 'var(--color-orange-600)', marginBottom: '0.25rem' }}>¡Completa tu catálogo y recibe Capital Semilla!</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '0.5rem' }}>
              Aún no has configurado tus habilidades y necesidades. Hazlo ahora para recibir tus primeros 20 créditos y empezar a intercambiar con la comunidad.
            </p>
            <button 
              className="btn-primary" 
              style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }}
              onClick={() => navigate('/onboarding')}
            >
              Completar Catálogo
            </button>
          </div>
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2>Muro de Publicaciones</h2>
      </div>
      
      <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', marginBottom: '2rem', gap: '2rem' }}>
        <button 
          onClick={() => setTab('explorar')}
          style={{ 
            background: 'none', border: 'none', padding: '0.75rem 0', 
            fontSize: '1rem', fontWeight: tab === 'explorar' ? 'bold' : 'normal',
            color: tab === 'explorar' ? 'var(--accent-primary)' : 'var(--text-secondary)',
            borderBottom: tab === 'explorar' ? '2px solid var(--accent-primary)' : '2px solid transparent',
            cursor: 'pointer'
          }}
        >
          Explorar
        </button>
        <button 
          onClick={() => setTab('parati')}
          style={{ 
            background: 'none', border: 'none', padding: '0.75rem 0', 
            fontSize: '1rem', fontWeight: tab === 'parati' ? 'bold' : 'normal',
            color: tab === 'parati' ? 'var(--color-purple-600)' : 'var(--text-secondary)',
            borderBottom: tab === 'parati' ? '2px solid var(--color-purple-600)' : '2px solid transparent',
            cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.5rem'
          }}
        >
          <Sparkles size={16} /> Para ti
        </button>
      </div>

      <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
        <div style={{ 
          flex: 1, 
          display: 'flex', 
          alignItems: 'center', 
          background: 'var(--bg-secondary)', 
          padding: '0.5rem 1rem', 
          borderRadius: '0.5rem',
          border: '1px solid var(--border-color)'
        }}>
          <Search size={20} color="var(--text-tertiary)" style={{ marginRight: '0.5rem' }} />
          <input 
            type="text" 
            placeholder="Buscar servicios o necesidades..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ border: 'none', background: 'transparent', outline: 'none', width: '100%', color: 'var(--text-primary)' }}
          />
        </div>
        <div style={{ position: 'relative' }}>
          <button 
            className="card interactive-card" 
            style={{ padding: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}
            onClick={() => setShowFilters(!showFilters)}
          >
            <Filter size={20} />
            <span>Filtros</span>
            <ChevronDown size={16} />
          </button>

          {showFilters && (
            <div className="card animate-in" style={{ 
              position: 'absolute', 
              top: '110%', 
              right: 0, 
              width: '200px', 
              padding: '1rem', 
              zIndex: 10,
              display: 'flex',
              flexDirection: 'column',
              gap: '0.5rem'
            }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input type="radio" name="filterType" value="all" checked={filterType === 'all'} onChange={(e) => setFilterType(e.target.value)} /> Todas
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input type="radio" name="filterType" value="oferta" checked={filterType === 'oferta'} onChange={(e) => setFilterType(e.target.value)} /> Ofertas
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input type="radio" name="filterType" value="necesidad" checked={filterType === 'necesidad'} onChange={(e) => setFilterType(e.target.value)} /> Necesidades
                </label>
              </div>
            </div>
          )}
        </div>
      </div>

      <div style={{ display: 'flex', gap: '2rem', alignItems: 'flex-start' }}>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {loading && <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem' }}>Cargando publicaciones del Muro...</p>}
          {error && <p style={{ textAlign: 'center', color: 'var(--color-red-600)', padding: '2rem' }}>{error}</p>}
          {!loading && !error && filteredPosts.length === 0 && (
             <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem' }}>No hay publicaciones en este momento.</p>
          )}
        {!loading && filteredPosts.map((post, index) => (
          <div key={post.id} className="card interactive-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', animationDelay: `${index * 0.1}s` }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <span style={{ 
                    fontSize: '0.75rem', 
                    padding: '0.25rem 0.5rem', 
                    borderRadius: '1rem',
                    backgroundColor: post.type === 'oferta' || post.type === 'habilidad' ? 'var(--color-green-100)' : 'var(--color-yellow-100)',
                    color: post.type === 'oferta' || post.type === 'habilidad' ? 'var(--color-green-700)' : 'var(--color-orange-600)',
                    fontWeight: 'bold',
                    textTransform: 'uppercase'
                  }}>
                    {post.type === 'habilidad' ? 'oferta' : post.type}
                  </span>
                  {post.isMatch && (
                    <span style={{ 
                      fontSize: '0.75rem', 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '1rem',
                      backgroundColor: 'var(--color-purple-100)',
                      color: 'var(--color-purple-700)',
                      fontWeight: 'bold',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.25rem'
                    }}>
                      <Sparkles size={12} /> Match por tu {post.matchReason === 'HABILIDAD' ? 'habilidad' : 'necesidad'}
                    </span>
                  )}
                  <span style={{ color: 'var(--text-tertiary)', fontSize: '0.875rem' }}>{post.timeAgo}</span>
                </div>
                <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>{post.title}</h3>
                <p style={{ color: 'var(--text-secondary)' }}>{post.description}</p>
              </div>
              
              {post.price > 0 && (
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--accent-primary)' }}>
                    {post.price} 
                    <span style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontWeight: 'normal' }}> cr</span>
                  </span>
                </div>
              )}
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div
                  onClick={() => post.userId === user?.id ? navigate('/profile') : navigate(`/user/${post.userId}`)}
                  style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--accent-primary)', fontWeight: 'bold', cursor: 'pointer' }}
                >
                  {post.user.charAt(0)}
                </div>
                <div>
                  <div
                    onClick={() => post.userId === user?.id ? navigate('/profile') : navigate(`/user/${post.userId}`)}
                    style={{ fontWeight: '500', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', color: 'var(--accent-primary)', textDecoration: 'underline', textDecorationColor: 'transparent', transition: 'text-decoration-color 0.2s' }}
                    onMouseEnter={e => e.currentTarget.style.textDecorationColor = 'var(--accent-primary)'}
                    onMouseLeave={e => e.currentTarget.style.textDecorationColor = 'transparent'}
                  >
                    {post.user}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontSize: '0.75rem' }}>
                    <Star size={12} fill="currentColor" /> {post.reputation}
                  </div>
                </div>
              </div>
              
              {(() => {
                const alreadyRequested = sentRequests.has(post.id);
                const noCredits = post.price > balance;
                const disabled = alreadyRequested || noCredits;
                
                return (
                  <button 
                    className="btn-primary" 
                    style={{ 
                      padding: '0.5rem 1rem', 
                      fontSize: '0.875rem',
                      backgroundColor: disabled ? 'var(--bg-tertiary)' : undefined,
                      color: disabled ? 'var(--text-tertiary)' : undefined,
                      border: disabled ? 'none' : undefined,
                      cursor: disabled ? 'not-allowed' : 'pointer'
                    }}
                    disabled={disabled}
                    onClick={() => navigate(`/request/${post.id}?type=${post.type}&userId=${post.userId}&price=${post.price}&title=${encodeURIComponent(post.title)}&desc=${encodeURIComponent(post.description)}&owner=${encodeURIComponent(post.user)}&rep=${post.reputation}`)}
                  >
                    {alreadyRequested ? 'Ya ofertaste' : (noCredits ? 'Sin créditos' : 'Contactar')}
                  </button>
                );
              })()}
            </div>
          </div>
        ))}
        </div>

        {/* Podio Widget Lateral */}
        <div style={{ width: '300px', flexShrink: 0, position: 'sticky', top: '1rem' }}>
            <div className="card" style={{ padding: '0' }}>
              <div 
                style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', borderBottom: showPodio ? '1px solid var(--border-color)' : 'none', cursor: 'pointer', backgroundColor: 'var(--bg-secondary)', borderTopLeftRadius: '0.5rem', borderTopRightRadius: '0.5rem', borderBottomLeftRadius: showPodio ? '0' : '0.5rem', borderBottomRightRadius: showPodio ? '0' : '0.5rem' }}
                onClick={() => setShowPodio(!showPodio)}
              >
                <h3 style={{ fontSize: '1rem', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Trophy size={18} color="var(--color-orange-600)" />
                  Podio Semanal
                </h3>
                <ChevronDown size={18} style={{ transform: showPodio ? 'rotate(180deg)' : 'rotate(0)', transition: 'transform 0.2s' }} />
              </div>
              
              {showPodio && (
                <div style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: 0 }}>Usuarios destacados por sus aportes a la comunidad.</p>
                  
                  {podio && podio.length > 0 ? (
                    podio.map((usr, index) => (
                      <div key={usr.idUsuario} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div style={{ width: '24px', height: '24px', borderRadius: '50%', backgroundColor: index === 0 ? 'var(--color-yellow-100)' : index === 1 ? 'var(--bg-tertiary)' : 'var(--color-orange-100)', color: index === 0 ? 'var(--color-orange-600)' : index === 1 ? 'var(--text-secondary)' : '#b45309', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.75rem', fontWeight: 'bold' }}>
                          {index + 1}
                        </div>
                        <div style={{ width: '36px', height: '36px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '1rem' }}>
                          {usr.nombreUsuario?.charAt(0)}
                        </div>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontSize: '0.875rem', fontWeight: '600' }}>{usr.nombreUsuario}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--accent-warning)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                            <Star size={10} fill="currentColor" /> {usr.reputacion}
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div style={{ textAlign: 'center', padding: '1rem 0', color: 'var(--text-tertiary)', fontSize: '0.875rem' }}>
                      Aún no hay usuarios en el podio esta semana.
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
      </div>
    </div>
  );
};

export default Wall;
