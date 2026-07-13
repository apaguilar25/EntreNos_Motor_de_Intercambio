import React, { createContext, useState, useCallback, useContext } from 'react';

export const ConfirmContext = createContext();

export const ConfirmProvider = ({ children }) => {
  const [modalData, setModalData] = useState({
    isOpen: false,
    title: '',
    message: '',
    resolve: null
  });

  const confirm = useCallback((title, message) => {
    return new Promise((resolve) => {
      setModalData({
        isOpen: true,
        title,
        message,
        resolve
      });
    });
  }, []);

  const handleConfirm = () => {
    if (modalData.resolve) modalData.resolve(true);
    setModalData({ isOpen: false, title: '', message: '', resolve: null });
  };

  const handleCancel = () => {
    if (modalData.resolve) modalData.resolve(false);
    setModalData({ isOpen: false, title: '', message: '', resolve: null });
  };

  return (
    <ConfirmContext.Provider value={{ confirm }}>
      {children}
      {modalData.isOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1200 }}>
          <div className="animate-in" style={{ backgroundColor: 'var(--bg-primary)', padding: '2rem', borderRadius: '1rem', width: '90%', maxWidth: '400px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>
            <h3 style={{ marginTop: 0, color: 'var(--color-green-700)', marginBottom: '0.5rem', fontSize: '1.25rem' }}>{modalData.title}</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '0.95rem', lineHeight: '1.5' }}>{modalData.message}</p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
              <button 
                className="btn-primary" 
                style={{ backgroundColor: 'var(--color-green-100)', color: 'var(--color-green-700)' }} 
                onClick={handleCancel}
              >
                Cancelar
              </button>
              <button 
                className="btn-primary" 
                style={{ backgroundColor: 'var(--color-green-700)', color: '#fff' }} 
                onClick={handleConfirm}
              >
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
    </ConfirmContext.Provider>
  );
};

export const useConfirm = () => {
  const context = useContext(ConfirmContext);
  if (!context) {
    throw new Error('useConfirm debe ser usado dentro de un ConfirmProvider');
  }
  return context;
};
