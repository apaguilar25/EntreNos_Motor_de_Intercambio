import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Image as ImageIcon } from 'lucide-react';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';

const CreateAuction = () => {
  const navigate = useNavigate();
  const { user, controladorSubasta } = React.useContext(AppContext);
  const { addToast } = React.useContext(ToastContext);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [duration, setDuration] = useState('1');
  const [status, setStatus] = useState('NUEVO');
  const [hasPhoto, setHasPhoto] = useState(null);

  return (
    <div className="animate-in" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <button 
          onClick={() => navigate(-1)} 
          style={{ background: 'transparent', display: 'flex', alignItems: 'center', color: 'var(--text-secondary)' }}
        >
          <ArrowLeft size={24} />
        </button>
        <h2>Crear Subasta de Activo</h2>
      </div>

      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div style={{ padding: '1rem', backgroundColor: 'var(--bg-warning-soft)', color: 'var(--text-on-warning-soft)', borderRadius: '0.5rem', fontSize: '0.875rem' }}>
          <strong>Nota:</strong> Las subastas son exclusivas para el trueque de bienes físicos o de consumo, no se utilizan créditos comunitarios.
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Título del Activo</label>
          <input 
            type="text" 
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Ej: Bicicleta Montañera Rin 26"
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Descripción y Bienes Esperados</label>
          <textarea 
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Describe el estado del activo y qué tipo de bienes esperas a cambio..."
            rows={4}
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none', resize: 'vertical' }}
          />
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Estado Físico del Activo</label>
          <select 
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
          >
            <option value="NUEVO">Nuevo</option>
            <option value="USADO">Usado</option>
            <option value="REPARADO">Reparado</option>
          </select>
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Duración de la Subasta</label>
          <select 
            value={duration}
            onChange={(e) => setDuration(e.target.value)}
            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', outline: 'none' }}
          >
            <option value="1">1 Día</option>
            <option value="5">5 Días</option>
            <option value="10">10 Días</option>
            <option value="20">20 Días</option>
          </select>
        </div>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>Fotos del Activo</label>
          <label 
            style={{ border: hasPhoto ? '2px solid var(--accent-primary)' : '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '2rem', textAlign: 'center', color: hasPhoto ? 'var(--accent-primary)' : 'var(--text-tertiary)', cursor: 'pointer', backgroundColor: hasPhoto ? 'var(--bg-secondary)' : 'transparent', display: 'block' }}
          >
            <input 
              type="file" 
              accept="image/*" 
              onChange={(e) => {
                const file = e.target.files[0];
                if (file) {
                  const reader = new FileReader();
                  reader.onloadend = () => setHasPhoto(reader.result);
                  reader.readAsDataURL(file);
                } else {
                  setHasPhoto(null);
                }
              }}
              style={{ display: 'none' }}
            />
            <ImageIcon size={32} style={{ margin: '0 auto 0.5rem' }} />
            <p>{hasPhoto ? 'Foto subida correctamente (Click para cambiar)' : 'Sube al menos una foto clara del producto (Click para subir)'}</p>
          </label>
        </div>

        <div style={{ fontSize: '0.75rem', color: 'var(--color-red-600)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <strong>Advertencia:</strong> Si la subasta se cierra y no se asigna un ganador 5 días después del cierre, serás penalizado y no podrás publicar subastas durante 72h
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
            style={{ flex: 1, padding: '0.75rem', borderRadius: '0.5rem', backgroundColor: 'var(--accent-warning)', color: 'var(--text-on-warning)' }}
            onClick={async () => {
              if(!title || !description) {
                addToast("Por favor llena el título y la descripción.", "error");
                return;
              }
              if(!hasPhoto) {
                addToast("Debes subir al menos una foto del activo.", "error");
                return;
              }
              
              const payload = {
                descripcion: description,
                nombreActivo: title,
                estadoFisico: status,
                imagenesUrls: [hasPhoto],
                diasDuracion: parseInt(duration)
              };

              try {
                // We use controladorSubasta from useContext
                
                const res = await controladorSubasta.crearSubasta(payload);
                if (res) {
                  addToast('Subasta creada con éxito.', 'success', '/notifications');
                  navigate(-1);
                } else {
                  addToast('Error al crear la subasta', 'error');
                }
              } catch (error) {
                addToast('Error de red al crear subasta', 'error');
              }
            }}
          >
            Crear Subasta
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateAuction;
