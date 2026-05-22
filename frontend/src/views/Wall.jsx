import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Star, ChevronDown, Trophy, Sparkles, Medal } from 'lucide-react';

const Wall = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [activeTab, setActiveTab] = useState('explorar'); // 'explorar' | 'parati'

  // Mocks de publicaciones
  const mockPosts = [
    {
      id: 1,
      type: 'oferta',
      user: 'Juan Pérez',
      reputation: 4.8,
      title: 'Reparación de Computadoras',
      description: 'Ofrezco servicio técnico para laptops y PCs de escritorio. Limpieza, formateo e instalación de software.',
      price: 50,
      timeAgo: 'hace 2 horas'
    },
    {
      id: 2,
      type: 'demanda',
      user: 'María López',
      reputation: 5.0,
      title: 'Clases de Matemáticas',
      description: 'Necesito tutor para mi hijo de 3er año de secundaria para prepararse para el examen final.',
      price: 30,
      timeAgo: 'hace 5 horas',
      isPodium: true,
      podiumCategory: 'Embajador de Calidad'
    },
    {
      id: 3,
      type: 'oferta',
      user: 'Carlos Ruiz',
      reputation: 4.2,
      title: 'Paseo de Perros',
      description: 'Paseo tu mascota por el conjunto residencial en horarios de la mañana o tarde.',
      price: 15,
      timeAgo: 'hace 1 día',
      isSuggested: true // Para simular HU6
    }
  ];

  // Podio mock
  const podium = [
    { title: 'Proveedor Élite', user: 'Ana Rojas', rep: 5.0, icon: <Trophy size={16} color="var(--color-orange-600)" /> },
    { title: 'Motor de la Economía', user: 'Luis Gomez', rep: 4.8, icon: <Star size={16} color="var(--accent-primary)" /> },
    { title: 'Embajador de Calidad', user: 'María López', rep: 5.0, icon: <Medal size={16} color="var(--color-green-700)" /> },
  ];

  const displayedPosts = activeTab === 'parati' ? mockPosts.filter(p => p.isSuggested) : mockPosts;

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Muro de Publicaciones</h2>
        <button className="btn-primary" onClick={() => navigate('/publish')}>Publicar</button>
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

      {/* Podio Semanal (HU10) */}
      <div className="card" style={{ marginBottom: '2rem', backgroundColor: 'var(--bg-secondary)', border: '1px dashed var(--border-color)' }}>
        <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-primary)' }}>
          <Trophy size={20} color="var(--color-orange-600)" /> Podio Semanal
        </h3>
        <div style={{ display: 'flex', gap: '1rem', overflowX: 'auto', paddingBottom: '0.5rem' }}>
          {podium.map((winner, idx) => (
            <div key={idx} style={{ flex: '1', minWidth: '150px', backgroundColor: 'var(--bg-primary)', padding: '1rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
              <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                {winner.user.charAt(0)}
              </div>
              <div style={{ fontSize: '0.875rem', fontWeight: 'bold', marginBottom: '0.25rem' }}>{winner.user}</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                {winner.icon} {winner.title}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Tabs (HU6) */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
        <button 
          onClick={() => setActiveTab('explorar')}
          style={{ padding: '0.5rem 1rem', background: 'none', border: 'none', borderBottom: activeTab === 'explorar' ? '2px solid var(--accent-primary)' : '2px solid transparent', color: activeTab === 'explorar' ? 'var(--accent-primary)' : 'var(--text-secondary)', fontWeight: activeTab === 'explorar' ? 'bold' : 'normal', cursor: 'pointer' }}
        >
          Explorar
        </button>
        <button 
          onClick={() => setActiveTab('parati')}
          style={{ padding: '0.5rem 1rem', background: 'none', border: 'none', borderBottom: activeTab === 'parati' ? '2px solid var(--accent-primary)' : '2px solid transparent', color: activeTab === 'parati' ? 'var(--accent-primary)' : 'var(--text-secondary)', fontWeight: activeTab === 'parati' ? 'bold' : 'normal', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem' }}
        >
          <Sparkles size={16} /> Para ti
        </button>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {displayedPosts.length === 0 && (
           <p style={{ textAlign: 'center', color: 'var(--text-tertiary)', padding: '2rem' }}>No hay sugerencias en este momento.</p>
        )}
        {displayedPosts.map((post, index) => (
          <div key={post.id} className="card interactive-card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem', animationDelay: `${index * 0.1}s` }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <span style={{ 
                    fontSize: '0.75rem', 
                    padding: '0.25rem 0.5rem', 
                    borderRadius: '1rem',
                    backgroundColor: post.type === 'oferta' ? 'var(--color-green-100)' : 'var(--color-yellow-100)',
                    color: post.type === 'oferta' ? 'var(--color-green-700)' : 'var(--color-orange-600)',
                    fontWeight: 'bold',
                    textTransform: 'uppercase'
                  }}>
                    {post.type}
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
                    {post.isPodium && (
                      <span style={{ fontSize: '0.65rem', backgroundColor: 'var(--color-orange-600)', color: '#fff', padding: '0.125rem 0.5rem', borderRadius: '1rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                        <Trophy size={10} /> Vecino Destacado
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontSize: '0.75rem' }}>
                    <Star size={12} fill="currentColor" /> {post.reputation}
                  </div>
                </div>
              </div>
              
              <button 
                className="btn-primary" 
                style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                onClick={() => navigate(`/request/${post.id}?type=${post.type}`)}
              >
                Contactar
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Wall;
