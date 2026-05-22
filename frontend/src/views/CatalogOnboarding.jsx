import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';

const CatalogOnboarding = () => {
  const navigate = useNavigate();
  const { user, setHasCatalog, setBalance, hasCatalog } = useContext(AppContext);

  const [skills, setSkills] = useState({});
  const [needs, setNeeds] = useState({});

  const availableSkills = [
    { id: 's1', label: 'Reparación de Computadoras' },
    { id: 's2', label: 'Asesoría Legal Básica' },
    { id: 's3', label: 'Paseo de Perros' },
  ];

  const availableNeeds = [
    { id: 'n1', label: 'Mantenimiento de Aire Acondicionado' },
    { id: 'n2', label: 'Clases de Matemáticas' },
    { id: 'n3', label: 'Traducción de Textos' },
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
      alert('Por favor, completa la información (Precio/Descripción) de todas las opciones marcadas.');
      return;
    }

    // Formatear para el Backend
    const habilidadesArreglo = Object.keys(skills).map(id => {
      const label = availableSkills.find(s => s.id === id).label;
      return {
        nombre: label,
        descripcionHabilidad: skills[id].description,
        precioCreditos: parseInt(skills[id].price) || 0
      };
    });

    const necesidadesArreglo = Object.keys(needs).map(id => {
      const label = availableNeeds.find(n => n.id === id).label;
      return {
        nombre: label,
        descripcionNecesidad: needs[id].description
      };
    });

    try {
      const response = await fetch(`http://localhost:8080/api/usuarios/${user?.id || 'USR-1001'}/catalogo`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          habilidades: habilidadesArreglo,
          necesidades: necesidadesArreglo
        })
      });

      if (!response.ok) {
        throw new Error("Error guardando el catálogo en el servidor.");
      }

      if (!hasCatalog) {
        setBalance(prev => prev + 20);
        alert('¡Felicidades! Has configurado tu catálogo por primera vez y recibido 20 créditos de Capital Semilla.');
      } else {
        alert('Catálogo actualizado exitosamente.');
      }
      
      setHasCatalog(true);
      navigate('/profile'); // Redirige al perfil para que vea los cambios
    } catch (err) {
      alert(err.message);
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
