import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Star, ChevronDown } from 'lucide-react';

const Wall = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);

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
      timeAgo: 'hace 5 horas'
    },
    {
      id: 3,
      type: 'oferta',
      user: 'Carlos Ruiz',
      reputation: 4.2,
      title: 'Paseo de Perros',
      description: 'Paseo tu mascota por el conjunto residencial en horarios de la mañana o tarde.',
      price: 15,
      timeAgo: 'hace 1 día'
    }
  ];

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

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {mockPosts.map((post, index) => (
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
                  <div style={{ fontWeight: '500', fontSize: '0.875rem' }}>{post.user}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontSize: '0.75rem' }}>
                    <Star size={12} fill="currentColor" /> {post.reputation}
                  </div>
                </div>
              </div>
              
              <button className="btn-primary">Contactar</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Wall;
