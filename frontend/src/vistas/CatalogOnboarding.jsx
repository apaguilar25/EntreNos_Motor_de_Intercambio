import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';

const CatalogOnboarding = () => {
  const navigate = useNavigate();
  const { user, setHasCatalog, setBalance, hasCatalog, controladorPerfil } = useContext(AppContext);
  const { addToast } = useContext(ToastContext);

  const [skills, setSkills] = useState({});
  const [needs, setNeeds] = useState({});

  const availableSkills = [
    { id: 'HAB-005', label: 'Soporte Técnico / Computación' },
    { id: 'HAB-001', label: 'Plomería' },
    { id: 'HAB-003', label: 'Carpintería' },
  ];

  const availableNeeds = [
    { id: 'HAB-002', label: 'Electricidad' },
    { id: 'HAB-004', label: 'Limpieza del Hogar' },
  ];

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
    });
    Object.values(needs).forEach(n => {
      if (!n.description) valid = false;
    });

    if (!valid) {
      addToast('Por favor, completa la información (Precio/Descripción) de todas las opciones marcadas.', 'error');
      return;
    }

    // Formatear para el Backend (ConfiguracionCatalogoRequestDTO)
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

      if (!response && response !== null) { // Dependiendo de lo que retorne el backend
        throw new Error("Error guardando el catálogo en el servidor.");
      }

      if (!hasCatalog) {
        setBalance(prev => prev + 20);
        addToast('¡Felicidades! Has configurado tu catálogo por primera vez y recibido 20 créditos de Capital Semilla.', 'success', '/profile');
      } else {
        addToast('Catálogo actualizado exitosamente.', 'success', '/profile');
      }
      
      setHasCatalog(true);
      navigate('/profile'); // Redirige al perfil para que vea los cambios
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

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

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
          {/* Habilidades */}
          <div>
            <h3 style={{ marginBottom: '1rem', color: 'var(--color-green-700)', backgroundColor: 'var(--color-green-100)', padding: '0.5rem', borderRadius: '0.5rem' }}>Habilidades (Lo que ofreces)</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {availableSkills.map(skill => (
                <div key={skill.id} style={{ border: '1px solid var(--border-color)', padding: '1rem', borderRadius: '0.5rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontWeight: '500' }}>
                    <input 
                      type="checkbox" 
                      checked={!!skills[skill.id]}
                      onChange={(e) => handleSkillChange(skill.id, e.target.checked)}
                    />
                    {skill.label}
                  </label>
                  
                  {skills[skill.id] && (
                    <div style={{ marginTop: '1rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', animation: 'fadeIn 0.2s ease-out' }}>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Precio (Créditos)</label>
                        <input 
                          type="number" 
                          value={skills[skill.id].price}
                          onChange={(e) => updateSkill(skill.id, 'price', e.target.value)}
                          placeholder="Ej: 50"
                          style={{ width: '100%', padding: '0.5rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)' }}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Descripción</label>
                        <textarea 
                          value={skills[skill.id].description}
                          onChange={(e) => updateSkill(skill.id, 'description', e.target.value)}
                          placeholder="Describe brevemente el servicio..."
                          rows={2}
                          style={{ width: '100%', padding: '0.5rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)', resize: 'vertical' }}
                        />
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Necesidades */}
          <div>
            <h3 style={{ marginBottom: '1rem', color: 'var(--color-orange-600)', backgroundColor: 'var(--color-yellow-100)', padding: '0.5rem', borderRadius: '0.5rem' }}>Necesidades (Lo que buscas)</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {availableNeeds.map(need => (
                <div key={need.id} style={{ border: '1px solid var(--border-color)', padding: '1rem', borderRadius: '0.5rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontWeight: '500' }}>
                    <input 
                      type="checkbox" 
                      checked={!!needs[need.id]}
                      onChange={(e) => handleNeedChange(need.id, e.target.checked)}
                    />
                    {need.label}
                  </label>
                  
                  {needs[need.id] && (
                    <div style={{ marginTop: '1rem', animation: 'fadeIn 0.2s ease-out' }}>
                      <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.25rem' }}>Condiciones / Detalles</label>
                      <textarea 
                        value={needs[need.id].description}
                        onChange={(e) => updateNeed(need.id, e.target.value)}
                        placeholder="Especifica algún requerimiento especial..."
                        rows={2}
                        style={{ width: '100%', padding: '0.5rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)', resize: 'vertical' }}
                      />
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

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
