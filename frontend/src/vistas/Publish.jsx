import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Image as ImageIcon } from 'lucide-react';
import { ToastContext } from '../contextos/ToastContext';
import { useContext } from 'react';

const Publish = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialType = searchParams.get('type') || 'oferta';
  const { addToast } = useContext(ToastContext);

  const [type, setType] = useState(initialType);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [credits, setCredits] = useState('');

  return (
    <div className="animate-in" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <button 
          onClick={() => navigate(-1)} 
          style={{ background: 'transparent', display: 'flex', alignItems: 'center', color: 'var(--text-secondary)' }}
        >
          <ArrowLeft size={24} />
        </button>
        <h2>Crear Publicación</h2>
      </div>

      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Tipo de Publicación</label>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button 
              className={`btn-primary ${type === 'oferta' ? '' : 'inactive'}`} 
              onClick={() => setType('oferta')}
              style={{ flex: 1, backgroundColor: type === 'oferta' ? 'var(--color-green-700)' : 'var(--bg-tertiary)', color: type === 'oferta' ? '#fff' : 'var(--text-primary)' }}
            >
              Oferta (Talento)
            </button>
            <button 
              className={`btn-primary ${type === 'demanda' ? '' : 'inactive'}`} 
              onClick={() => setType('demanda')}
              style={{ flex: 1, backgroundColor: type === 'demanda' ? 'var(--color-orange-600)' : 'var(--bg-tertiary)', color: type === 'demanda' ? '#fff' : 'var(--text-primary)' }}
            >
              Demanda (Necesidad)
            </button>
          </div>
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Título</label>
          <input 
            type="text" 
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Ej: Reparación de Computadoras"
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Descripción</label>
          <textarea 
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Describe los detalles..."
            rows={4}
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none', resize: 'vertical' }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Créditos (cr)</label>
          <input 
            type="number" 
            value={credits}
            onChange={(e) => setCredits(e.target.value)}
            placeholder={type === 'oferta' ? 'Precio en créditos' : 'Presupuesto máximo'}
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Imagen (Opcional)</label>
          <div style={{ border: '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '2rem', textAlign: 'center', color: 'var(--text-tertiary)', cursor: 'pointer' }}>
            <ImageIcon size={32} style={{ margin: '0 auto 0.5rem' }} />
            <p>Haz clic para subir una imagen</p>
          </div>
        </div>

        <div style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
          <button 
            onClick={() => navigate(-1)} 
            style={{ flex: 1, padding: '0.75rem', borderRadius: '0.5rem', background: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-primary)', fontWeight: '500', cursor: 'pointer' }}
          >
            Cancelar
          </button>
          <button 
            className="btn-primary" 
            style={{ flex: 1, padding: '0.75rem', borderRadius: '0.5rem' }}
            onClick={() => {
              addToast('Publicación creada (Mock)', 'success');
              navigate(-1);
            }}
          >
            Crear Publicación
          </button>
        </div>
      </div>
    </div>
  );
};

export default Publish;
