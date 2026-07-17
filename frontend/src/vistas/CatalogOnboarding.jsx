import React, { useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';

const CatalogOnboarding = () => {
  const navigate = useNavigate();
  const { user, setHasCatalog, setBalance, hasCatalog, controladorPerfil } = useContext(AppContext);
  const { addToast } = useContext(ToastContext);

  const [categorias, setCategorias] = useState([]);
  const [skills, setSkills] = useState({});
  const [needs, setNeeds] = useState({});
  const [loadingCat, setLoadingCat] = useState(true);

  useEffect(() => {
    const fetchCategorias = async () => {
      try {
        const res = await fetch('http://localhost:8080/api/habilidades');
        if (res.ok) {
          const data = await res.json();
          setCategorias(data);
        }
      } catch (e) {
        console.error('Error al cargar categorías:', e);
      } finally {
        setLoadingCat(false);
      }
    };
    fetchCategorias();
  }, []);

  const handleSkillChange = (id, checked) => {
    if (checked) {
      setSkills(prev => ({ ...prev, [id]: { price: '', description: '' } }));
    } else {
      setSkills(prev => {
        const newSkills = { ...prev };
        delete newSkills[id];
        return newSkills;
      });
    }
  };

  const handleNeedChange = (id, checked) => {
    if (checked) {
      setNeeds(prev => ({ ...prev, [id]: { description: '' } }));
    } else {
      setNeeds(prev => {
        const newNeeds = { ...prev };
        delete newNeeds[id];
        return newNeeds;
      });
    }
  };

  const updateSkill = (id, field, value) => {
    setSkills(prev => ({
      ...prev,
      [id]: { ...prev[id], [field]: value }
    }));
  };

  const updateNeed = (id, value) => {
    setNeeds(prev => ({
      ...prev,
      [id]: { description: value }
    }));
  };

  const handleSave = async () => {
    let valid = true;
    Object.values(skills).forEach(s => {
      if (!s.price || !s.description) valid = false;
      else {
        const p = Number(s.price);
        if (!Number.isInteger(p) || p <= 0) valid = false;
      }
    });
    Object.values(needs).forEach(n => {
      if (!n.description) valid = false;
    });

    if (!valid) {
      addToast('Por favor, completa la información de todas las opciones marcadas y asegúrate de que el precio sea un entero positivo.', 'error');
      return;
    }

    const ofertasArreglo = Object.keys(skills).map(id => {
      return {
        idHabilidadCategoria: id,
        descripcionServicio: skills[id].description,
        precioCreditos: parseInt(skills[id].price) || 0
      };
    });

    const necesidadesArreglo = Object.keys(needs).map(id => {
      return {
        idHabilidadCategoria: id,
        descripcionCondiciones: needs[id].description
      };
    });

    try {
      const response = await controladorPerfil.actualizarCatalogo(user?.id || 'USR-1001', {
        ofertas: ofertasArreglo,
        necesidades: necesidadesArreglo
      });

      if (!hasCatalog) {
        setBalance(prev => prev + 20);
        addToast('¡Felicidades! Has configurado tu catálogo por primera vez y recibido 20 créditos de Capital Semilla.', 'success', '/profile');
      } else {
        addToast('Catálogo actualizado exitosamente.', 'success', '/profile');
      }

      setHasCatalog(true);
      navigate('/profile');
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  if (loadingCat) {
    return (
      <div className="animate-in" style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 1rem', textAlign: 'center' }}>
        <p style={{ color: 'var(--text-tertiary)' }}>Cargando categorías disponibles...</p>
      </div>
    );
  }

  return (
    <div className="animate-in" style={{ maxWidth: '800px', margin: '2rem auto', padding: '0 1rem' }}>
      <div className="card">
        <h2 style={{ marginBottom: '0.5rem' }}>{hasCatalog ? 'Editar Catálogo' : 'Configura tu Catálogo de Servicios'}</h2>
        {!hasCatalog && (
          <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
            Selecciona las habilidades que puedes ofrecer a la comunidad y las necesidades que requieres.
            <strong> ¡Al completar tu catálogo por primera vez recibirás 20 créditos como capital semilla!</strong>
          </p>
        )}

        {categorias.length === 0 ? (
          <p style={{ color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '2rem' }}>
            No hay categorías disponibles en este momento.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {categorias.map(cat => {
              const isSelected = !!skills[cat.id] || !!needs[cat.id];
              const isHovered = false;
              return (
              <div key={cat.id} className="interactive-card" style={{
                border: isSelected ? '2px solid var(--color-green-700)' : '1px solid var(--border-color)',
                padding: '1rem', borderRadius: '0.5rem',
                transition: 'border-color 0.2s, box-shadow 0.2s',
                cursor: 'default',
                boxShadow: isSelected ? '0 0 0 1px rgba(4, 120, 87, 0.1)' : 'none'
              }}
              onMouseEnter={(e) => { if (!isSelected) e.currentTarget.style.borderColor = 'var(--color-green-100)'; }}
              onMouseLeave={(e) => { if (!isSelected) e.currentTarget.style.borderColor = 'var(--border-color)'; }}
              >
                <div style={{ fontWeight: 'bold', fontSize: '1rem', marginBottom: '0.75rem', color: 'var(--text-primary)' }}>{cat.categoria}</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {/* Ofrecer */}
                  <div style={{
                    padding: '0.5rem', borderRadius: '0.375rem',
                    backgroundColor: skills[cat.id] ? 'var(--color-green-50)' : 'transparent',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => { if (!skills[cat.id]) e.currentTarget.style.backgroundColor = 'var(--color-green-50)'; }}
                  onMouseLeave={(e) => { if (!skills[cat.id]) e.currentTarget.style.backgroundColor = 'transparent'; }}
                  >
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={!!skills[cat.id]}
                        onChange={(e) => handleSkillChange(cat.id, e.target.checked)}
                        style={{ accentColor: 'var(--color-green-700)', width: '16px', height: '16px', cursor: 'pointer' }}
                      />
                      <span style={{ color: 'var(--color-green-700)', fontWeight: '600', fontSize: '0.9rem' }}>Ofrecer este servicio</span>
                    </label>
                    {skills[cat.id] && (
                      <div style={{ marginTop: '0.75rem', marginLeft: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.5rem', animation: 'fadeIn 0.2s ease-out' }}>
                        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                          <label style={{ fontSize: '0.875rem', whiteSpace: 'nowrap', fontWeight: '500' }}>Precio (Créditos):</label>
                          <input
                            type="number" min="0" step="1"
                            value={skills[cat.id].price}
                            onChange={(e) => {
                              const v = parseInt(e.target.value, 10);
                              updateSkill(cat.id, 'price', v >= 0 ? v : '');
                            }}
                            placeholder="Ej: 50"
                            style={{ width: '120px', padding: '0.4rem 0.6rem', borderRadius: '0.375rem', border: '1px solid var(--color-green-100)', outline: 'none', fontSize: '0.875rem', transition: 'border-color 0.2s, box-shadow 0.2s' }}
                            onFocus={(e) => { e.target.style.borderColor = 'var(--color-green-700)'; e.target.style.boxShadow = '0 0 0 2px rgba(4, 120, 87, 0.2)'; }}
                            onBlur={(e) => { e.target.style.borderColor = 'var(--color-green-100)'; e.target.style.boxShadow = 'none'; }}
                          />
                        </div>
                        <textarea
                          value={skills[cat.id].description}
                          onChange={(e) => updateSkill(cat.id, 'description', e.target.value)}
                          placeholder="Describe brevemente el servicio que ofreces..."
                          rows={2}
                          style={{ width: '100%', padding: '0.5rem 0.6rem', borderRadius: '0.375rem', border: '1px solid var(--color-green-100)', outline: 'none', fontSize: '0.875rem', resize: 'vertical', transition: 'border-color 0.2s, box-shadow 0.2s' }}
                          onFocus={(e) => { e.target.style.borderColor = 'var(--color-green-700)'; e.target.style.boxShadow = '0 0 0 2px rgba(4, 120, 87, 0.2)'; }}
                          onBlur={(e) => { e.target.style.borderColor = 'var(--color-green-100)'; e.target.style.boxShadow = 'none'; }}
                        />
                      </div>
                    )}
                  </div>
                  {/* Solicitar */}
                  <div style={{
                    padding: '0.5rem', borderRadius: '0.375rem',
                    backgroundColor: needs[cat.id] ? 'var(--color-green-50)' : 'transparent',
                    transition: 'background-color 0.2s'
                  }}
                  onMouseEnter={(e) => { if (!needs[cat.id]) e.currentTarget.style.backgroundColor = 'var(--color-green-50)'; }}
                  onMouseLeave={(e) => { if (!needs[cat.id]) e.currentTarget.style.backgroundColor = 'transparent'; }}
                  >
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={!!needs[cat.id]}
                        onChange={(e) => handleNeedChange(cat.id, e.target.checked)}
                        style={{ accentColor: 'var(--color-green-700)', width: '16px', height: '16px', cursor: 'pointer' }}
                      />
                      <span style={{ color: 'var(--color-green-700)', fontWeight: '600', fontSize: '0.9rem' }}>Necesito este servicio</span>
                    </label>
                    {needs[cat.id] && (
                      <div style={{ marginTop: '0.75rem', marginLeft: '1.5rem', animation: 'fadeIn 0.2s ease-out' }}>
                        <textarea
                          value={needs[cat.id].description}
                          onChange={(e) => updateNeed(cat.id, e.target.value)}
                          placeholder="Describe qué necesitas..."
                          rows={2}
                          style={{ width: '100%', padding: '0.5rem 0.6rem', borderRadius: '0.375rem', border: '1px solid var(--color-green-100)', outline: 'none', fontSize: '0.875rem', resize: 'vertical', transition: 'border-color 0.2s, box-shadow 0.2s' }}
                          onFocus={(e) => { e.target.style.borderColor = 'var(--color-green-700)'; e.target.style.boxShadow = '0 0 0 2px rgba(4, 120, 87, 0.2)'; }}
                          onBlur={(e) => { e.target.style.borderColor = 'var(--color-green-100)'; e.target.style.boxShadow = 'none'; }}
                        />
                      </div>
                    )}
                  </div>
                </div>
              </div>
              );
            })}
          </div>
        )}

        <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
          {!hasCatalog ? (
            <button
              style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontWeight: '500' }}
              onClick={() => navigate('/')}
            >
              Más tarde
            </button>
          ) : (
            <button
              style={{ background: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-primary)', padding: '0.5rem 1rem', borderRadius: '0.5rem', cursor: 'pointer' }}
              onClick={() => navigate(-1)}
            >
              Cancelar
            </button>
          )}
          <button className="btn-primary" onClick={handleSave}>
            Guardar Catálogo
          </button>
        </div>
      </div>
    </div>
  );
};

export default CatalogOnboarding;
