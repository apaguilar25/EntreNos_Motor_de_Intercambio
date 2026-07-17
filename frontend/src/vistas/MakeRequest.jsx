import React, { useState, useContext, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';
import { ConfirmContext, useConfirm } from '../contextos/ConfirmContext';
import { ArrowLeft, User as UserIcon, Package, MessageSquare, Coins } from 'lucide-react';

const MakeRequest = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const type = searchParams.get('type') || 'oferta'; // oferta, demanda, subasta
  const { user, balance, setBalance, controladorSubasta, controladorMuro, controladorPerfil } = useContext(AppContext);
  const { addToast } = useContext(ToastContext);
  const { confirm } = useConfirm();

  const [message, setMessage] = useState('');
  const [items, setItems] = useState('');
  const [showInterestPrompt, setShowInterestPrompt] = useState(true);
  const [estadoFisico, setEstadoFisico] = useState('NUEVO');
  const [offeredPrice, setOfferedPrice] = useState(null);
  const [priceFromCatalog, setPriceFromCatalog] = useState(null);

  // Mock JSON items
  const availableGoods = [
    { id: 1, name: 'Harina de Maíz' },
    { id: 2, name: 'Arroz' },
    { id: 3, name: 'Pasta' },
    { id: 4, name: 'Aceite' }
  ];
  const [selectedGoods, setSelectedGoods] = useState({});
  const [offerImage, setOfferImage] = useState(null);


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

  // Cargar catálogo para auto-detectar precio si ofrece servicio que ya tiene en su catálogo
  useEffect(() => {
    const loadCatalog = async () => {
      if (type !== 'necesidad' || !user?.id || !titleUrl) return;
      try {
        const perfil = await controladorPerfil.obtenerDatosPerfil(user.id);
        const habs = perfil.habilidadesOfrecidas || [];
        const match = habs.find(h =>
          h.habilidadBase?.categoria?.toLowerCase() === (titleUrl || '').toLowerCase()
        );
        if (match) {
          setPriceFromCatalog(match.precioCreditos);
          setOfferedPrice(match.precioCreditos);
        }
      } catch (e) {
        console.error('Error cargando catálogo:', e);
      }
    };
    loadCatalog();
  }, [type, user, titleUrl]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (type === 'subasta') {
      const hasSelection = Object.keys(selectedGoods).length > 0;
      let validQuantity = true;
      Object.values(selectedGoods).forEach(q => {
        if (!q || q <= 0) validQuantity = false;
      });

      if (!hasSelection) {
        addToast('Debes seleccionar al menos un bien para la puja.', 'error');
        return;
      }
      if (!validQuantity) {
        addToast('Debes indicar una cantidad válida para los bienes seleccionados.', 'error');
        return;
      }
      if (!offerImage) {
        addToast('Es obligatorio adjuntar una imagen como evidencia visual física de los productos.', 'error');
        return;
      }
      if (!message) {
        addToast('Debes escribir una descripción para tu oferta.', 'error');
        return;
      }

      try {
        const lineasPayload = Object.entries(selectedGoods).map(([goodId, qty]) => {
          const goodInfo = availableGoods.find(g => g.id === parseInt(goodId));
          return {
            cantidad: qty,
            nombre: goodInfo.name
          };
        });

        const payload = {
          idPostor: user?.id || 'USR-1002',
          idSubasta: id,
          bienesOfrecidos: lineasPayload,
          descripcion: message,
          estadoFisico: estadoFisico,
          imagenesUrls: [offerImage]
        };

        const response = await controladorSubasta.hacerOferta(id, payload);

        if (!response) {
          throw new Error('Error al enviar la oferta.');
        }

        addToast('¡Oferta enviada con éxito!', 'success', '/notifications');
        navigate(-1);
      } catch (error) {
        addToast(error.message, 'error');
      }
    } else {
      if (!message) {
        addToast('Debes escribir un mensaje.', 'error');
        return;
      }

      if (type === 'necesidad') {
        if (offeredPrice === null || offeredPrice <= 0 || !Number.isInteger(Number(offeredPrice))) {
          addToast('Debes indicar el precio que deseas cobrar por tu servicio.', 'error');
          return;
        }
      } else {
        // Validación de insolvencia solo para ofertas (el solicitante paga)
        if (balance < cost) {
          addToast(`Error: Fondos insuficientes. Tienes ${balance} cr pero la solicitud cuesta ${cost} cr.`, 'error');
          return;
        }
      }

      if (showInterestPrompt) {
        try {
          const profile = await controladorPerfil.obtenerDatosPerfil(user.id);
          const currentCategory = data.title;
          
          // Verificar si la categoría ya está en intereses, ofertas o necesidades
          const inOfertas = profile?.ofertas?.some(o => o.habilidadBase?.categoria === currentCategory || currentCategory.includes(o.habilidadBase?.categoria));
          const inNecesidades = profile?.necesidades?.some(n => n.necesidadBase?.categoria === currentCategory || currentCategory.includes(n.necesidadBase?.categoria));
          const inIntereses = profile?.intereses?.includes(currentCategory);

          if (!inOfertas && !inNecesidades && !inIntereses) {
            const addInterest = await confirm("Añadir Interés", "¿Deseas añadir esta categoría a tus intereses para recibir mejores sugerencias?");
            if (addInterest) {
              addToast("Categoría añadida a tus intereses.", 'info');
            }
          }
        } catch (error) {
          console.error("Error validando el catálogo del usuario:", error);
        }
      }

      // Enviar solicitud al backend
      try {
        const idUsuario = user?.id || 'user-123';
        const precioFinal = type === 'necesidad' ? offeredPrice : undefined;
        const response = await controladorMuro.solicitarServicio(id, idUsuario, precioFinal);

        if (!response || response.error) {
          throw new Error(response?.error || 'Error al enviar la solicitud.');
        }

        // Descontar saldo localmente solo para ofertas (el solicitante paga)
        if (type !== 'necesidad' && cost > 0) {
          setBalance(prev => prev - cost);
        }

        addToast('¡Solicitud enviada con éxito!', 'success', '/notifications');
        navigate(-1);
      } catch (error) {
        addToast(error.message, 'error');
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
            <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'var(--accent-primary)', color: 'var(--text-on-accent)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <UserIcon size={20} />
            </div>
            <div>
              <div style={{ fontWeight: 'bold' }}>{user?.name || 'Usuario Actual'}</div>
              <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Reputación: {user?.reputacion?.toFixed(1) || '5.0'} ⭐</div>
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
                            min="0" step="1"
                            value={selectedGoods[good.id]}
                            onChange={(e) => {
                              const v = parseInt(e.target.value, 10);
                              setSelectedGoods({...selectedGoods, [good.id]: v >= 0 ? v : 0});
                            }}
                            style={{ width: '60px', padding: '0.25rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)', outline: 'none' }}
                          />
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Imagen de la Oferta (Obligatorio)</label>
                  <label 
                    style={{ border: offerImage ? '2px solid var(--accent-primary)' : '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '1rem', textAlign: 'center', color: offerImage ? 'var(--accent-primary)' : 'var(--text-tertiary)', cursor: 'pointer', backgroundColor: offerImage ? 'var(--bg-secondary)' : 'transparent', display: 'block' }}
                  >
                    <input 
                      type="file" 
                      accept="image/*" 
                      onChange={(e) => {
                        const file = e.target.files[0];
                        if (file) {
                          const reader = new FileReader();
                          reader.onloadend = () => setOfferImage(reader.result);
                          reader.readAsDataURL(file);
                        } else {
                          setOfferImage(null);
                        }
                      }}
                      style={{ display: 'none' }}
                    />
                    <p>{offerImage ? 'Imagen adjuntada (Cambiar)' : 'Click para subir imagen'}</p>
                  </label>
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Estado Físico de los Bienes</label>
                  <select 
                    value={estadoFisico} 
                    onChange={(e) => setEstadoFisico(e.target.value)}
                    style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', outline: 'none' }}
                  >
                    <option value="NUEVO">Nuevo</option>
                    <option value="USADO">Usado</option>
                    <option value="REPARADO">Reparado</option>
                  </select>
                </div>

                <div>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', fontWeight: '500' }}>
                    <MessageSquare size={16} /> Descripción de tu Oferta
                  </label>
                  <textarea 
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Describe los detalles de los productos, caducidad o condiciones de entrega..."
                    rows={3}
                    style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none', resize: 'vertical' }}
                  />
                </div>
              </>
            ) : (
              <>
                {type === 'necesidad' && (
                  <div>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', fontWeight: '500' }}>
                      <Coins size={16} /> Tus créditos a cobrar
                    </label>
                    {priceFromCatalog !== null && (
                      <span style={{
                        display: 'inline-flex', alignItems: 'center', gap: '0.35rem',
                        fontSize: '0.72rem', fontWeight: '600',
                        color: 'var(--accent-primary)',
                        backgroundColor: 'rgba(var(--accent-primary-rgb, 59,130,246), 0.08)',
                        border: '1px solid var(--accent-primary)',
                        borderRadius: '1rem', padding: '0.2rem 0.6rem',
                        marginBottom: '0.4rem'
                      }}>
                        ✓ Precio sugerido de tu catálogo — puedes modificarlo
                      </span>
                    )}
                    <input 
                      type="number" min="0" step="1"
                      value={offeredPrice ?? ''}
                      onChange={e => {
                        const v = parseInt(e.target.value, 10);
                        setOfferedPrice(v >= 0 ? v : 0);
                      }}
                      placeholder="Ej: 5"
                      style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none', boxSizing: 'border-box' }}
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
              </>
            )}

            <button type="submit" className="btn-primary" style={{ marginTop: '1rem', padding: '0.75rem' }}>
              {type === 'subasta' ? 'Enviar Puja' : 'Enviar Solicitud'}
            </button>
          </form>
        </div>

        {/* Right Column: Post Summary */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', backgroundColor: 'var(--bg-secondary)', border: '1px dashed var(--border-color)' }}>
          <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>{type === 'subasta' ? 'Resumen de la Subasta' : 'Resumen de la Publicación'}</h3>
          
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

          {(type === 'subasta' || cost > 0) && (
            <div style={{ marginTop: 'auto', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-secondary)', fontWeight: '500' }}>{type === 'subasta' ? 'Busca a cambio:' : 'Costo:'}</span>
              <span style={{ fontWeight: 'bold', color: 'var(--accent-primary)', fontSize: '1.125rem' }}>{type === 'subasta' ? data.price : `${cost} cr`}</span>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};

export default MakeRequest;
