import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Star, ChevronDown, Trophy, Sparkles, Medal, AlertCircle } from 'lucide-react';
import { AppContext } from '../App';

const Wall = () => {
  const navigate = useNavigate();
  const { user, balance, hasCatalog, controladorMuro } = useContext(AppContext);
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        setLoading(true);
        // Usar ControladorMuro en lugar de fetch crudo
        const data = await controladorMuro.obtenerPublicaciones(searchTerm);
        
        // Mapear DTO al formato que usa la vista
        const mappedPosts = data.map((item, index) => ({
          id: item.idPublicacion || index, // backend DTO has idPublicacion
          type: item.tipoPublicacion ? item.tipoPublicacion.toLowerCase() : 'oferta',
          user: item.nombreUsuario || 'Usuario Desconocido',
          userId: item.idUsuario,
          reputation: item.reputacionUsuario || 0,
          title: item.nombreServicio || 'Sin título',
          description: item.descripcion || '',
          price: item.precioCreditos || 0,
          timeAgo: 'reciente'
        }))
        .filter(post => post.userId !== (user?.id || 'USR-1001'));
        
        setPosts(mappedPosts);
        setError(null);
      } catch (err) {
        console.error(err);
        setError('No se pudo conectar con el servidor.');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [searchTerm, controladorMuro, user]);

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

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Muro de Publicaciones</h2>
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
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center' }}>
                Faltan por agregar categorías (Definición Backend)
              </div>
            </div>
          )}
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {loading && <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem' }}>Cargando publicaciones del Muro...</p>}
        {error && <p style={{ textAlign: 'center', color: 'var(--color-red-600)', padding: '2rem' }}>{error}</p>}
        {!loading && !error && posts.length === 0 && (
           <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem' }}>No hay publicaciones en este momento.</p>
        )}
        {!loading && posts.map((post, index) => (
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
                  <span style={{ color: 'var(--text-tertiary)', fontSize: '0.875rem' }}>{post.timeAgo}</span>
                </div>
                <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>{post.title}</h3>
                <p style={{ color: 'var(--text-secondary)' }}>{post.description}</p>
              </div>
              
              <div style={{ textAlign: 'right' }}>
                <span style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--accent-primary)' }}>
                  {post.price} 
                  <span style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontWeight: 'normal' }}> cr</span>
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--accent-primary)', fontWeight: 'bold' }}>
                  {post.user.charAt(0)}
                </div>
                <div>
                  <div style={{ fontWeight: '500', fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    {post.user}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontSize: '0.75rem' }}>
                    <Star size={12} fill="currentColor" /> {post.reputation}
                  </div>
                </div>
              </div>
              
              <button 
                className="btn-primary" 
                style={{ 
                  padding: '0.5rem 1rem', 
                  fontSize: '0.875rem',
                  backgroundColor: post.price > balance ? 'var(--bg-tertiary)' : undefined,
                  color: post.price > balance ? 'var(--text-tertiary)' : undefined,
                  border: post.price > balance ? 'none' : undefined,
                  cursor: post.price > balance ? 'not-allowed' : 'pointer'
                }}
                disabled={post.price > balance}
                onClick={() => navigate(`/request/${post.id}?type=${post.type}&userId=${post.userId}&price=${post.price}&title=${encodeURIComponent(post.title)}&desc=${encodeURIComponent(post.description)}&owner=${encodeURIComponent(post.user)}&rep=${post.reputation}`)}
              >
                {post.price > balance ? 'Sin créditos' : 'Contactar'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Wall;
