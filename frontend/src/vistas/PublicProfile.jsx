import React, { useState, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Star, Trophy, Medal, BookOpen, Hammer, AlertCircle } from 'lucide-react';
import { AppContext } from '../App';

const BASE_URL = 'http://localhost:8080/api';

// Colores del podio
const podioColors = ['#F59E0B', '#9CA3AF', '#B45309'];
const podioBg = ['var(--color-yellow-100)', 'var(--bg-tertiary)', 'var(--color-orange-100)'];
const podioLabel = ['🥇 1er lugar', '🥈 2do lugar', '🥉 3er lugar'];

const PublicProfile = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { controladorGamificacion } = useContext(AppContext);

  const [perfil, setPerfil] = useState(null);
  const [publicaciones, setPublicaciones] = useState([]);
  const [subastas, setSubastas] = useState([]);
  const [podioPos, setPodioPos] = useState(null); // null o número 1-3
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('publicaciones');

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        // 1. Perfil público del usuario (endpoint preparado por el equipo backend)
        const resPerfil = await fetch(`${BASE_URL}/usuarios/${id}/perfil-publico`);
        if (resPerfil.ok) {
          const data = await resPerfil.json();
          setPerfil(data);
        } else {
          // Fallback: intentar con el endpoint general si perfil-publico aún no existe
          const resFallback = await fetch(`${BASE_URL}/usuarios/${id}`);
          if (resFallback.ok) {
            setPerfil(await resFallback.json());
          } else {
            setError('No se pudo cargar el perfil de este usuario.');
          }
        }

        // 2. Publicaciones del usuario
        const resPubs = await fetch(`${BASE_URL}/publicaciones/usuario/${id}`);
        if (resPubs.ok) {
          setPublicaciones(await resPubs.json());
        }

        // 3. Todas las subastas → filtrar por propietario
        const resSub = await fetch(`${BASE_URL}/subastas`);
        if (resSub.ok) {
          const allSubs = await resSub.json();
          setSubastas(allSubs.filter(s => s.idPropietario === id));
        }

        // 4. Podio — verificar si aparece
        if (controladorGamificacion) {
          try {
            const podioData = await controladorGamificacion.obtenerPodio();
            const lista = podioData?.proveedorElite || (Array.isArray(podioData) ? podioData : []);
            const pos = lista.findIndex(u => u.idUsuario === id);
            if (pos >= 0) setPodioPos(pos); // 0-based index
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
  const reputacion = typeof perfil?.reputacion === 'number' ? perfil.reputacion.toFixed(1) : (perfil?.reputacionUsuario?.toFixed(1) ?? '—');
  const logros = perfil?.logrosDesbloqueados || perfil?.logros || [];
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
          {perfil?.urlFotoPerfil ? (
            <img src={perfil.urlFotoPerfil} alt={nombre} style={{ width: '80px', height: '80px', borderRadius: '50%', objectFit: 'cover', border: '3px solid rgba(255,255,255,0.5)' }} />
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

          {/* Podio badge */}
          {podioPos !== null && (
            <div style={{
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.25rem',
              backgroundColor: 'rgba(255,255,255,0.15)', borderRadius: '0.75rem',
              padding: '0.75rem 1rem', border: '1px solid rgba(255,255,255,0.3)'
            }}>
              <Trophy size={24} color={podioColors[podioPos]} />
              <span style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#fff' }}>
                {podioLabel[podioPos]}
              </span>
              <span style={{ fontSize: '0.65rem', color: 'rgba(255,255,255,0.7)' }}>Podio semanal</span>
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
          {publicaciones.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem 0' }}>
              Este usuario no tiene publicaciones activas.
            </p>
          ) : (
            publicaciones.map((pub, i) => {
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
            })
          )}
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
            subastas.map((s, i) => (
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
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default PublicProfile;
