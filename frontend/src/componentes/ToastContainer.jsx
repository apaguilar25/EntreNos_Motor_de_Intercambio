import React, { useContext } from 'react';
import { ToastContext } from '../contextos/ToastContext';
import { X } from 'lucide-react';
import { Link } from 'react-router-dom';
import './ToastContainer.css';

const ToastContainer = () => {
  const { toasts, removeToast } = useContext(ToastContext);

  if (toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((toast) => {
        let toastClass = 'toast-info';
        if (toast.type === 'success') toastClass = 'toast-success';
        if (toast.type === 'error') toastClass = 'toast-error';

        return (
          <div key={toast.id} className={`toast ${toastClass}`}>
            <div className="toast-progress"></div>
            <div className="toast-content">
              <span className="toast-message">{toast.message}</span>
              {toast.linkTo && (
                <Link to={toast.linkTo} className="toast-link" onClick={() => removeToast(toast.id)}>
                  Ver Detalles
                </Link>
              )}
            </div>
            <button className="toast-close" onClick={() => removeToast(toast.id)} aria-label="Cerrar">
              <X size={16} />
            </button>
          </div>
        );
      })}
    </div>
  );
};

export default ToastContainer;
