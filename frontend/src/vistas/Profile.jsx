import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppContext } from '../App';
import { ConfirmContext, useConfirm } from '../contextos/ConfirmContext';
import { Star, ShieldCheck, Edit2, Trash2 } from 'lucide-react';

const Profile = () => {
  const { user, controladorPerfil, controladorSubasta, controladorGamificacion } = useContext(AppContext);
  const { confirm } = useConfirm();
  const navigate = useNavigate();
  
  const [userProfile, setUserProfile] = useState(null);
  const [sentRequests, setSentRequests] = useState([]);
  const [myAuctions, setMyAuctions] = useState([]);
  const [transacciones, setTransacciones] = useState([]);
  const [logros, setLogros] = useState([]);
  const [loading, setLoading] = useState(true);
  const [usersMap, setUsersMap] = useState({});
  const [pubsMap, setPubsMap] = useState({});
  const [alertMessage, setAlertMessage] = useState('');

  // Modal Reporte
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportData, setReportData] = useState({ idPublicacion: '', idUsuarioInvolucrado: '', descripcionProblema: '', fotosEvidenciaBase64: [], esDemandante: false });

  // Mapa de incidencias por transacción
  const [incidenciasMap, setIncidenciasMap] = useState({});

  // Modal Apelar Reporte (defensa)
  const [appealModalOpen, setAppealModalOpen] = useState(false);
  const [appealData, setAppealData] = useState({ idTransaccion: '', descripcion: '', fotosEvidenciaBase64: [] });

  // Modal Cancelar Reporte
  const [cancelReportModalOpen, setCancelReportModalOpen] = useState(false);
  const [cancelReportData, setCancelReportData] = useState({ idTransaccion: '', motivo: '' });

  // Modal Edición
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editType, setEditType] = useState(''); // 'oferta', 'necesidad', 'subasta'
  const [editData, setEditData] = useState({ idInstancia: '', descripcion: '', precio: 0, titulo: '' });


  const loadTransactionsAndIncidencias = async () => {
  if (!user?.id) return []; // Retornar un array vacío si no hay usuario
  const transResponse = await fetch(`http://localhost:8080/api/transacciones`);
  let userTrans = [];
  if (transResponse.ok) {
    const transData = await transResponse.json();
    userTrans = transData.filter(t =>
      (t.idDemandante === user.id || t.idOfertante === user.id) &&
      t.estado !== 'RECHAZADA' && t.estado !== 'CANCELADA'
    );
    setTransacciones(userTrans);
  }
  
  const incMap = {};
  if (userTrans && userTrans.length > 0) {
    for (const tx of userTrans) {
      if (tx.estado === 'EN_DISPUTA') {
        try {
          const incRes = await fetch(`http://localhost:8080/api/transacciones/${tx.idTransaccion}/incidencia`);
          if (incRes.ok) {
            const inc = await incRes.json();
            incMap[tx.idTransaccion] = inc;
          }
        } catch (e) { /* ignore */ }
      }
    }
  }
  setIncidenciasMap(incMap);
  
  return userTrans; // <--- AGREGAMOS ESTE RETURN para poder usarlo en el useEffect
};

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

      // Guardamos las transacciones retornadas en una variable accesible aquí
      const activeTransactions = await loadTransactionsAndIncidencias(); 

      if (controladorGamificacion) {
        const logrosData = await controladorGamificacion.obtenerLogros(user.id);
        setLogros(logrosData || []);
      }

      try {
        const ids = new Set();
        // Usamos activeTransactions en lugar de la variable inexistente userTrans
        if (activeTransactions) { 
          activeTransactions.forEach(t => { ids.add(t.idDemandante); ids.add(t.idOfertante); });
        }
        if (auctionsData) {
          auctionsData.forEach(a => {
            if (a.propuestas) a.propuestas.forEach(p => ids.add(p.idPostor));
          });
        }
        if (sentData) {
          sentData.forEach(s => {
            ids.add(s.idUsuarioDefensor);
            ids.add(s.idUsuarioReportante);
            if (s.idReceptor) ids.add(s.idReceptor);
          });
        }
        
        const uMap = {};
        for (const uId of ids) {
           if (uId) {
              const p = await controladorPerfil.obtenerDatosPerfil(uId);
              uMap[uId] = p.nombre || uId;
           }
        }
        setUsersMap(uMap);
        
        const resPubs = await fetch('http://localhost:8080/api/publicaciones');
        if (resPubs.ok) {
           const pubsData = await resPubs.json();
           const pMap = {};
           pubsData.forEach(p => pMap[p.idPublicacion] = p.nombreServicio);
           setPubsMap(pMap);
        }
      } catch (e) {
        console.error("Error fetching auxiliary data", e);
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
    const isConfirmed = await confirm("Cancelar Solicitud", "¿Estás seguro que deseas cancelar esta solicitud? Los créditos serán devueltos a tu monedero.");
    if (!isConfirmed) return;
    try {
      const res = await fetch(`http://localhost:8080/api/solicitudes/${idSolicitud}/cancelar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idUsuario: user.id })
      });
      if (res.ok) {
        setAlertMessage("Solicitud cancelada con éxito.");
        const sentData = await controladorPerfil.obtenerSolicitudesEnviadas(user.id);
        setSentRequests(sentData);
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al cancelar la solicitud");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleAdjudicar = async (oferta) => {
    const isConfirmed = await confirm("Adjudicar Ganador", "¿Confirmas adjudicar este postor como el ganador? Esta acción no se puede deshacer.");
    if (!isConfirmed) return;
    try {
      const res = await controladorSubasta.adjudicarGanador(oferta.idSubasta, oferta.idPropuesta);
      if (res) {
        setAlertMessage("Ganador adjudicado con éxito. Subasta concluida.");
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);
      } else {
        setAlertMessage("Error al adjudicar la subasta");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleReportarIncidencia = async (e) => {
    e.preventDefault();
    if (reportData.descripcionProblema.length < 20) {
      setAlertMessage("La descripción del incidente debe tener al menos 20 caracteres.");
      return;
    }

    try {
      const fotos = reportData.fotosEvidenciaBase64.length > 0 ? reportData.fotosEvidenciaBase64 : [];
      const res = await fetch(`http://localhost:8080/api/transacciones/${reportData.idPublicacion}/reportar-incidencia`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idUsuario: user.id,
          descripcion: reportData.descripcionProblema,
          urlEvidencia: fotos.length > 0 ? fotos[0] : "N/A",
          fotosEvidencia: fotos
        })
      });
      if (res.ok) {
        setAlertMessage("Incidencia reportada con éxito. Nuestro equipo la revisará.");
        setReportModalOpen(false);
        setReportData({ idPublicacion: '', idUsuarioInvolucrado: '', descripcionProblema: '', fotosEvidenciaBase64: [], esDemandante: false });
        await loadTransactionsAndIncidencias();
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al reportar");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    const promises = files.map(file => new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (ev) => resolve(ev.target.result);
      reader.readAsDataURL(file);
    }));
    Promise.all(promises).then(base64Files => {
      setReportData({ ...reportData, fotosEvidenciaBase64: [...reportData.fotosEvidenciaBase64, ...base64Files] });
    });
  };

  const removeFile = (index) => {
    const updated = reportData.fotosEvidenciaBase64.filter((_, i) => i !== index);
    setReportData({ ...reportData, fotosEvidenciaBase64: updated });
  };

  const handleAppealSubmit = async (e) => {
    e.preventDefault();
    if (appealData.descripcion.length < 20) {
      setAlertMessage("La descripción de la apelación debe tener al menos 20 caracteres.");
      return;
    }
    try {
      const fotos = appealData.fotosEvidenciaBase64.length > 0 ? appealData.fotosEvidenciaBase64 : [];
      const res = await fetch(`http://localhost:8080/api/transacciones/${appealData.idTransaccion}/defender-incidencia`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idUsuario: user.id,
          descripcion: appealData.descripcion,
          urlEvidencia: fotos.length > 0 ? fotos[0] : "N/A",
          fotosEvidencia: fotos
        })
      });
      if (res.ok) {
        setAlertMessage("Apelación enviada con éxito. El administrador revisará tu defensa.");
        setAppealModalOpen(false);
        setAppealData({ idTransaccion: '', descripcion: '', fotosEvidenciaBase64: [] });
        await loadTransactionsAndIncidencias();
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al enviar apelación");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleAppealFileChange = (e) => {
    const files = Array.from(e.target.files);
    const promises = files.map(file => new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (ev) => resolve(ev.target.result);
      reader.readAsDataURL(file);
    }));
    Promise.all(promises).then(base64Files => {
      setAppealData({ ...appealData, fotosEvidenciaBase64: [...appealData.fotosEvidenciaBase64, ...base64Files] });
    });
  };

  const removeAppealFile = (index) => {
    const updated = appealData.fotosEvidenciaBase64.filter((_, i) => i !== index);
    setAppealData({ ...appealData, fotosEvidenciaBase64: updated });
  };

  const handleCancelarReporte = async (e) => {
    e.preventDefault();
    if (cancelReportData.motivo.length < 10) {
      setAlertMessage("Debes proporcionar una razón de al menos 10 caracteres.");
      return;
    }
    try {
      const res = await fetch(`http://localhost:8080/api/transacciones/${cancelReportData.idTransaccion}/cancelar-reporte`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idUsuario: user.id,
          descripcion: cancelReportData.motivo,
          urlEvidencia: "N/A"
        })
      });
      if (res.ok) {
        setAlertMessage("Reporte cancelado exitosamente. La contraparte ha sido notificada.");
        setCancelReportModalOpen(false);
        setCancelReportData({ idTransaccion: '', motivo: '' });
        await loadTransactionsAndIncidencias();
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al cancelar el reporte");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
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
        const token = sessionStorage.getItem('entreNosToken');
        res = await fetch(`http://localhost:8080/api/subastas/${editData.idInstancia}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', ...(token ? { 'Authorization': `Bearer ${token}` } : {}) },
          body: JSON.stringify({ descripcion: editData.descripcion })
        });
      }

      if (res.ok) {
        setAlertMessage("Publicación modificada con éxito.");
        setEditModalOpen(false);
        const data = await controladorPerfil.obtenerDatosPerfil(user.id);
        setUserProfile(data);
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);
      } else {
        const errorData = await res.json().catch(() => ({}));
        setAlertMessage(errorData.error || errorData.message || "Error al modificar");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleDelete = async (type, idInstancia) => {
    const isConfirmed = await confirm("Eliminar Publicación", "¿Estás seguro que deseas eliminar esta publicación? Esta acción no se puede deshacer.");
    if (!isConfirmed) return;
    try {
      let res;
      if (type === 'oferta') {
        res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/habilidades/${idInstancia}`, { method: 'DELETE' });
      } else if (type === 'necesidad') {
        res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/necesidades/${idInstancia}`, { method: 'DELETE' });
      } else if (type === 'subasta') {
        const token = sessionStorage.getItem('entreNosToken');
        res = await fetch(`http://localhost:8080/api/subastas/${idInstancia}`, { method: 'DELETE', headers: { ...(token ? { 'Authorization': `Bearer ${token}` } : {}) } });
      }

      if (res.ok) {
        setAlertMessage("Publicación eliminada.");
        const data = await controladorPerfil.obtenerDatosPerfil(user.id);
        setUserProfile(data);
        const auctionsData = await controladorSubasta.obtenerMisSubastas();
        setMyAuctions(auctionsData);
      } else {
        const errorData = await res.json().catch(() => ({}));
        setAlertMessage(errorData.error || errorData.message || "Error al eliminar");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
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
                      maxWidth: '160px',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      backgroundColor: auction.estado === 'ACTIVA' || auction.estado === 'ESPERANDO_DECISION' ? 'var(--color-yellow-100)' : 'var(--color-green-100)',
                      color: auction.estado === 'ACTIVA' || auction.estado === 'ESPERANDO_DECISION' ? 'var(--color-orange-600)' : 'var(--color-green-700)'
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
                            <span style={{ fontWeight: 'bold', fontSize: '0.875rem' }}>De: {usersMap[of.idPostor] || of.idPostor}</span>
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
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>A: {usersMap[req.idReceptor] || req.idReceptor} • Costo: {req.precioCreditos} cr</p>
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
                <div key={index} style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <h4 style={{ marginBottom: '0.25rem', fontSize: '1rem' }}>Tx: {tx.idTransaccion}</h4>
                      <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>Publicación: {pubsMap[tx.idPublicacion] || tx.idPublicacion} • Costo: {tx.creditosComprometidos || tx.precioFinal || 0} cr</p>
                      <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                        {tx.idDemandante === user.id ? `Tú eres el Solicitante (Contraparte: ${usersMap[tx.idOfertante] || tx.idOfertante})` : `Tú eres el Proveedor (Contraparte: ${usersMap[tx.idDemandante] || tx.idDemandante})`}
                      </p>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' }}>
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
                            const contraparteId = tx.idDemandante === user.id ? tx.idOfertante : tx.idDemandante;
                            setReportData({ 
                              ...reportData, 
                              idPublicacion: tx.idTransaccion,
                              tituloPublicacion: pubsMap[tx.idPublicacion] || tx.idPublicacion, 
                              idUsuarioInvolucrado: contraparteId,
                              nombreContraparte: usersMap[contraparteId] || contraparteId,
                              esDemandante: tx.idDemandante === user.id
                            });
                            setReportModalOpen(true);
                          }}
                        >
                          Reportar Incidencia
                        </button>
                      )}
                      {tx.estado === 'EN_DISPUTA' && (() => {
                        const inc = incidenciasMap[tx.idTransaccion];
                        const soyReportante = inc && inc.idUsuarioReportante === user.id;
                        const yaDefendi = inc && inc.idUsuarioDefensor === user.id;
                        if (soyReportante) {
                          return (
                            <button key="cancelar" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', backgroundColor: 'transparent', border: '1px solid var(--color-orange-600)', color: 'var(--color-orange-600)', borderRadius: '0.25rem', cursor: 'pointer' }}
                              onClick={() => { setCancelReportData({ idTransaccion: tx.idTransaccion, motivo: '' }); setCancelReportModalOpen(true); }}>
                              Cancelar Reporte
                            </button>
                          );
                        } else if (yaDefendi) {
                          return (
                            <span key="defendido" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', backgroundColor: 'var(--color-green-100)', color: 'var(--color-green-700)', borderRadius: '0.25rem', fontWeight: 'bold' }}>
                              Defensa Enviada
                            </span>
                          );
                        } else {
                          return (
                            <button key="apelar" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', backgroundColor: 'transparent', border: '1px solid var(--color-green-700)', color: 'var(--color-green-700)', borderRadius: '0.25rem', cursor: 'pointer' }}
                              onClick={() => { setAppealData({ idTransaccion: tx.idTransaccion, descripcion: '', fotosEvidenciaBase64: [] }); setAppealModalOpen(true); }}>
                              Apelar Reporte
                            </button>
                          );
                        }
                      })()}
                    </div>
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
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              <strong>Reportante:</strong> {user?.name || user?.nombre || 'Tú'}<br/>
              <strong>Contraparte:</strong> {reportData.nombreContraparte}<br/>
              <strong>Publicación/Transacción:</strong> {reportData.tituloPublicacion}
            </p>
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
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Evidencia visual {reportData.esDemandante && <span style={{ fontWeight: 'normal', color: 'var(--text-tertiary)' }}>(Opcional — Adjunta fotos del problema)</span>}</label>
                <input type="file" accept="image/*" multiple onChange={handleFileChange} style={{ width: '100%', padding: '0.5rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)' }} />
                {reportData.fotosEvidenciaBase64.length > 0 && (
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.5rem' }}>
                    {reportData.fotosEvidenciaBase64.map((foto, idx) => (
                      <div key={idx} style={{ position: 'relative', width: '80px', height: '80px' }}>
                        <img src={foto} alt={`Evidencia ${idx + 1}`} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '0.25rem' }} />
                        <button type="button" onClick={() => removeFile(idx)} style={{ position: 'absolute', top: '-6px', right: '-6px', width: '20px', height: '20px', borderRadius: '50%', border: 'none', backgroundColor: 'var(--color-red-600)', color: '#fff', fontSize: '0.75rem', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', lineHeight: 1 }}>×</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setReportModalOpen(false)} style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '0.75rem' }}>Enviar Reporte</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {cancelReportModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Cancelar Reporte de Incidencia</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              Al cancelar el reporte, la contraparte será notificada y podrá iniciar su propia incidencia si lo considera necesario.
            </p>
            <form onSubmit={handleCancelarReporte} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Motivo de la cancelación</label>
                <textarea 
                  value={cancelReportData.motivo}
                  onChange={(e) => setCancelReportData({ ...cancelReportData, motivo: e.target.value })}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', resize: 'vertical' }}
                  rows={4}
                  placeholder="Explica por qué deseas cancelar el reporte..."
                  required
                />
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setCancelReportModalOpen(false)} style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}>Volver</button>
                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '0.75rem' }}>Confirmar Cancelación</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {appealModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Apelar Reporte</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              La otra parte ha reportado una incidencia en esta transacción. Presenta tu defensa explicando tu versión de los hechos.
            </p>
            <form onSubmit={handleAppealSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Descripción de tu defensa</label>
                <textarea
                  value={appealData.descripcion}
                  onChange={(e) => setAppealData({ ...appealData, descripcion: e.target.value })}
                  style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', resize: 'vertical' }}
                  rows={4}
                  placeholder="Explica tu versión de lo ocurrido..."
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Evidencia visual <span style={{ fontWeight: 'normal', color: 'var(--text-tertiary)' }}>(Opcional — Adjunta fotos que respalden tu defensa)</span></label>
                <input type="file" accept="image/*" multiple onChange={handleAppealFileChange} style={{ width: '100%', padding: '0.5rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)' }} />
                {appealData.fotosEvidenciaBase64.length > 0 && (
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.5rem' }}>
                    {appealData.fotosEvidenciaBase64.map((foto, idx) => (
                      <div key={idx} style={{ position: 'relative', width: '80px', height: '80px' }}>
                        <img src={foto} alt={`Defensa ${idx + 1}`} style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '0.25rem' }} />
                        <button type="button" onClick={() => removeAppealFile(idx)} style={{ position: 'absolute', top: '-6px', right: '-6px', width: '20px', height: '20px', borderRadius: '50%', border: 'none', backgroundColor: 'var(--color-red-600)', color: '#fff', fontSize: '0.75rem', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', lineHeight: 1 }}>×</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setAppealModalOpen(false)} style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}>Cancelar</button>
                <button type="submit" className="btn-primary" style={{ flex: 1, padding: '0.75rem' }}>Enviar Defensa</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editModalOpen && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>{editType === 'subasta' ? 'Modificar Subasta' : 'Modificar Publicación'}</h3>
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

      {/* Centralized Alert Modal */}
      {alertMessage && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }}>
          <div className="card animate-in" style={{ width: '90%', maxWidth: '400px', padding: '2rem', textAlign: 'center' }}>
            <h3 style={{ marginBottom: '1rem', color: 'var(--text-primary)' }}>Aviso</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem', lineHeight: '1.5' }}>{alertMessage}</p>
            <button className="btn-primary" style={{ width: '100%', padding: '0.75rem' }} onClick={() => setAlertMessage('')}>
              Aceptar
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Profile;
