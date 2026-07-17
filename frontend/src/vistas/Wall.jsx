import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Star, ChevronDown, Trophy, Sparkles, Medal, AlertCircle, Award, Users, ThumbsUp } from 'lucide-react';
import { AppContext } from '../App';

const PODIUM_META = {
  proveedorElite: { icon: <Award size={16} />, label: 'Proveedor Elite', desc: 'Usuarios con mayor oferta de servicios en la comunidad.', color: '#b45309', bg: 'var(--color-orange-100)' },
  motorEconomia: { icon: <ThumbsUp size={16} />, label: 'Motor de Economía', desc: 'Usuarios con mayor interacción y transacciones realizadas.', color: '#0369a1', bg: '#dbeafe' },
  embajadorCalidad: { icon: <Medal size={16} />, label: 'Embajador de Calidad', desc: 'Usuarios con mejor reputación y reseñas positivas.', color: '#15803d', bg: '#dcfce7' }
};

const Wall = () => {
  const navigate = useNavigate();
  const { user, balance, hasCatalog, controladorMuro, controladorGamificacion, controladorPerfil } = useContext(AppContext);
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [tab, setTab] = useState('explorar'); // 'explorar' o 'parati'
  const [filterType, setFilterType] = useState('all'); // 'all', 'oferta', 'necesidad'
  const [posts, setPosts] = useState([]);
  const [podio, setPodio] = useState({ proveedorElite: [], motorEconomia: [], embajadorCalidad: [] });
  const [showPodio, setShowPodio] = useState(true);
  const [showPodiumCat, setShowPodiumCat] = useState({ proveedorElite: true, motorEconomia: true, embajadorCalidad: true });
  const [podiumUserIds, setPodiumUserIds] = useState(new Set());
  const [podiumTooltip, setPodiumTooltip] = useState(null);
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
        const raw = await controladorGamificacion.obtenerPodio();
        if (raw && !Array.isArray(raw)) {
          const safe = {
            proveedorElite: raw.proveedorElite || [],
            motorEconomia: raw.motorEconomia || [],
            embajadorCalidad: raw.embajadorCalidad || []
          };
          setPodio(safe);
          const ids = new Set();
          [...safe.proveedorElite, ...safe.motorEconomia, ...safe.embajadorCalidad].forEach(u => ids.add(u.idUsuario));
          setPodiumUserIds(ids);
        }
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
                    {podiumUserIds.has(post.userId) && (
                      <Trophy size={14} color="var(--color-orange-600)" style={{ cursor: 'pointer', marginLeft: '0.25rem' }}
                        onClick={(e) => { e.stopPropagation(); setPodiumTooltip(post.userId); }} />
                    )}
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
                  {Object.keys(PODIUM_META).map(catKey => {
                    const cat = PODIUM_META[catKey];
                    const entries = (podio[catKey] || []).slice(0, 3);
                    const isOpen = showPodiumCat[catKey];
                    return (
                      <div key={catKey} style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', overflow: 'hidden' }}>
                        <div
                          style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0.75rem', cursor: 'pointer', backgroundColor: cat.bg }}
                          onClick={() => setShowPodiumCat({ ...showPodiumCat, [catKey]: !isOpen })}
                        >
                          <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', fontWeight: 'bold', color: cat.color }}>
                            {cat.icon} {cat.label}
                          </span>
                          <ChevronDown size={14} style={{ transform: isOpen ? 'rotate(180deg)' : 'rotate(0)', transition: 'transform 0.2s', color: cat.color }} />
                        </div>
                        {isOpen && (
                          <div style={{ padding: '0.5rem 0.75rem' }}>
                            <p style={{ fontSize: '0.7rem', color: 'var(--text-tertiary)', margin: '0 0 0.5rem 0' }}>{cat.desc}</p>
                            {entries.length > 0 ? entries.map((usr, idx) => (
                              <div key={usr.idUsuario} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.3rem 0', borderBottom: idx < entries.length - 1 ? '1px solid var(--border-color)' : 'none' }}>
                                <div style={{ width: '20px', height: '20px', borderRadius: '50%', backgroundColor: idx === 0 ? 'var(--color-yellow-100)' : idx === 1 ? 'var(--bg-tertiary)' : 'var(--color-orange-100)', color: idx === 0 ? 'var(--color-orange-600)' : idx === 1 ? 'var(--text-secondary)' : '#b45309', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.65rem', fontWeight: 'bold' }}>
                                  {idx + 1}
                                </div>
                                <div style={{ width: '28px', height: '28px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '0.75rem' }}>
                                  {usr.nombreUsuario?.charAt(0)}
                                </div>
                                <div style={{ flex: 1 }}>
                                  <div style={{ fontSize: '0.8rem', fontWeight: '600' }}>{usr.nombreUsuario}</div>
                                  <div style={{ fontSize: '0.65rem', color: 'var(--accent-warning)', display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                                    <Star size={9} fill="currentColor" /> {usr.valor}
                                  </div>
                                </div>
                              </div>
                            )) : (
                              <div style={{ textAlign: 'center', padding: '0.5rem 0', color: 'var(--text-tertiary)', fontSize: '0.75rem' }}>
                                Sin datos esta semana
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Tooltip modal for trophy click */}
          {podiumTooltip && (
            <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}
              onClick={() => setPodiumTooltip(null)}>
              <div style={{ background: 'var(--bg-primary)', border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1.5rem', maxWidth: '320px', width: '90%', boxShadow: '0 8px 24px rgba(0,0,0,0.2)' }}
                onClick={e => e.stopPropagation()}>
                <h4 style={{ margin: '0 0 1rem 0', display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1rem' }}>
                  <Trophy size={18} color="var(--color-orange-600)" /> Podios alcanzados
                </h4>
                {Object.keys(PODIUM_META).filter(catKey => (podio[catKey] || []).some(u => u.idUsuario === podiumTooltip)).length === 0 ? (
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-tertiary)' }}>Este usuario no está en ningún podio esta semana.</p>
                ) : (
                  Object.keys(PODIUM_META).filter(catKey => (podio[catKey] || []).some(u => u.idUsuario === podiumTooltip)).map(catKey => {
                    const cat = PODIUM_META[catKey];
                    return (
                      <div key={catKey} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.5rem', padding: '0.5rem 0', borderBottom: '1px solid var(--border-color)' }}>
                        <div style={{ color: cat.color, marginTop: '0.1rem' }}>{cat.icon}</div>
                        <div>
                          <div style={{ fontWeight: 'bold', fontSize: '0.85rem', color: cat.color }}>{cat.label}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{cat.desc}</div>
                        </div>
                      </div>
                    );
                  })
                )}
                <button className="btn-primary" style={{ marginTop: '1rem', width: '100%', padding: '0.4rem', fontSize: '0.85rem' }} onClick={() => setPodiumTooltip(null)}>Cerrar</button>
              </div>
            </div>
          )}
      </div>
    </div>
  );
};

export default Wall;
