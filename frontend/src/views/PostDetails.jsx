import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, User as UserIcon, MessageSquare, CheckCircle, XCircle } from 'lucide-react';

const PostDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  
  // Mock requests data
  const [requests, setRequests] = useState([
    { id: 101, user: 'María Pérez', reputation: 4.8, message: 'Me interesa mucho, tengo disponibilidad mañana en la tarde.', status: 'pending' },
    { id: 102, user: 'Carlos Sánchez', reputation: 4.2, message: 'Puedo hacerlo, pero cobraría 5 cr adicionales por transporte.', status: 'pending' }
  ]);

  const acceptRequest = (reqId) => {
    setRequests(requests.map(r => r.id === reqId ? { ...r, status: 'accepted' } : { ...r, status: 'rejected' }));
  };

  const rejectRequest = (reqId) => {
    setRequests(requests.map(r => r.id === reqId ? { ...r, status: 'rejected' } : r));
  };

  return (
    <div className="animate-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <button 
          onClick={() => navigate(-1)} 
          style={{ background: 'transparent', display: 'flex', alignItems: 'center', color: 'var(--text-secondary)' }}
        >
          <ArrowLeft size={24} />
        </button>
        <h2>Detalles de la Publicación #{id}</h2>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
        {/* Post Info Section */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="card">
            <h3 style={{ marginBottom: '1rem' }}>Asesoría Legal Básica</h3>
            <span style={{ 
              display: 'inline-block',
              padding: '0.25rem 0.75rem', 
              borderRadius: '1rem', 
              fontSize: '0.75rem', 
              fontWeight: 'bold', 
              backgroundColor: 'var(--color-green-100)', 
              color: '#047857',
              marginBottom: '1rem'
            }}>
              Oferta Activa
            </span>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem', lineHeight: '1.6' }}>
              Revisión de contratos, asesoramiento en redacción de documentos legales simples y consultas generales sobre derechos laborales. 
              Garantizo respuesta en menos de 24 horas y revisión exhaustiva.
            </p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
              <span style={{ color: 'var(--text-secondary)' }}>Precio fijado:</span>
              <span style={{ fontWeight: 'bold', color: 'var(--accent-primary)', fontSize: '1.25rem' }}>40 cr</span>
            </div>
          </div>
        </div>

        {/* Interested Users Section */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <h3 style={{ fontSize: '1.25rem' }}>Interesados y Solicitudes</h3>
          
          {requests.map((req) => (
            <div key={req.id} className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: 'var(--bg-tertiary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-secondary)' }}>
                    <UserIcon size={16} />
                  </div>
                  <div>
                    <div style={{ fontWeight: '600', fontSize: '0.875rem' }}>{req.user}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>Reputación: {req.reputation} ⭐</div>
                  </div>
                </div>
                {req.status === 'accepted' && <span style={{ color: '#047857', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.875rem', fontWeight: '500' }}><CheckCircle size={16} /> Aceptado</span>}
                {req.status === 'rejected' && <span style={{ color: 'var(--text-tertiary)', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.875rem' }}><XCircle size={16} /> Rechazado</span>}
              </div>
              
              <div style={{ backgroundColor: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: '0.5rem', fontSize: '0.875rem', color: 'var(--text-primary)', marginBottom: '1rem', display: 'flex', gap: '0.5rem', alignItems: 'flex-start' }}>
                <MessageSquare size={16} style={{ marginTop: '0.125rem', flexShrink: 0, color: 'var(--text-tertiary)' }} />
                <span>{req.message}</span>
              </div>

              {req.status === 'pending' && (
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button 
                    onClick={() => acceptRequest(req.id)}
                    className="btn-primary" 
                    style={{ flex: 1, padding: '0.5rem', fontSize: '0.875rem' }}
                  >
                    Aceptar
                  </button>
                  <button 
                    onClick={() => rejectRequest(req.id)}
                    style={{ flex: 1, padding: '0.5rem', fontSize: '0.875rem', background: 'transparent', border: '1px solid var(--border-color)', borderRadius: '0.5rem', color: 'var(--text-secondary)' }}
                  >
                    Rechazar
                  </button>
                </div>
              )}
            </div>
          ))}
          
          {requests.length === 0 && (
            <div className="card" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-tertiary)' }}>
              Aún no hay interesados en esta publicación.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PostDetails;
