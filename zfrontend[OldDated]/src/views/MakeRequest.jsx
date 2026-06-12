import React, { useState, useContext } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { AppContext } from '../App';
import { ArrowLeft, User as UserIcon, Package, MessageSquare } from 'lucide-react';

const MakeRequest = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const type = searchParams.get('type') || 'oferta'; // oferta, demanda, subasta
  const { user, balance, setBalance } = useContext(AppContext);

  const [message, setMessage] = useState('');
  const [items, setItems] = useState('');
  const [showInterestPrompt, setShowInterestPrompt] = useState(true);

  // Mock JSON items
  const availableGoods = [
    { id: 1, name: 'Harina de Maíz' },
    { id: 2, name: 'Arroz' },
    { id: 3, name: 'Pasta' },
    { id: 4, name: 'Aceite' }
  ];
  const [selectedGoods, setSelectedGoods] = useState({});
  const [offerImage, setOfferImage] = useState(false);


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

  const userIdUrl = searchParams.get('userId');
  const priceUrl = searchParams.get('price');
  const titleUrl = searchParams.get('title');
  const descUrl = searchParams.get('desc');
  const ownerUrl = searchParams.get('owner');
  const repUrl = searchParams.get('rep');

  // Si no hay datos en la URL, se usan los mock data (para subastas u otras vistas no conectadas aún)
  const data = (titleUrl && ownerUrl) ? {
    title: titleUrl,
    description: descUrl,
    owner: ownerUrl,
    reputation: repUrl || 5.0,
    price: priceUrl,
    label: type.toUpperCase(),
    userId: userIdUrl
  } : (postData[type] || postData.oferta);

  const cost = parseInt(data.price) || 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (type === 'subasta') {
      const hasSelection = Object.keys(selectedGoods).length > 0;
      let validQuantity = true;
      Object.values(selectedGoods).forEach(q => {
        if (!q || q <= 0) validQuantity = false;
      });

      if (!hasSelection) {
        alert('Debes seleccionar al menos un bien para la puja.');
        return;
      }
      if (!validQuantity) {
        alert('Debes indicar una cantidad válida para los bienes seleccionados.');
        return;
      }
      if (!offerImage) {
        alert('Es obligatorio adjuntar una imagen como evidencia visual física de los productos.');
        return;
      }

      try {
        const lineasPayload = Object.entries(selectedGoods).map(([goodId, qty]) => {
          const goodInfo = availableGoods.find(g => g.id === parseInt(goodId));
          return {
            cantidad: qty,
            bienConsumo: { nombreBienConsumo: goodInfo.name }
          };
        });

        const payload = {
          idOfertante: user?.id || 'USR-1002',
          lineas: lineasPayload
        };

        const response = await fetch(`http://localhost:8080/api/subastas/${id}/ofertar`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (!response.ok) {
          const errData = await response.text();
          throw new Error(errData || 'Error al enviar la oferta.');
        }

        alert('¡Oferta enviada con éxito!');
        navigate(-1);
      } catch (error) {
        alert(error.message);
      }
    } else {
      if (!message) {
        alert('Debes escribir un mensaje.');
        return;
      }
      
      // Validación de insolvencia (HU2)
      if (balance < cost) {
        alert(`Error: Fondos insuficientes. Tienes ${balance} cr pero la solicitud cuesta ${cost} cr.`);
        return;
      }

      if (showInterestPrompt) {
        const addInterest = window.confirm("¿Deseas añadir esta categoría a tus intereses para recibir mejores sugerencias?");
        if (addInterest) {
          alert("Categoría añadida a tus intereses.");
        }
      }

      // Enviar solicitud al backend
      try {
        const payload = {
          idEmisor: user?.id || 'user-123',
          idReceptor: data.userId || 'owner-456',
          nombreServicio: data.title,
          precioCreditos: cost,
          descripcionServicio: message
        };

        const response = await fetch('http://localhost:8080/api/solicitudes/proponer', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(payload)
        });

        if (!response.ok) {
          const errData = await response.json();
          throw new Error(errData.error || 'Error al enviar la solicitud.');
        }

        // Descontar saldo localmente para reflejar el backend
        if (cost > 0) {
          setBalance(prev => prev - cost);
        }

        alert('¡Solicitud enviada con éxito!');
        navigate(-1);
      } catch (error) {
        alert(error.message);
      }
    }
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
            {type === 'subasta' ? (
              <>
                <div>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', fontWeight: '500' }}>
                    <Package size={16} /> Bienes a Ofrecer (Selecciona y pon cantidad)
                  </label>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    {availableGoods.map(good => (
                      <div key={good.id} style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <input 
                          type="checkbox" 
                          id={`good-${good.id}`} 
                          checked={selectedGoods[good.id] !== undefined}
                          onChange={(e) => {
                            const newGoods = { ...selectedGoods };
                            if (e.target.checked) {
                              newGoods[good.id] = 1;
                            } else {
                              delete newGoods[good.id];
                            }
                            setSelectedGoods(newGoods);
                          }}
                        />
                        <label htmlFor={`good-${good.id}`} style={{ flex: 1, cursor: 'pointer' }}>{good.name}</label>
                        {selectedGoods[good.id] !== undefined && (
                          <input 
                            type="number" 
                            min="1" 
                            value={selectedGoods[good.id]}
                            onChange={(e) => setSelectedGoods({...selectedGoods, [good.id]: parseInt(e.target.value) || 0})}
                            style={{ width: '60px', padding: '0.25rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)', outline: 'none' }}
                          />
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Imagen de la Oferta (Obligatorio)</label>
                  <div 
                    onClick={() => setOfferImage(!offerImage)}
                    style={{ border: offerImage ? '2px solid var(--accent-primary)' : '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '1rem', textAlign: 'center', color: offerImage ? 'var(--accent-primary)' : 'var(--text-tertiary)', cursor: 'pointer', backgroundColor: offerImage ? 'var(--bg-secondary)' : 'transparent' }}
                  >
                    <p>{offerImage ? 'Imagen adjuntada' : 'Click para simular carga de imagen'}</p>
                  </div>
                </div>
              </>
            ) : (
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
            )}

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
