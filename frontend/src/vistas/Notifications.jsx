import React, { useState } from 'react';
import { Filter, Bell } from 'lucide-react';

const Notifications = () => {
  const [filter, setFilter] = useState('Todas');

  return (
    <div className="animate-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Notificaciones</h2>
        
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Filter size={18} color="var(--text-secondary)" />
          <select 
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            style={{ 
              padding: '0.5rem', 
              borderRadius: '0.5rem', 
              border: '1px solid var(--border-color)', 
              backgroundColor: 'var(--bg-secondary)', 
              color: 'var(--text-primary)',
              outline: 'none',
              cursor: 'pointer'
            }}
          >
            <option value="Todas">Todas</option>
            <option value="Oferta">Ofertas</option>
            <option value="Demanda">Demandas</option>
            <option value="Subasta">Subastas</option>
          </select>
        </div>
      </div>

      <div className="card" style={{ padding: '3rem 1rem', textAlign: 'center', color: 'var(--text-tertiary)' }}>
        <Bell size={48} style={{ margin: '0 auto 1rem', opacity: 0.5 }} />
        <h3 style={{ marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>No hay notificaciones</h3>
        <p style={{ fontSize: '0.875rem' }}>
          Aún no tienes notificaciones de {filter.toLowerCase() === 'todas' ? 'ningún tipo' : filter.toLowerCase() + 's'}. 
          Aquí aparecerán las solicitudes y actualizaciones de tus publicaciones.
        </p>
      </div>
    </div>
  );
};

export default Notifications;
