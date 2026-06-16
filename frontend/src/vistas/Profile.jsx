import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { Star, ShieldCheck, Edit2, Trash2 } from 'lucide-react';

const Profile = () => {
  const { user, controladorPerfil, controladorSubasta, controladorGamificacion } = useContext(AppContext);
  const navigate = useNavigate();
  
  const [userProfile, setUserProfile] = useState(null);
  const [sentRequests, setSentRequests] = useState([]);
  const [myAuctions, setMyAuctions] = useState([]);
  const [transacciones, setTransacciones] = useState([]);
  const [logros, setLogros] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal Reporte
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportData, setReportData] = useState({ idPublicacion: '', idUsuarioInvolucrado: '', descripcionProblema: '', fotosEvidenciaBase64: [] });

  // Modal Edición
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editType, setEditType] = useState(''); // 'oferta', 'necesidad', 'subasta'
  const [editData, setEditData] = useState({ idInstancia: '', descripcion: '', precio: 0, titulo: '' });


  useEffect(() => {
    const fetchProfile = async () => {
      if (!user?.id) return;
      try {
        setLoading(true);
        const data = await controladorPerfil.obtenerDatosPerfil(user.id);
        setUserProfile(data);
        
        const sentData = await controladorPerfil.obtenerSolicitudesEnviadas(user.id);
        setSentRequests(sentData);
        
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);

        const transResponse = await fetch(`http://localhost:8080/api/transacciones`);
        if (transResponse.ok) {
          const transData = await transResponse.json();
          const userTrans = transData.filter(t => t.idDemandante === user.id || t.idOfertante === user.id);
          setTransacciones(userTrans);
        }

        if (controladorGamificacion) {
          const logrosData = await controladorGamificacion.obtenerLogros(user.id);
          setLogros(logrosData || []);
        }
      } catch (err) {
        console.error("Error cargando perfil", err);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user, controladorPerfil, controladorSubasta]);

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

  const handleAdjudicar = async (oferta) => {
    if (!window.confirm("¿Confirmas adjudicar este postor como el ganador? Esta acción no se puede deshacer.")) return;
    try {
      const res = await controladorSubasta.adjudicarGanador(oferta.idSubasta, oferta.idPropuesta);
      if (res) {
        alert("Ganador adjudicado con éxito. Subasta concluida.");
        // Refetch
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);
      } else {
        alert("Error al adjudicar la subasta");
      }
    } catch (err) {
      alert("Error de conexión");
    }
  };

  const handleReportarIncidencia = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch(`http://localhost:8080/api/transacciones/${reportData.idPublicacion}/reportar-incidencia`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idUsuario: user.id,
          descripcion: reportData.descripcionProblema,
          urlEvidencia: "N/A"
        })
      });
      if (res.ok) {
        alert("Incidencia reportada con éxito. Nuestro equipo la revisará.");
        setReportModalOpen(false);
        setReportData({ idPublicacion: '', idUsuarioInvolucrado: '', descripcionProblema: '', fotosEvidenciaBase64: [] });
      } else {
        const errorData = await res.json();
        alert(errorData.error || "Error al reportar");
      }
    } catch (err) {
      alert("Error de conexión");
    }
  };

  const handleEditClick = (type, item) => {
    setEditType(type);
    if (type === 'oferta') {
      setEditData({ idInstancia: item.idInstancia, descripcion: item.descripcionServicio, precio: item.precioCreditos, titulo: item.habilidadBase?.categoria });
    } else if (type === 'necesidad') {
      setEditData({ idInstancia: item.idInstancia, descripcion: item.descripcionCondiciones, precio: 0, titulo: item.necesidadBase?.categoria });
    } else if (type === 'subasta') {
      setEditData({ idInstancia: item.id, descripcion: item.descripcion, precio: 0, titulo: item.nombreActivo });
    }
    setEditModalOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    try {
      let res;
      if (editType === 'oferta') {
        res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/habilidades`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ idInstancia: editData.idInstancia, precioCreditos: editData.precio, descripcionServicio: editData.descripcion })
        });
      } else if (editType === 'necesidad') {
        res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/necesidades`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ idInstancia: editData.idInstancia, descripcionCondiciones: editData.descripcion })
        });
      } else if (editType === 'subasta') {
        res = await fetch(`http://localhost:8080/api/subastas/${editData.idInstancia}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ descripcion: editData.descripcion })
        });
      }

      if (res.ok) {
        alert("Publicación modificada con éxito.");
        setEditModalOpen(false);
        const data = await controladorPerfil.obtenerDatosPerfil(user.id);
        setUserProfile(data);
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);
      } else {
        const errorData = await res.json().catch(() => ({}));
        alert(errorData.error || errorData.message || "Error al modificar");
      }
    } catch (err) {
      alert("Error de conexión");
    }
  };

  const handleDelete = async (type, idInstancia) => {
    if (!window.confirm("¿Estás seguro que deseas eliminar esta publicación? Esta acción no se puede deshacer.")) return;
    try {
      let res;
      if (type === 'oferta') {
        res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/habilidades/${idInstancia}`, { method: 'DELETE' });
      } else if (type === 'necesidad') {
        res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/necesidades/${idInstancia}`, { method: 'DELETE' });
      } else if (type === 'subasta') {
        res = await fetch(`http://localhost:8080/api/subastas/${idInstancia}`, { method: 'DELETE' });
      }

      if (res.ok) {
        alert("Publicación eliminada.");
        const data = await controladorPerfil.obtenerDatosPerfil(user.id);
        setUserProfile(data);
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);
      } else {
        const errorData = await res.json().catch(() => ({}));
        alert(errorData.error || errorData.message || "Error al eliminar");
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
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontWeight: 'bold' }}>
              <Star size={16} fill="currentColor" /> {userProfile?.reputacionHistorica || 5.0}
            </span>
            <span style={{ color: 'var(--text-tertiary)', fontSize: '0.875rem' }}>Reputación de Comunidad</span>
          </div>

          {/* Medallas/Logros */}
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            {logros.length === 0 ? (
              <span style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)' }}>Aún no has desbloqueado medallas.</span>
            ) : (
              logros.map((l, i) => (
                <div key={i} title={l.descripcionLogro} style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', backgroundColor: 'var(--color-yellow-100)', color: 'var(--color-orange-600)', padding: '0.25rem 0.5rem', borderRadius: '1rem', fontSize: '0.75rem', fontWeight: 'bold' }}>
                  <Star size={12} fill="currentColor" /> {l.nombre}
                </div>
              ))
            )}
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
            ) : (!userProfile?.ofertas || userProfile.ofertas.length === 0) ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No has registrado habilidades.
              </div>
            ) : (
              userProfile.ofertas.map((hab, idx) => (
                <div 
                  key={idx}
                  className="interactive-card" 
                  style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', position: 'relative' }}
                >
                  <div style={{ position: 'absolute', top: '0.5rem', right: '0.5rem', display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => handleEditClick('oferta', hab)} style={{ background: 'transparent', border: 'none', color: 'var(--text-tertiary)', cursor: 'pointer' }}><Edit2 size={16} /></button>
                    <button onClick={() => handleDelete('oferta', hab.idInstancia)} style={{ background: 'transparent', border: 'none', color: 'var(--color-red-600)', cursor: 'pointer' }}><Trash2 size={16} /></button>
                  </div>
                  <div style={{ fontWeight: '600', paddingRight: '3rem' }}>{hab.habilidadBase?.categoria || 'Servicio'}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{hab.descripcionServicio}</div>
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
                  style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', position: 'relative' }}
                >
                  <div style={{ position: 'absolute', top: '0.5rem', right: '0.5rem', display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => handleEditClick('necesidad', nec)} style={{ background: 'transparent', border: 'none', color: 'var(--text-tertiary)', cursor: 'pointer' }}><Edit2 size={16} /></button>
                    <button onClick={() => handleDelete('necesidad', nec.idInstancia)} style={{ background: 'transparent', border: 'none', color: 'var(--color-red-600)', cursor: 'pointer' }}><Trash2 size={16} /></button>
                  </div>
                  <div style={{ fontWeight: '600', paddingRight: '3rem' }}>{nec.necesidadBase?.categoria || 'Necesidad'}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{nec.descripcionCondiciones}</div>
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
            {loading ? (
              <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando...</p>
            ) : myAuctions.length === 0 ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No tienes subastas activas.
              </div>
            ) : (
              myAuctions.map((auction, idx) => (
                <div key={idx} className="interactive-card" style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', position: 'relative' }}>
                  <div style={{ position: 'absolute', top: '1rem', right: '1rem', display: 'flex', gap: '0.5rem' }}>
                    {auction.estado !== 'CERRADA' && (
                      <button onClick={() => handleEditClick('subasta', auction)} style={{ background: 'transparent', border: 'none', color: 'var(--text-tertiary)', cursor: 'pointer' }}><Edit2 size={16} /></button>
                    )}
                    <button onClick={() => handleDelete('subasta', auction.id)} style={{ background: 'transparent', border: 'none', color: 'var(--color-red-600)', cursor: 'pointer' }}><Trash2 size={16} /></button>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', paddingRight: '4rem' }}>
                    <h4 style={{ margin: 0 }}>{auction.nombreActivo}</h4>
                    <span style={{ 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '1rem', 
                      fontSize: '0.75rem', 
                      fontWeight: 'bold',
                      backgroundColor: auction.estado === 'ACTIVA' ? 'var(--color-yellow-100)' : 'var(--color-green-100)',
                      color: auction.estado === 'ACTIVA' ? 'var(--color-orange-600)' : 'var(--color-green-700)'
                    }}>
                      {auction.estado}
                    </span>
                  </div>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>{auction.descripcion}</p>
                  
                  {auction.propuestas && auction.propuestas.length > 0 && (
                    <div style={{ marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '0.5rem' }}>
                      <h5 style={{ fontSize: '0.875rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>Pujas Recibidas:</h5>
                      {auction.propuestas.map((of, i) => (
                        <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem', backgroundColor: 'var(--bg-secondary)', borderRadius: '0.25rem', marginBottom: '0.5rem' }}>
                          <div>
                            <span style={{ fontWeight: 'bold', fontSize: '0.875rem' }}>De: {of.idPostor}</span>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                              Bienes: {of.bienesOfrecidos?.map(b => `${b.cantidad}x ${b.nombre}`).join(', ')}
                            </div>
                          </div>
                          {(auction.estado === 'ACTIVA' || auction.estado === 'ESPERANDO_DECISION') && (
                            <button 
                              className="btn-primary" 
                              style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', backgroundColor: 'var(--color-green-700)', color: '#fff' }}
                              onClick={() => handleAdjudicar({ idSubasta: auction.id, idPropuesta: of.idPropuesta })}
                            >
                              Adjudicar
                            </button>
                          )}
                          {of.esGanadora && (
                            <span style={{ color: 'var(--color-green-700)', fontWeight: 'bold', fontSize: '0.75rem' }}>¡GANADORA!</span>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))
            )}
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
                        onClick={() => handleCancelRequest(req.idSolicitud)}
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

        {/* Historial de Transacciones */}
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.25rem' }}>Mis Transacciones Activas</h3>
          </div>
          <div className="card" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {transacciones.length === 0 ? (
              <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                No tienes transacciones activas.
              </div>
            ) : (
              transacciones.map((tx, index) => (
                <div key={index} style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <h4 style={{ marginBottom: '0.25rem', fontSize: '1rem' }}>Tx: {tx.idTransaccion}</h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Publicación: {tx.idPublicacion} • Costo: {tx.precioFinal} cr</p>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                      {tx.idDemandante === user.id ? 'Tú eres el Solicitante' : 'Tú eres el Proveedor'}
                    </p>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span style={{ 
                      padding: '0.25rem 0.5rem', 
                      borderRadius: '1rem', 
                      fontSize: '0.75rem', 
                      fontWeight: 'bold',
                      backgroundColor: tx.estado === 'PENDIENTE' || tx.estado === 'INICIADA' ? 'var(--color-yellow-100)' : tx.estado === 'FINALIZADA' ? 'var(--color-green-100)' : 'var(--color-red-100)',
                      color: tx.estado === 'PENDIENTE' || tx.estado === 'INICIADA' ? 'var(--color-orange-600)' : tx.estado === 'FINALIZADA' ? 'var(--color-green-700)' : 'var(--color-red-600)'
                    }}>
                      {tx.estado}
                    </span>
                    {(tx.estado === 'PENDIENTE' || tx.estado === 'INICIADA') && (
                      <button 
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', backgroundColor: 'transparent', border: '1px solid var(--color-orange-600)', color: 'var(--color-orange-600)', borderRadius: '0.25rem', cursor: 'pointer' }}
                        onClick={() => {
                          setReportData({ ...reportData, idPublicacion: tx.idTransaccion, idUsuarioInvolucrado: tx.idDemandante === user.id ? tx.idOfertante : tx.idDemandante });
                          setReportModalOpen(true);
                        }}
                      >
                        Reportar Problema
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {reportModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Reportar Incidencia</h3>
            <form onSubmit={handleReportarIncidencia} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Descripción del problema</label>
                <textarea 
                  value={reportData.descripcionProblema}
                  onChange={(e) => setReportData({ ...reportData, descripcionProblema: e.target.value })}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', resize: 'vertical' }}
                  rows={4}
                  placeholder="Detalla lo ocurrido durante el intercambio..."
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Evidencia visual (Opcional)</label>
                <div style={{ border: '2px dashed var(--border-color)', borderRadius: '0.5rem', padding: '2rem', textAlign: 'center', color: 'var(--text-tertiary)' }}>
                  <p>Sube fotos del problema aquí</p>
                </div>
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setReportModalOpen(false)} style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '0.75rem' }}>Enviar Reporte</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Modificar Publicación</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem', fontSize: '0.875rem' }}>
              <strong>Título/Servicio:</strong> {editData.titulo}
            </p>
            <form onSubmit={handleEditSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Descripción</label>
                <textarea 
                  value={editData.descripcion}
                  onChange={(e) => setEditData({ ...editData, descripcion: e.target.value })}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', resize: 'vertical' }}
                  rows={4}
                  required
                />
              </div>
              {editType === 'oferta' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Precio (Créditos)</label>
                  <input 
                    type="number"
                    min="1"
                    value={editData.precio}
                    onChange={(e) => setEditData({ ...editData, precio: Number(e.target.value) })}
                    style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)' }}
                    required
                  />
                </div>
              )}
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setEditModalOpen(false)} style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '0.75rem' }}>Guardar Cambios</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;
