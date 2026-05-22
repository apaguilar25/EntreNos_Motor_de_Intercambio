import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { Star, ShieldCheck, FileText } from 'lucide-react';

const Profile = () => {
  const { user } = useContext(AppContext);
  const navigate = useNavigate();
  
  const [userProfile, setUserProfile] = useState(null);
  const [sentRequests, setSentRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      if (!user?.id) return;
      try {
        setLoading(true);
        const res = await fetch(`http://localhost:8080/api/usuarios/${user.id}`);
        if (res.ok) {
          const data = await res.json();
          setUserProfile(data);
        }
        
        const sentResponse = await fetch(`http://localhost:8080/api/solicitudes/enviadas/${user.id}`);
        if (sentResponse.ok) {
          const sentData = await sentResponse.json();
          setSentRequests(sentData);
        }
      } catch (err) {
        console.error("Error cargando perfil", err);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user]);

  const handleCancelRequest = async (idSolicitud) => {
    if (!window.confirm("¿Estás seguro que deseas cancelar esta solicitud? Los créditos serán devueltos a tu monedero.")) return;
    try {
      const res = await fetch(`http://localhost:8080/api/solicitudes/cancelar/${idSolicitud}`, {
        method: 'PUT'
      });
      if (res.ok) {
        alert("Solicitud cancelada con éxito.");
        // Refetch sent requests to update UI
        const sentResponse = await fetch(`http://localhost:8080/api/solicitudes/enviadas/${user.id}`);
        if (sentResponse.ok) {
          const sentData = await sentResponse.json();
          setSentRequests(sentData);
        }
      } else {
        const errorData = await res.json();
        alert(errorData.error || "Error al cancelar la solicitud");
      }
    } catch (err) {
      alert("Error de conexión");
    }
  };

  return (
    <div className="animate-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2>Mi Perfil y Catálogo</h2>
        <button 
          className="btn-primary" 
          onClick={() => navigate('/onboarding')}
        >
          Editar Catálogo
        </button>
      </div>

      {/* Tarjeta de Identidad */}
      <div className="card" style={{ display: 'flex', gap: '2rem', alignItems: 'center', marginBottom: '2rem' }}>
        <div style={{ 
          width: '100px', 
          height: '100px', 
          borderRadius: '50%', 
          backgroundColor: 'var(--accent-primary)',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '2.5rem',
          fontWeight: 'bold'
        }}>
          {user?.name?.charAt(0).toUpperCase() || 'U'}
        </div>
        
        <div style={{ flex: 1 }}>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            {user?.name || 'Usuario'}
            <ShieldCheck size={20} color="var(--accent-primary)" />
          </h3>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>{user?.email || 'correo@plazaalameda.com'}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontWeight: 'bold' }}>
              <Star size={16} fill="currentColor" /> {userProfile?.reputacionHistorica || 5.0}
            </span>
            <span style={{ color: 'var(--text-tertiary)', fontSize: '0.875rem' }}>Reputación de Comunidad</span>
          </div>
        </div>
      </div>

      <div className="responsive-grid">
        {/* Catálogo de Ofertas */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Ofertas</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {loading ? (
              <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando...</p>
            ) : (!userProfile?.habilidades || userProfile.habilidades.length === 0) ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No has registrado habilidades.
              </div>
            ) : (
              userProfile.habilidades.map((hab, idx) => (
                <div 
                  key={idx}
                  className="interactive-card" 
                  style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}
                >
                  <div style={{ fontWeight: '600' }}>{hab.nombre}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{hab.descripcionHabilidad}</div>
                  <div style={{ color: 'var(--accent-primary)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>{hab.precioCreditos} cr</div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Catálogo de Necesidades */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Necesidades</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {loading ? (
               <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando...</p>
            ) : (!userProfile?.necesidades || userProfile.necesidades.length === 0) ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No hay necesidades registradas.
              </div>
            ) : (
              userProfile.necesidades.map((nec, idx) => (
                <div 
                  key={idx}
                  className="interactive-card" 
                  style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem' }}
                >
                  <div style={{ fontWeight: '600' }}>{nec.nombre}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{nec.descripcionNecesidad}</div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Catálogo de Subastas */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Subastas</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
              Módulo en desarrollo por Backend. No hay subastas conectadas.
            </div>
          </div>
        </div>

        {/* Historial de Ofertas Enviadas (HU5) */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Ofertas Enviadas</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {sentRequests.length === 0 ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No tienes ofertas activas.
              </div>
            ) : (
              sentRequests.map((req, index) => (
                <div key={index} style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h4 style={{ marginBottom: '0.25rem', fontSize: '1rem' }}>{req.nombreServicio}</h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>A: {req.idReceptor} • Costo: {req.precioCreditos} cr</p>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span style={{ 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '1rem', 
                      fontSize: '0.75rem', 
                      fontWeight: 'bold',
                      backgroundColor: req.estado === 'PENDIENTE' ? 'var(--color-yellow-100)' : req.estado === 'ACEPTADA' ? 'var(--color-green-100)' : 'var(--color-red-100)',
                      color: req.estado === 'PENDIENTE' ? 'var(--color-orange-600)' : req.estado === 'ACEPTADA' ? 'var(--color-green-700)' : 'var(--color-red-600)'
                    }}>
                      {req.estado}
                    </span>
                    {req.estado === 'PENDIENTE' && (
                      <button 
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', backgroundColor: 'transparent', border: '1px solid var(--color-red-600)', color: 'var(--color-red-600)', borderRadius: '0.25rem', cursor: 'pointer' }}
                        onClick={() => handleCancelRequest(req.idSolicitudIntercambio)}
                      >
                        Cancelar
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
