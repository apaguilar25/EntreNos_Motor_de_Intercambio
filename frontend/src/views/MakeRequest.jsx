import React, { useState, useContext } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { AppContext } from '../App';
import { ArrowLeft, User as UserIcon, Package, MessageSquare } from 'lucide-react';

const MakeRequest = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const type = searchParams.get('type') || 'oferta'; // oferta, demanda, subasta
  const { user } = useContext(AppContext);

  const [message, setMessage] = useState('');
  const [items, setItems] = useState('');

  // Mock data based on type
  const postData = {
    oferta: {
      title: 'Reparación de Computadoras',
      description: 'Ofrezco servicio técnico para laptops y PCs de escritorio. Limpieza, formateo e instalación de software.',
      owner: 'Juan Pérez',
      reputation: 4.8,
      price: '50 cr',
      label: 'OFERTA'
    },
    demanda: {
      title: 'Mantenimiento de Aire Acondicionado',
      description: 'Necesito limpieza de filtros y revisión de gas para un aire split de 12000 BTU.',
      owner: 'María López',
      reputation: 4.5,
      price: 'Hasta 60 cr',
      label: 'NECESIDAD'
    },
    subasta: {
      title: 'Bicicleta Montañera Rin 26',
      description: 'Casi nueva, poco uso. La subasto por alimentos no perecederos o artículos de limpieza.',
      owner: 'Miguel Veliz',
      reputation: 5.0,
      price: 'Alimentos / Limpieza',
      label: 'SUBASTA'
    }
  };

  const data = postData[type] || postData.oferta;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (type === 'subasta' && !items) {
      alert('Debes indicar los bienes que ofreces para la subasta.');
      return;
    }
    if (!message) {
      alert('Debes escribir un mensaje.');
      return;
    }
    
    alert('¡Solicitud enviada con éxito!');
    navigate(-1);
  };

  return (
    <div className="animate-in" style={{ maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <button 
          onClick={() => navigate(-1)} 
          style={{ background: 'transparent', display: 'flex', alignItems: 'center', color: 'var(--text-secondary)' }}
        >
          <ArrowLeft size={24} />
        </button>
        <h2>{type === 'subasta' ? 'Realizar Puja' : 'Contactar y Solicitar'}</h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
        
        {/* Left Column: Form */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>Tu Propuesta</h3>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--accent-primary)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <UserIcon size={20} />
            </div>
            <div>
              <div style={{ fontWeight: 'bold' }}>{user?.name || 'Usuario Actual'}</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Reputación: 5.0 ⭐</div>
            </div>
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {type === 'subasta' && (
              <div>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', fontWeight: '500' }}>
                  <Package size={16} /> Bienes a Ofrecer
                </label>
                <input 
                  type="text" 
                  value={items}
                  onChange={(e) => setItems(e.target.value)}
                  placeholder="Ej: 2 Harinas PAN + 1 Arroz"
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
                />
              </div>
            )}

            <div>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', fontWeight: '500' }}>
                <MessageSquare size={16} /> Mensaje
              </label>
              <textarea 
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Escribe un mensaje detallando tu disponibilidad o condiciones..."
                rows={5}
                style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none', resize: 'vertical' }}
              />
            </div>

            <button type="submit" className="btn-primary" style={{ marginTop: '1rem', padding: '0.75rem' }}>
              {type === 'subasta' ? 'Enviar Puja' : 'Enviar Solicitud'}
            </button>
          </form>
        </div>

        {/* Right Column: Post Summary */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', backgroundColor: 'var(--bg-secondary)', border: '1px dashed var(--border-color)' }}>
          <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>Resumen de la Publicación</h3>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <UserIcon size={20} />
            </div>
            <div>
              <div style={{ fontWeight: 'bold' }}>{data.owner}</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Reputación: {data.reputation} ⭐</div>
            </div>
          </div>

          <div>
            <span style={{ 
              display: 'inline-block',
              padding: '0.25rem 0.75rem', 
              borderRadius: '1rem', 
              fontSize: '0.75rem', 
              fontWeight: 'bold', 
              backgroundColor: 'var(--color-green-100)', 
              color: '#047857',
              marginBottom: '0.75rem'
            }}>
              {data.label}
            </span>
            <h4 style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>{data.title}</h4>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: '1.6' }}>
              {data.description}
            </p>
          </div>

          <div style={{ marginTop: 'auto', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ color: 'var(--text-secondary)', fontWeight: '500' }}>{type === 'subasta' ? 'Busca a cambio:' : 'Costo:'}</span>
            <span style={{ fontWeight: 'bold', color: 'var(--accent-primary)', fontSize: '1.125rem' }}>{data.price}</span>
          </div>
        </div>

      </div>
    </div>
  );
};

export default MakeRequest;
