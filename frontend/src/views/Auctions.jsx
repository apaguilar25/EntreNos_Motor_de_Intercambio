import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, Hammer } from 'lucide-react';

const Auctions = () => {
  const navigate = useNavigate();
  const mockAuctions = [
    {
      id: 1,
      title: 'Bicicleta Montañera Rin 26',
      description: 'Casi nueva, poco uso. La subasto por alimentos no perecederos o artículos de limpieza.',
      owner: 'Miguel Veliz',
      timeLeft: '02:15:30',
      currentOffers: 3,
      topOffer: '2 Harinas PAN + 1 Arroz'
    },
    {
      id: 2,
      title: 'Licuadora Oster',
      description: 'Funciona perfectamente pero compré una nueva. Busco productos de higiene personal.',
      owner: 'Andrea Aguilar',
      timeLeft: '12:00:00',
      currentOffers: 1,
      topOffer: '1 Champú + 2 Jabones'
    }
  ];

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Motor de Subastas de Activos</h2>
        <button className="btn-primary" style={{ backgroundColor: 'var(--accent-warning)', color: '#fff' }} onClick={() => navigate('/create-auction')}>Crear Subasta</button>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {mockAuctions.map((auction, index) => (
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
                <h3 style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>{auction.title}</h3>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem', fontSize: '0.875rem' }}>{auction.description}</p>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>Subastador: {auction.owner}</div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '2rem' }}>
                  <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: 'bold' }}>Tiempo Restante</span>
                    <span style={{ color: 'var(--color-orange-600)', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <Clock size={16} /> {auction.timeLeft}
                    </span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)', textTransform: 'uppercase', fontWeight: 'bold' }}>Mejor Oferta (Bienes)</span>
                    <span style={{ color: 'var(--color-green-700)', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <Hammer size={16} /> {auction.topOffer}
                    </span>
                  </div>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' }}>
                  <button 
                    className="btn-primary" 
                    style={{ backgroundColor: 'var(--color-green-700)', color: '#fff', padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                    onClick={() => navigate(`/request/${auction.id}?type=subasta`)}
                  >
                    Pujar con Bienes
                  </button>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{auction.currentOffers} pujas activas</span>
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
