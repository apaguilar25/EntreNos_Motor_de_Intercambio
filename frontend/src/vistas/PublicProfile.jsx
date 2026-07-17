import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Star, Trophy, Medal, BookOpen, Hammer, AlertCircle, Sparkles } from 'lucide-react';
import Pagination from '../componentes/ui/Pagination';
import { AppContext } from '../App';

const BASE_URL = 'http://localhost:8080/api';

const PublicProfile = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { controladorGamificacion } = useContext(AppContext);

  const [perfil, setPerfil] = useState(null);
  const [publicaciones, setPublicaciones] = useState([]);
  const [subastas, setSubastas] = useState([]);
  const [podioTops, setPodioTops] = useState([]); // array of tops the user is in
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('publicaciones');
  const [imgError, setImgError] = useState(false);
  const [pubFilter, setPubFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    setCurrentPage(1);
  }, [activeTab, pubFilter]);

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const token = sessionStorage.getItem('entreNosToken');
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        // 1. Perfil público del usuario (endpoint preparado por el equipo backend)
        const resPerfil = await fetch(`${BASE_URL}/usuarios/${id}/perfil-publico`, { headers });
        let perfilData = null;
        if (resPerfil.ok) {
          const data = await resPerfil.json();
          perfilData = {
            ...data.datosPersonales,
            logros: data.logros || [],
            subastasList: data.subastas || []
          };
          setPerfil(perfilData);
          setSubastas(perfilData.subastasList);
        } else {
          // Fallback: intentar con el endpoint general si perfil-publico aún no existe
          const resFallback = await fetch(`${BASE_URL}/usuarios/${id}`, { headers });
          if (resFallback.ok) {
            perfilData = await resFallback.json();
            setPerfil(perfilData);
          } else {
            setError('No se pudo cargar el perfil de este usuario.');
          }
        }

        // 2. Publicaciones del usuario
        const resPubs = await fetch(`${BASE_URL}/publicaciones/usuario/${id}`, { headers });
        if (resPubs.ok) {
          setPublicaciones(await resPubs.json());
        }

        // 3. Subastas del usuario (si no vinieron en perfil-publico)
        if (!perfilData?.subastasList) {
          const resSub = await fetch(`${BASE_URL}/subastas`, { headers });
          if (resSub.ok) {
            const allSubs = await resSub.json();
            setSubastas(allSubs.filter(s => s.idPropietario === id));
          }
        }

        // 4. Podio — verificar si aparece
        if (controladorGamificacion) {
          try {
            const podioData = await controladorGamificacion.obtenerPodio();
            const tops = [];
            
            if (podioData.proveedorElite) {
              const pos = podioData.proveedorElite.findIndex(u => u.idUsuario === id);
              if (pos >= 0) tops.push({ name: 'Proveedor Elite', pos, icon: Trophy, color: '#F59E0B' });
            }
            if (podioData.motorEconomia) {
              const pos = podioData.motorEconomia.findIndex(u => u.idUsuario === id);
              if (pos >= 0) tops.push({ name: 'Motor de la Economía', pos, icon: Sparkles, color: '#10B981' });
            }
            if (podioData.embajadorCalidad) {
              const pos = podioData.embajadorCalidad.findIndex(u => u.idUsuario === id);
              if (pos >= 0) tops.push({ name: 'Embajador de Calidad', pos, icon: Medal, color: '#3B82F6' });
            }
            
            setPodioTops(tops);
          } catch (_) { /* podio no disponible */ }
        }
      } catch (e) {
        console.error('Error cargando perfil público:', e);
        setError('Error de conexión con el servidor.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id, controladorGamificacion]);

  if (loading) {
    return (
      <div className="animate-in" style={{ maxWidth: '800px', margin: '0 auto', textAlign: 'center', padding: '4rem 0' }}>
        <p style={{ color: 'var(--text-tertiary)' }}>Cargando perfil...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="animate-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
        <button onClick={() => navigate(-1)} style={{ background: 'transparent', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)', marginBottom: '2rem' }}>
          <ArrowLeft size={20} /> Volver
        </button>
        <div className="card" style={{ display: 'flex', alignItems: 'center', gap: '1rem', backgroundColor: 'var(--color-yellow-100)', borderColor: 'var(--color-orange-600)' }}>
          <AlertCircle color="var(--color-orange-600)" />
          <p style={{ color: 'var(--color-orange-600)', margin: 0 }}>{error}</p>
        </div>
      </div>
    );
  }

  const nombre = perfil?.nombre || perfil?.nombreUsuario || 'Usuario';
  const repValue = perfil?.promedioCalificacion ?? perfil?.reputacion ?? perfil?.reputacionUsuario;
  const reputacion = typeof repValue === 'number' ? repValue.toFixed(1) : '—';
  const logros = perfil?.logros || perfil?.logrosDesbloqueados || [];
  const inicial = nombre.charAt(0).toUpperCase();

  return (
    <div className="animate-in" style={{ maxWidth: '860px', margin: '0 auto' }}>
      {/* Header */}
      <button
        onClick={() => navigate(-1)}
        style={{ background: 'transparent', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)', marginBottom: '2rem' }}
      >
        <ArrowLeft size={20} /> Volver
      </button>

      {/* Hero card */}
      <div className="card" style={{
        background: 'linear-gradient(135deg, var(--accent-primary) 0%, var(--color-purple-600, #7c3aed) 100%)',
        color: '#fff',
        marginBottom: '1.5rem',
        position: 'relative',
        overflow: 'hidden'
      }}>
        {/* Decorative circle */}
        <div style={{ position: 'absolute', top: '-40px', right: '-40px', width: '180px', height: '180px', borderRadius: '50%', background: 'rgba(255,255,255,0.08)' }} />

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', position: 'relative', zIndex: 1 }}>
          {/* Avatar */}
          {perfil?.urlFotoPerfil && !imgError ? (
            <img 
              src={perfil.urlFotoPerfil} 
              alt={nombre} 
              onError={() => setImgError(true)}
              style={{ width: '80px', height: '80px', borderRadius: '50%', objectFit: 'cover', border: '3px solid rgba(255,255,255,0.5)' }} 
            />
          ) : (
            <div style={{
              width: '80px', height: '80px', borderRadius: '50%',
              backgroundColor: 'rgba(255,255,255,0.2)', display: 'flex',
              alignItems: 'center', justifyContent: 'center',
              fontSize: '2rem', fontWeight: 'bold', color: '#fff',
              border: '3px solid rgba(255,255,255,0.4)'
            }}>
              {inicial}
            </div>
          )}

          <div style={{ flex: 1 }}>
            <h2 style={{ margin: 0, fontSize: '1.5rem', color: '#fff' }}>{nombre}</h2>
            {perfil?.descripcionPersonal && (
              <p style={{ margin: '0.25rem 0 0', fontSize: '0.875rem', color: 'rgba(255,255,255,0.8)' }}>{perfil.descripcionPersonal}</p>
            )}
            <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginTop: '0.75rem', flexWrap: 'wrap' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.9rem', fontWeight: '600' }}>
                <Star size={16} fill="#fff" /> {reputacion}
              </span>
              <span style={{ fontSize: '0.875rem', color: 'rgba(255,255,255,0.8)' }}>
                {publicaciones.length} publicación{publicaciones.length !== 1 ? 'es' : ''}
              </span>
              <span style={{ fontSize: '0.875rem', color: 'rgba(255,255,255,0.8)' }}>
                {subastas.length} subasta{subastas.length !== 1 ? 's' : ''}
              </span>
            </div>
          </div>

          {/* Podio badges */}
          {podioTops.length > 0 && (
            <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              {podioTops.map((top, idx) => {
                const Icon = top.icon;
                const labels = ['🥇 1er lugar', '🥈 2do lugar', '🥉 3er lugar'];
                return (
                  <div key={idx} style={{
                    display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.25rem',
                    backgroundColor: 'rgba(255,255,255,0.15)', borderRadius: '0.75rem',
                    padding: '0.75rem 1rem', border: '1px solid rgba(255,255,255,0.3)'
                  }}>
                    <Icon size={24} color={top.color} />
                    <span style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#fff' }}>
                      {labels[top.pos] || `Top ${top.pos + 1}`}
                    </span>
                    <span style={{ fontSize: '0.65rem', color: 'rgba(255,255,255,0.7)' }}>{top.name}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Medallas / Logros */}
      {logros.length > 0 && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', fontSize: '1rem' }}>
            <Medal size={18} color="var(--color-orange-600)" /> Medallas obtenidas
          </h3>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
            {logros.map((l, i) => (
              <span key={i} title={l.descripcionLogro || l.nombre || l} style={{
                display: 'inline-flex', alignItems: 'center', gap: '0.35rem',
                backgroundColor: 'var(--color-yellow-100)', color: 'var(--color-orange-600)',
                padding: '0.3rem 0.75rem', borderRadius: '1rem',
                fontSize: '0.8rem', fontWeight: '600'
              }}>
                🏅 {l.nombreLogro || l.nombre || l}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', marginBottom: '1.5rem', gap: '2rem' }}>
        {[
          { key: 'publicaciones', label: 'Publicaciones', icon: <BookOpen size={16} /> },
          { key: 'subastas', label: 'Subastas', icon: <Hammer size={16} /> },
        ].map(tab => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            style={{
              background: 'none', border: 'none', padding: '0.75rem 0',
              fontSize: '0.95rem', fontWeight: activeTab === tab.key ? 'bold' : 'normal',
              color: activeTab === tab.key ? 'var(--accent-primary)' : 'var(--text-secondary)',
              borderBottom: activeTab === tab.key ? '2px solid var(--accent-primary)' : '2px solid transparent',
              cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.5rem'
            }}
          >
            {tab.icon} {tab.label}
            <span style={{
              backgroundColor: activeTab === tab.key ? 'var(--accent-primary)' : 'var(--bg-tertiary)',
              color: activeTab === tab.key ? '#fff' : 'var(--text-secondary)',
              borderRadius: '1rem', padding: '0.1rem 0.5rem', fontSize: '0.72rem', fontWeight: '600'
            }}>
              {tab.key === 'publicaciones' ? publicaciones.length : subastas.length}
            </span>
          </button>
        ))}
      </div>

      {/* Tab: Publicaciones */}
      {activeTab === 'publicaciones' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          
          {/* Filtros de Publicaciones */}
          {publicaciones.length > 0 && (
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
              {['all', 'HABILIDAD', 'NECESIDAD'].map(fType => (
                <button
                  key={fType}
                  onClick={() => setPubFilter(fType)}
                  style={{
                    padding: '0.35rem 1rem',
                    borderRadius: '2rem',
                    border: '1px solid',
                    fontSize: '0.85rem',
                    fontWeight: pubFilter === fType ? 'bold' : 'normal',
                    backgroundColor: pubFilter === fType ? 'var(--accent-primary)' : 'transparent',
                    color: pubFilter === fType ? '#fff' : 'var(--text-secondary)',
                    borderColor: pubFilter === fType ? 'var(--accent-primary)' : 'var(--border-color)',
                    cursor: 'pointer'
                  }}
                >
                  {fType === 'all' ? 'Todas' : fType === 'HABILIDAD' ? 'Ofertas' : 'Necesidades'}
                </button>
              ))}
            </div>
          )}

          {(() => {
            const filtradas = publicaciones.filter(pub => pubFilter === 'all' || pub.tipoPublicacion === pubFilter);
            if (publicaciones.length === 0) {
              return (
                <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem 0' }}>
                  Este usuario no tiene publicaciones activas.
                </p>
              );
            }
            if (filtradas.length === 0) {
              return (
                <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem 0' }}>
                  No hay publicaciones que coincidan con este filtro.
                </p>
              );
            }
            return (
                <>
                  <Pagination currentPage={currentPage} totalItems={filtradas.length} pageSize={5} onPageChange={setCurrentPage} />
                  {filtradas.slice((currentPage - 1) * 5, currentPage * 5).map((pub, i) => {
              const esOferta = pub.tipoPublicacion === 'HABILIDAD';
              return (
                <div key={pub.idPublicacion || i} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
                      <span style={{
                        fontSize: '0.72rem', fontWeight: 'bold', padding: '0.2rem 0.5rem', borderRadius: '1rem',
                        backgroundColor: esOferta ? 'var(--color-green-100)' : 'var(--color-yellow-100)',
                        color: esOferta ? 'var(--color-green-700)' : 'var(--color-orange-600)',
                        textTransform: 'uppercase'
                      }}>
                        {esOferta ? 'Oferta' : 'Necesidad'}
                      </span>
                    </div>
                    <h4 style={{ margin: '0 0 0.25rem', fontSize: '1rem' }}>{pub.nombreServicio || pub.titulo || 'Sin título'}</h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', margin: 0 }}>{pub.descripcion}</p>
                  </div>
                  {pub.precioCreditos > 0 && (
                    <div style={{ textAlign: 'right', flexShrink: 0 }}>
                      <span style={{ fontSize: '1.25rem', fontWeight: 'bold', color: 'var(--accent-primary)' }}>
                        {pub.precioCreditos}
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)', fontWeight: 'normal' }}> cr</span>
                      </span>
                    </div>
                  )}
                </div>
              );
            })}
            </>
          );
          })()}
        </div>
      )}

      {/* Tab: Subastas */}
      {activeTab === 'subastas' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {subastas.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem 0' }}>
              Este usuario no tiene subastas activas.
            </p>
          ) : (
            <>
              <Pagination currentPage={currentPage} totalItems={subastas.length} pageSize={5} onPageChange={setCurrentPage} />
              {subastas.slice((currentPage - 1) * 5, currentPage * 5).map((s, i) => (
              <div key={s.id || i} className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
                    <span style={{
                      fontSize: '0.72rem', fontWeight: 'bold', padding: '0.2rem 0.5rem', borderRadius: '1rem',
                      backgroundColor: s.estado === 'ACTIVA' ? 'var(--color-green-100)' : 'var(--bg-tertiary)',
                      color: s.estado === 'ACTIVA' ? 'var(--color-green-700)' : 'var(--text-tertiary)',
                      textTransform: 'uppercase'
                    }}>
                      {s.estado || 'SUBASTA'}
                    </span>
                    {s.estadoFisico && (
                      <span style={{ fontSize: '0.72rem', color: 'var(--text-tertiary)' }}>{s.estadoFisico}</span>
                    )}
                  </div>
                  <h4 style={{ margin: '0 0 0.25rem', fontSize: '1rem' }}>{s.nombreActivo || 'Sin título'}</h4>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', margin: 0 }}>{s.descripcion}</p>
                </div>
                <div style={{ textAlign: 'right', flexShrink: 0 }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>{s.propuestas?.length || 0} pujas</div>
                </div>
              </div>
            ))}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default PublicProfile;
