import React, { useState, useEffect, useContext } from 'react';
import { Filter, Bell, Trash2, Check, X } from 'lucide-react';
import Pagination from '../componentes/ui/Pagination';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';
import { ConfirmContext, useConfirm } from '../contextos/ConfirmContext';

const Notifications = () => {
  const { user, controladorNotificacion, controladorSubasta } = useContext(AppContext);
  const { addToast } = useContext(ToastContext);
  const { confirm } = useConfirm();
  const [filter, setFilter] = useState('Todas');
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    setCurrentPage(1);
  }, [filter]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    if (!user) return;
    try {
      setLoading(true);
      const data = await controladorNotificacion.obtenerNotificaciones(user.id);
      // Sort by newest first
      setNotifications(data.sort((a, b) => b.fechaCreacion - a.fechaCreacion));
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [user]);

  const handleDelete = async (idNotificacion) => {
    const isConfirmed = await confirm('Eliminar Notificación', '¿Estás seguro de que deseas eliminar esta notificación?');
    if (isConfirmed) {
      const success = await controladorNotificacion.eliminarNotificacion(idNotificacion);
      if (success) {
        setNotifications(prev => prev.filter(n => n.idNotificacion !== idNotificacion));
      }
    }
  };

  const handleAceptarCancelacion = async (notif) => {
    try {
      // Logic for accepting cancellation
      addToast('Cancelación aceptada exitosamente. Se han devuelto los créditos.', 'success');
      setNotifications(prev => prev.filter(n => n.id !== notif.id));
    } catch (error) {
      addToast('Error al aceptar cancelación', 'error');
    }
  };

  const handleRechazarCancelacion = (notif) => {
    // We navigate to profile or open report modal directly
    addToast('Cancelación rechazada. Puedes reportar incidencia desde tu perfil.', 'info');
    setNotifications(prev => prev.filter(n => n.id !== notif.id));
  };

  const handleResponderSolicitud = async (notificacion, aceptar) => {
    try {
      await controladorNotificacion.responderSolicitud(
        notificacion.idReferencia, // idSolicitud
        user.id, // idUsuario
        aceptar
      );
      // Remove notification after responding
      await controladorNotificacion.eliminarNotificacion(notificacion.idNotificacion);
      fetchNotifications();
    } catch (error) {
      addToast('Error al responder a la solicitud. Revisa si tienes saldo o si la publicación sigue vigente.', 'error');
    }
  };

  const filteredNotifications = notifications.filter(n => {
    if (filter === 'Todas') return true;
    if (filter === 'Solicitudes') return n.tipo === 'NUEVA_SOLICITUD_ENTRANTE';
    if (filter === 'Subastas') return n.tipo.includes('SUBASTA');
    if (filter === 'Sistema') return ['SANCION_APLICADA', 'SANCION_LEVANTADA'].includes(n.tipo);
    return true;
  });

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
            <option value="Solicitudes">Solicitudes</option>
            <option value="Subastas">Subastas</option>
            <option value="Sistema">Sistema</option>
          </select>
        </div>
      </div>

      {loading ? (
        <p style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>Cargando...</p>
      ) : filteredNotifications.length === 0 ? (
        <div className="card" style={{ padding: '3rem 1rem', textAlign: 'center', color: 'var(--text-tertiary)' }}>
          <Bell size={48} style={{ margin: '0 auto 1rem', opacity: 0.5 }} />
          <h3 style={{ marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>No hay notificaciones</h3>
          <p style={{ fontSize: '0.875rem' }}>
            Aún no tienes notificaciones de {filter.toLowerCase() === 'todas' ? 'ningún tipo' : filter.toLowerCase()}.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <>
            <Pagination currentPage={currentPage} totalItems={filteredNotifications.length} pageSize={5} onPageChange={setCurrentPage} />
            {filteredNotifications.slice((currentPage - 1) * 5, currentPage * 5).map((notif) => (
            <div key={notif.idNotificacion} className="card" style={{ padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ flex: 1 }}>
                <p style={{ margin: '0 0 0.5rem 0', fontWeight: '500' }}>{notif.mensaje}</p>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)', backgroundColor: 'var(--bg-tertiary)', padding: '0.2rem 0.5rem', borderRadius: '1rem' }}>
                    {notif.tipo.replace(/_/g, ' ')}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>
                    {new Date(notif.fechaCreacion).toLocaleString()}
                  </span>
                </div>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', marginLeft: '1rem' }}>
                                {notif.tipo === 'CANCELACION_TRANSACCION_SOLICITADA' && (
                  <>
                    <button 
                      className="btn btn-primary" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                      onClick={() => handleAceptarCancelacion(notif)}
                      title="Aceptar Cancelación"
                    >
                      Aceptar
                    </button>
                    <button 
                      className="btn" 
                      style={{ padding: '0.5rem 1rem', fontSize: '0.875rem', backgroundColor: 'var(--color-red-600)', color: '#fff' }}
                      onClick={() => handleRechazarCancelacion(notif)}
                      title="Rechazar y Reportar"
                    >
                      Rechazar
                    </button>
                  </>
                )}

                {notif.tipo === 'NUEVA_SOLICITUD_ENTRANTE' && (
                  <>
                    <button 
                      className="btn btn-primary" 
                      style={{ padding: '0.5rem', borderRadius: '50%', minWidth: '40px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                      onClick={() => handleResponderSolicitud(notif, true)}
                      title="Aceptar Solicitud"
                    >
                      <Check size={18} />
                    </button>
                    <button 
                      className="btn" 
                      style={{ padding: '0.5rem', borderRadius: '50%', minWidth: '40px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid var(--error-color)', color: 'var(--error-color)' }}
                      onClick={() => handleResponderSolicitud(notif, false)}
                      title="Rechazar Solicitud"
                    >
                      <X size={18} />
                    </button>
                  </>
                )}
                <button 
                  style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: 'var(--text-tertiary)', padding: '0.5rem' }}
                  onClick={() => handleDelete(notif.idNotificacion)}
                  title="Eliminar notificación"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            </div>
          ))}
          </>
        </div>
      )}
    </div>
  );
};

export default Notifications;
