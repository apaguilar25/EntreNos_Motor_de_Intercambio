import React, { useContext, useState } from 'react';
import { createPortal } from 'react-dom';
import { AppContext } from '../App';
import { Wallet as WalletIcon, ArrowUpRight, ArrowDownRight, Clock, X, Star, AlertTriangle, Image as ImageIcon, CheckCircle } from 'lucide-react';

const Wallet = () => {
  const { user, setBalance, controladorPerfil } = useContext(AppContext);
  const [selectedTx, setSelectedTx] = useState(null);
  
  // States for Modals
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [showFraudModal, setShowFraudModal] = useState(false);
  const [rating, setRating] = useState(0);

  const [transactions, setTransactions] = useState([]);
  const [availableBalance, setAvailableBalance] = useState(0);
  const [retainedBalance, setRetainedBalance] = useState(0);

  React.useEffect(() => {
    const fetchBalance = async () => {
      if (!user?.id) return;
      try {
        const monedero = await controladorPerfil.obtenerSaldo(user.id);
        setAvailableBalance(monedero.creditosDisponibles || 0);
        setRetainedBalance(monedero.creditosComprometidos || monedero.creditosRetenidos || 0);
        if (setBalance) {
            setBalance(monedero.creditosDisponibles || 0);
        }
      } catch (err) {
        console.error("Error cargando monedero:", err);
      }
    };
    fetchBalance();
  }, [user, controladorPerfil, setBalance]);



  return (
    <div className="animate-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Mi Billetera</h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '2.5rem' }}>
        {/* Saldo Disponible */}
        <div className="card" style={{ backgroundColor: 'var(--accent-primary)', color: 'var(--text-on-accent)', border: 'none' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', opacity: 0.9 }}>
            <WalletIcon size={20} />
            <span style={{ fontSize: '1.125rem', fontWeight: '500' }}>Créditos Disponibles</span>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            {availableBalance} <span style={{ fontSize: '1.5rem', fontWeight: 'normal', opacity: 0.8 }}>cr</span>
          </div>
        </div>

        {/* Saldo Retenido */}
        <div className="card" style={{ backgroundColor: 'var(--bg-warning-soft)', color: 'var(--text-on-warning-soft)', border: 'none' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', opacity: 0.9 }}>
            <Clock size={20} />
            <span style={{ fontSize: '1.125rem', fontWeight: '500' }}>Créditos Retenidos</span>
          </div>
          <div style={{ fontSize: '3rem', fontWeight: 'bold' }}>
            {retainedBalance} <span style={{ fontSize: '1.5rem', fontWeight: 'normal', opacity: 0.8 }}>cr</span>
          </div>
          <p style={{ fontSize: '0.75rem', marginTop: '0.5rem', opacity: 0.8 }}>
            Garantía para transacciones en curso (Épica 5).
          </p>
        </div>
      </div>

      <h3 style={{ marginBottom: '1rem', fontSize: '1.25rem' }}>Historial de Transacciones</h3>
      <div className="card" style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-tertiary)' }}>
        <p style={{ fontStyle: 'italic', marginBottom: '0.5rem' }}>Aún no hay transacciones en tu historial.</p>
        <p style={{ fontSize: '0.875rem' }}>Módulo en desarrollo por el equipo Backend.</p>
      </div>

      {/* Receipt Modal */}
      {selectedTx && createPortal(
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '1rem'
        }}>
          <div className="card animate-in" style={{ width: '100%', maxWidth: '400px', position: 'relative' }}>
            <button 
              onClick={() => setSelectedTx(null)}
              style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', color: 'var(--text-tertiary)' }}
            >
              <X size={20} />
            </button>
            <h3 style={{ textAlign: 'center', marginBottom: '1.5rem' }}>Recibo de Transacción</h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px dashed var(--border-color)', paddingBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-secondary)' }}>ID Transacción</span>
                <span style={{ fontWeight: '500' }}>#{selectedTx.id.toString().padStart(6, '0')}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px dashed var(--border-color)', paddingBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Concepto</span>
                <span style={{ fontWeight: '500', textAlign: 'right', maxWidth: '60%' }}>{selectedTx.concept}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px dashed var(--border-color)', paddingBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Fecha</span>
                <span style={{ fontWeight: '500' }}>{selectedTx.date}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px dashed var(--border-color)', paddingBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Monto</span>
                <span style={{ fontWeight: 'bold', color: selectedTx.type === 'ingreso' ? 'var(--accent-primary)' : 'var(--text-primary)' }}>
                  {selectedTx.type === 'ingreso' ? '+' : '-'}{selectedTx.amount} cr
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '0.5rem' }}>
                <span style={{ color: 'var(--text-secondary)' }}>Estado</span>
                <span style={{ 
                  fontWeight: 'bold', 
                  color: selectedTx.status === 'Retenido' ? 'var(--text-on-warning-soft)' : '#047857',
                  backgroundColor: selectedTx.status === 'Retenido' ? 'var(--bg-warning-soft)' : 'var(--color-green-100)',
                  padding: '0.25rem 0.5rem',
                  borderRadius: '0.25rem',
                  fontSize: '0.875rem'
                }}>
                  {selectedTx.status}
                </span>
              </div>
            </div>
            
            
            {selectedTx.status === 'Retenido' && (
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
                <button 
                  className="btn-primary" 
                  style={{ flex: 1, padding: '0.5rem', backgroundColor: '#047857' }}
                  onClick={() => {
                    setShowRatingModal(true);
                  }}
                >
                  <CheckCircle size={16} style={{ display: 'inline', marginRight: '0.25rem' }} /> Finalizar
                </button>
                <button 
                  style={{ flex: 1, padding: '0.5rem', backgroundColor: 'transparent', border: '1px solid var(--color-red-600)', color: 'var(--color-red-600)', borderRadius: '0.5rem', cursor: 'pointer', fontWeight: 'bold' }}
                  onClick={() => {
                    setShowFraudModal(true);
                  }}
                >
                  <AlertTriangle size={16} style={{ display: 'inline', marginRight: '0.25rem' }} /> Reportar
                </button>
              </div>
            )}
            
            <button className="btn-primary" style={{ width: '100%', marginTop: '1rem' }} onClick={() => setSelectedTx(null)}>
              Cerrar
            </button>
          </div>
        </div>,
        document.body
      )}

      {/* Rating Modal */}
      {showRatingModal && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 110, padding: '1rem' }}>
          <div className="card animate-in" style={{ width: '100%', maxWidth: '350px', textAlign: 'center' }}>
            <h3 style={{ marginBottom: '1rem' }}>Califica el Servicio</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>¿Cómo fue tu experiencia con esta transacción?</p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginBottom: '2rem' }}>
              {[1,2,3,4,5].map(star => (
                <Star 
                  key={star} 
                  size={32} 
                  fill={star <= rating ? 'var(--color-orange-600)' : 'transparent'} 
                  color={star <= rating ? 'var(--color-orange-600)' : 'var(--text-tertiary)'} 
                  style={{ cursor: 'pointer' }}
                  onClick={() => setRating(star)}
                />
              ))}
            </div>
            <button 
              className="btn-primary" 
              style={{ width: '100%' }}
              onClick={() => {
                setTransactions(transactions.map(t => t.id === selectedTx.id ? { ...t, status: 'Completado' } : t));
                setShowRatingModal(false);
                setSelectedTx(null);
                setRating(0);
                // Aquí simularíamos abrir el pop-up de medalla desbloqueada si aplicara
                alert("¡Gracias por calificar! Se ha liberado el pago.");
              }}
              disabled={rating === 0}
            >
              Enviar Calificación
            </button>
          </div>
        </div>,
        document.body
      )}

      {/* Fraud Modal */}
      {showFraudModal && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 110, padding: '1rem' }}>
          <div className="card animate-in" style={{ width: '100%', maxWidth: '400px', position: 'relative' }}>
            <button onClick={() => setShowFraudModal(false)} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', color: 'var(--text-tertiary)' }}>
              <X size={20} />
            </button>
            <h3 style={{ marginBottom: '1rem', color: 'var(--color-red-600)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <AlertTriangle size={20} /> Reportar Incidencia
            </h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem', fontSize: '0.875rem' }}>Describe el problema y proporciona evidencia. La transacción quedará retenida bajo revisión.</p>
            
            <textarea 
              placeholder="Detalles del problema..."
              rows={3}
              style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)', color: 'var(--text-primary)', marginBottom: '1rem', resize: 'vertical' }}
            />
            
            <div style={{ border: '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '1.5rem', textAlign: 'center', color: 'var(--text-tertiary)', cursor: 'pointer', marginBottom: '1.5rem' }}>
              <ImageIcon size={24} style={{ margin: '0 auto 0.5rem' }} />
              <p style={{ fontSize: '0.875rem' }}>Subir foto de evidencia</p>
            </div>

            <button 
              className="btn-primary" 
              style={{ width: '100%', backgroundColor: 'var(--color-red-600)' }}
              onClick={() => {
                setTransactions(transactions.map(t => t.id === selectedTx.id ? { ...t, status: 'Bajo Revisión' } : t));
                setShowFraudModal(false);
                setSelectedTx(null);
                alert("Incidencia reportada. El equipo de soporte lo revisará pronto.");
              }}
            >
              Enviar Reporte
            </button>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
};

export default Wallet;
