import React, { useContext, useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { useNavigate, useLocation } from 'react-router-dom';
import { AppContext } from '../App';
import { ConfirmContext, useConfirm } from '../contextos/ConfirmContext';
import { Star, ShieldCheck, Edit2, Trash2 } from 'lucide-react';
import Pagination from '../componentes/ui/Pagination';

const Profile = () => {
  const { user, setUser, setBalance, controladorPerfil, controladorSubasta, controladorGamificacion } = useContext(AppContext);
  const { confirm } = useConfirm();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [userProfile, setUserProfile] = useState(null);

  const handleAvatarChange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = async () => {
        const base64String = reader.result;
        try {
          const res = await fetch(`http://localhost:8080/api/usuarios/${user.id}/foto`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ urlFoto: base64String })
          });
          if (res.ok) {
            setUser({ ...user, urlFotoPerfil: base64String });
            setAlertMessage('Foto de perfil actualizada con éxito.');
          } else {
            setAlertMessage('Error al actualizar la foto de perfil.');
          }
        } catch (err) {
          setAlertMessage('Error al conectar con el servidor.');
        }
      };
      reader.readAsDataURL(file);
    }
  };

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
  const [transaccionDetalleModalOpen, setTransaccionDetalleModalOpen] = useState(false);
  const [selectedTxDetalle, setSelectedTxDetalle] = useState(null);
  const [cancelTransaccionModalOpen, setCancelTransaccionModalOpen] = useState(false);
  const [cancelMotivoSeleccionado, setCancelMotivoSeleccionado] = useState('');
  const [reportData, setReportData] = useState({ idPublicacion: '', idUsuarioInvolucrado: '', descripcionProblema: '', fotosEvidenciaBase64: [], esDemandante: false });

  // Mapa de incidencias por transacción
  const [incidenciasMap, setIncidenciasMap] = useState({});

  // Modal Calificar
  const [calificarModalOpen, setCalificarModalOpen] = useState(false);
  const [calificacion, setCalificacion] = useState(0);
  const [calificacionTxId, setCalificacionTxId] = useState(null);
  const [calificacionTx, setCalificacionTx] = useState(null);
  const [transaccionesYaCalificadas, setTransaccionesYaCalificadas] = useState(() => {
    try {
      const saved = localStorage.getItem('txYaCalificadas');
      return saved ? new Set(JSON.parse(saved)) : new Set();
    } catch { return new Set(); }
  });

  const marcarYaCalificada = (txId) => {
    if (!txId) return;
    setTransaccionesYaCalificadas(prev => {
      const next = new Set(prev);
      next.add(txId);
      localStorage.setItem('txYaCalificadas', JSON.stringify([...next]));
      return next;
    });
  };

  // Modal Apelar Reporte (defensa)
  const [appealModalOpen, setAppealModalOpen] = useState(false);
  const [appealData, setAppealData] = useState({ idTransaccion: '', descripcion: '', fotosEvidenciaBase64: [] });

  // Modal Cancelar Reporte
  const [cancelReportModalOpen, setCancelReportModalOpen] = useState(false);
  const [cancelReportData, setCancelReportData] = useState({ idTransaccion: '', motivo: '' });

  // Modal Adjudicar (contacto ganador)
  const [adjudicarModalOpen, setAdjudicarModalOpen] = useState(false);
  const [adjudicarData, setAdjudicarData] = useState(null);

  // Historial de propuestas (pujas realizadas)
  const [myProposals, setMyProposals] = useState([]);

  // Modal Edición
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editType, setEditType] = useState(''); // 'oferta', 'necesidad', 'subasta'
  const [editData, setEditData] = useState({ idInstancia: '', descripcion: '', precio: 0, titulo: '' });
  const [sectionModalOpen, setSectionModalOpen] = useState(null);
  const [txFilter, setTxFilter] = useState('TODAS');
  const [sentFilter, setSentFilter] = useState('TODAS');
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    setCurrentPage(1);
  }, [sectionModalOpen, sentFilter, txFilter]);

  const handleSubmitRating = async (txId, valor) => {
    try {
      const res = await fetch(`http://localhost:8080/api/transacciones/${txId}/calificar?idUsuario=${user.id}&calificacion=${valor}`, {
        method: 'POST'
      });
      if (res.ok) {
        setAlertMessage("¡Gracias por calificar!");
        loadTransactionsAndIncidencias();
      }
    } catch (e) {}
  };

  useEffect(() => {
    if (transacciones && transacciones.length > 0 && !calificarModalOpen) {
      const unrated = transacciones.find(t =>
        t.estado === 'FINALIZADA' &&
        t.idDemandante === user.id &&
        !t.resena &&
        !transaccionesYaCalificadas.has(t.idTransaccion)
      );
      if (unrated) {
        marcarYaCalificada(unrated.idTransaccion);
        setCalificacionTxId(unrated.idTransaccion);
        setCalificacionTx(unrated);
        setCalificacion(0);
        setCalificarModalOpen(true);
      }
    }
  }, [transacciones]);

  const loadTransactionsAndIncidencias = async () => {
    if (!user?.id) return [];
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
    if (userTrans) {
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
    return userTrans;
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

        const proposalsData = await controladorSubasta.obtenerHistorialPropuestas();
        setMyProposals(proposalsData);

        const fetchedUserTrans = await loadTransactionsAndIncidencias();

        if (controladorGamificacion) {
          const logrosData = await controladorGamificacion.obtenerLogros(user.id);
          setLogros(logrosData || []);
        }

        try {
          const resPubs = await fetch('http://localhost:8080/api/publicaciones');
          let pMap = {};
          if (resPubs.ok) {
             const pubsData = await resPubs.json();
             pubsData.forEach(p => pMap[p.idPublicacion] = p);
             setPubsMap(pMap);
          }

          // Construir mapa de usuarios directamente desde publicaciones (ya incluye nombreUsuario).
          // Esto evita llamadas a /api/usuarios/{id} que generan errores 404 en el navegador,
          // ya que el browser siempre registra errores HTTP de red aunque el JS los capture.
          const uMap = {};
          Object.values(pMap).forEach(p => {
            if (p.idUsuario && p.nombreUsuario) {
              uMap[p.idUsuario] = p.nombreUsuario;
            }
          });
          // Nota: No se hacen llamadas extra a /api/usuarios/{id} para ningún ID adicional.
          // Si un ID no está en la lista de publicaciones, se mostrará "Usuario" como fallback.

          setUsersMap(uMap);
        } catch (e) {
          console.error('Error fetching auxiliary data', e);
        }
      } catch (err) {
        console.error('Error cargando perfil', err);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user, controladorPerfil, controladorSubasta]);

  const handleCancelRequest = async (idSolicitud) => {
    const isConfirmed = await confirm('Cancelar Solicitud', '¿Estás seguro que deseas cancelar esta solicitud? Los créditos serán devueltos a tu monedero.');
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
        setAdjudicarData({
          ganador: res.contactoGanador,
          propietario: res.contactoPropietario
        });
        setAdjudicarModalOpen(true);
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

  const refreshSaldo = async () => {
    if (!user?.id) return;
    try {
      const monedero = await controladorPerfil.obtenerSaldo(user.id);
      setUser({ ...user, creditosDisponibles: monedero.creditosDisponibles, creditosComprometidos: monedero.creditosRetenidos });
      setBalance(monedero.creditosDisponibles);
    } catch (e) {
      console.error("Error actualizando saldo", e);
    }
  };

  const handleConfirmEntrega = async (idTransaccion) => {
    const isConfirmed = await confirm("Confirmar Entrega", "¿Estás seguro que quieres confirmar la finalización? Confirma que has entregado el servicio/producto al demandante. Esta acción no se puede deshacer.");
    if (!isConfirmed) return;
    try {
      const res = await fetch(`http://localhost:8080/api/transacciones/${idTransaccion}/confirmar-ofertante`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (res.ok) {
        const data = await res.json();
        setAlertMessage("Entrega confirmada con éxito.");
        await loadTransactionsAndIncidencias();
        await refreshSaldo();
        if (data?.transaccion?.estado === 'FINALIZADA') {
          setAlertMessage("Transacción completada. El demandante ahora puede calificar el servicio.");
        }
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al confirmar entrega");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleConfirmRecepcion = async (idTransaccion) => {
    const isConfirmed = await confirm("Confirmar Recepción", "¿Estás seguro que quieres confirmar la finalización? Confirma que has recibido el servicio/producto del ofertante. Esta acción no se puede deshacer.");
    if (!isConfirmed) return;
    try {
      const res = await fetch(`http://localhost:8080/api/transacciones/${idTransaccion}/confirmar-demandante`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (res.ok) {
        const data = await res.json();
        setAlertMessage("Recepción confirmada con éxito.");
        await loadTransactionsAndIncidencias();
        await refreshSaldo();
        if (data?.transaccion?.estado === 'FINALIZADA') {
          setCalificacionTxId(data.transaccion.idTransaccion);
          setCalificacionTx(data.transaccion);
          setCalificacion(0);
          setCalificarModalOpen(true);
        }
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al confirmar recepción");
      }
    } catch (err) {
      setAlertMessage("Error de conexión");
    }
  };

  const handleCalificar = async () => {
    if (calificacion < 1 || calificacion > 5) return;
    const txId = calificacionTxId;
    const puntuacion = calificacion;
    setCalificarModalOpen(false);
    setCalificacion(0);
    setCalificacionTxId(null);
    setCalificacionTx(null);
    marcarYaCalificada(txId);
    try {
      const res = await fetch(`http://localhost:8080/api/transacciones/${txId}/calificar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idUsuario: user.id, calificacion: puntuacion })
      });
      if (res.ok) {
        setAlertMessage("Calificación enviada con éxito. ¡Gracias por tu opinión!");
        await loadTransactionsAndIncidencias();
        await refreshSaldo();
      } else {
        const errorData = await res.json();
        setAlertMessage(errorData.error || "Error al calificar");
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
    if (editType === 'oferta') {
      const precio = Number(editData.precio);
      if (!Number.isInteger(precio) || precio <= 0) {
        addToast('El precio en créditos debe ser un número entero positivo.', 'error');
        return;
      }
    }
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
            fontWeight: 'bold',
            overflow: 'hidden',
            position: 'relative'
          }}>

            {user?.urlFotoPerfil && user.urlFotoPerfil !== 'default.png' ? (
              <img src={user.urlFotoPerfil} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            ) : (
              user?.name?.charAt(0).toUpperCase() || 'U'
            )}
            <input type="file" accept="image/*" style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', opacity: 0, cursor: 'pointer' }} onChange={handleAvatarChange} title="Cambiar foto de perfil" />

          </div>
        
        <div style={{ flex: 1 }}>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            {user?.name || 'Usuario'}
            <ShieldCheck size={20} color="var(--accent-primary)" />
          </h3>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>{user?.email || 'correo@plazaalameda.com'}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--accent-warning)', fontWeight: 'bold' }}>
              <Star size={16} fill="currentColor" /> {(userProfile?.reputacionHistorica || 5).toFixed(1)}
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
        {/* Resumen Ofertas */}
        <div className="card interactive-card" onClick={() => setSectionModalOpen('ofertas')} style={{ cursor: 'pointer' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Mis Ofertas</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{userProfile?.ofertas?.length || 0} registradas</p>
          <span style={{ color: 'var(--accent-primary)', fontSize: '0.875rem', fontWeight: 'bold' }}>Ver Detalles &rarr;</span>
        </div>

        {/* Resumen Necesidades */}
        <div className="card interactive-card" onClick={() => setSectionModalOpen('necesidades')} style={{ cursor: 'pointer' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Mis Necesidades</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{userProfile?.necesidades?.length || 0} registradas</p>
          <span style={{ color: 'var(--accent-primary)', fontSize: '0.875rem', fontWeight: 'bold' }}>Ver Detalles &rarr;</span>
        </div>
        
        {/* Resumen Subastas */}
        <div className="card interactive-card" onClick={() => setSectionModalOpen('subastas')} style={{ cursor: 'pointer' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Mis Subastas</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{myAuctions.length} activas</p>
          <span style={{ color: 'var(--accent-primary)', fontSize: '0.875rem', fontWeight: 'bold' }}>Ver Detalles &rarr;</span>
        </div>
        
        {/* Resumen Solicitudes */}
        <div className="card interactive-card" onClick={() => setSectionModalOpen('enviadas')} style={{ cursor: 'pointer' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Mis Solicitudes Enviadas</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{sentRequests.length} pendientes</p>
          <span style={{ color: 'var(--accent-primary)', fontSize: '0.875rem', fontWeight: 'bold' }}>Ver Detalles &rarr;</span>
        </div>
        
        {/* Resumen Transacciones */}
        <div className="card interactive-card" onClick={() => setSectionModalOpen('transacciones')} style={{ cursor: 'pointer' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Transacciones Activas</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{transacciones.length} en curso</p>
          <span style={{ color: 'var(--accent-primary)', fontSize: '0.875rem', fontWeight: 'bold' }}>Ver Detalles &rarr;</span>
        </div>

        {/* Resumen Pujas Realizadas */}
        <div className="card interactive-card" onClick={() => setSectionModalOpen('pujas')} style={{ cursor: 'pointer' }}>
          <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Pujas Realizadas</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{myProposals.length} registradas</p>
          <span style={{ color: 'var(--accent-primary)', fontSize: '0.875rem', fontWeight: 'bold' }}>Ver Detalles &rarr;</span>
        </div>
      </div>

      {sectionModalOpen && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 900, padding: '1rem' }}>
          <div className="card animate-in" style={{ width: '100%', maxWidth: '800px', maxHeight: '90vh', overflowY: 'auto', position: 'relative', padding: '2rem' }}>
            <button 
              onClick={() => setSectionModalOpen(null)} 
              style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-tertiary)' }}
            >
              &times;
            </button>

            {sectionModalOpen === 'ofertas' && (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2 style={{ fontSize: '1.25rem' }}>Mis Ofertas</h2>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {loading ? (
                    <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando...</p>
                  ) : (!userProfile?.ofertas || userProfile.ofertas.length === 0) ? (
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                      No has registrado habilidades.
                    </div>
                  ) : (
                    <> <Pagination currentPage={currentPage} totalItems={userProfile.ofertas.length} pageSize={5} onPageChange={setCurrentPage} /> {userProfile.ofertas.slice((currentPage - 1) * 5, currentPage * 5).map((hab, idx) => (
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
                        <div style={{ color: 'var(--accent-primary)', fontWeight: 'bold', fontSize: '0.875rem', marginTop: '0.25rem' }}>{hab.precioCreditos}</div>
                      </div>
                    ))}
                    </>
                  )}
                </div>
              </div>
            )}

            {sectionModalOpen === 'necesidades' && (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2 style={{ fontSize: '1.25rem' }}>Mis Necesidades</h2>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {loading ? (
                     <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando...</p>
                  ) : (!userProfile?.necesidades || userProfile.necesidades.length === 0) ? (
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                      No hay necesidades registradas.
                    </div>
                  ) : (
                    <> <Pagination currentPage={currentPage} totalItems={userProfile.necesidades.length} pageSize={5} onPageChange={setCurrentPage} /> {userProfile.necesidades.slice((currentPage - 1) * 5, currentPage * 5).map((nec, idx) => (
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
                    ))}
                    </>
                  )}
                </div>
              </div>
            )}

            {sectionModalOpen === 'subastas' && (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2 style={{ fontSize: '1.25rem' }}>Mis Subastas</h2>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {loading ? (
                    <p style={{ textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando...</p>
                  ) : myAuctions.length === 0 ? (
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                      No tienes subastas activas.
                    </div>
                  ) : (
                    <> <Pagination currentPage={currentPage} totalItems={myAuctions.length} pageSize={5} onPageChange={setCurrentPage} /> {myAuctions.slice((currentPage - 1) * 5, currentPage * 5).map((auction, idx) => (
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
                                <div style={{ flex: 1 }}>
                                  <span style={{ fontWeight: 'bold', fontSize: '0.875rem' }}>De: {usersMap[of.idPostor] || of.idPostor}</span>
                                  <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                                    Bienes: {of.bienesOfrecidos?.map(b => `${b.cantidad}x ${b.nombre}`).join(', ')}
                                  </div>
                                  {of.imagenesUrls && of.imagenesUrls.length > 0 && (
                                    <div style={{ display: 'flex', gap: '0.25rem', marginTop: '0.25rem' }}>
                                      {of.imagenesUrls.map((url, j) => (
                                        <img key={j} src={url} alt={`Foto puja ${j+1}`} style={{ width: '40px', height: '40px', objectFit: 'cover', borderRadius: '0.25rem', border: '1px solid var(--border-color)' }} />
                                      ))}
                                    </div>
                                  )}
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
                    ))}
                    </>
                  )}
                </div>
              </div>
            )}

            {sectionModalOpen === 'enviadas' && (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2 style={{ fontSize: '1.25rem' }}>Mis Solicitudes Enviadas</h2>
                  <select value={sentFilter} onChange={(e) => { setSentFilter(e.target.value); setCurrentPage(1); }} style={{ padding: '0.25rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)', outline: 'none', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}>
                    <option value="TODAS">Todas</option>
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="ACEPTADA">Aceptada</option>
                    <option value="RECHAZADA">Rechazada</option>
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {sentRequests.filter(req => sentFilter === 'TODAS' || req.estado === sentFilter).length === 0 ? (
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                      No tienes ofertas activas.
                    </div>
                  ) : (
                    <> <Pagination currentPage={currentPage} totalItems={sentRequests.filter(req => sentFilter === 'TODAS' || req.estado === sentFilter).length} pageSize={5} onPageChange={setCurrentPage} /> {sentRequests.filter(req => sentFilter === 'TODAS' || req.estado === sentFilter).slice((currentPage - 1) * 5, currentPage * 5).map((req, index) => (
                      <div key={index} style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <h4 style={{ marginBottom: '0.25rem', fontSize: '1rem' }}>{pubsMap[req.idPublicacion]?.nombreServicio || req.idPublicacion}</h4>
                          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>A: {usersMap[pubsMap[req.idPublicacion]?.idUsuario] || pubsMap[req.idPublicacion]?.idUsuario || 'Usuario'} • Costo: {req.precioOfertado}</p>
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
                    ))}
                    </>
                  )}
                </div>
              </div>
            )}

            {sectionModalOpen === 'transacciones' && (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2 style={{ fontSize: '1.25rem' }}>Mis Transacciones Activas</h2>
                  <select value={txFilter} onChange={(e) => { setTxFilter(e.target.value); setCurrentPage(1); }} style={{ padding: '0.25rem', borderRadius: '0.25rem', border: '1px solid var(--border-color)', outline: 'none', marginLeft: '1rem', background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}>
                    <option value="TODAS">Todas</option>
                    <option value="INICIADA">Iniciada</option>
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="FINALIZADA">Finalizada</option>
                    <option value="EN_DISPUTA">En Disputa</option>
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {transacciones.filter(tx => txFilter === 'TODAS' || tx.estado === txFilter).length === 0 ? (
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                      No tienes transacciones activas.
                    </div>
                  ) : (
                    <> <Pagination currentPage={currentPage} totalItems={transacciones.filter(tx => txFilter === 'TODAS' || tx.estado === txFilter).length} pageSize={5} onPageChange={setCurrentPage} /> {transacciones.filter(tx => txFilter === 'TODAS' || tx.estado === txFilter).slice((currentPage - 1) * 5, currentPage * 5).map((tx, index) => (
                      <div key={index} 
                        onClick={() => { setSelectedTxDetalle(tx); setTransaccionDetalleModalOpen(true); }}
                        style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1rem', cursor: 'pointer', transition: 'box-shadow 0.2s' }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <div>
                              <h4 style={{ marginBottom: '0.25rem', fontSize: '1.1rem' }}>
                                Tx: {pubsMap[tx.idPublicacion]?.nombreServicio || tx.idPublicacion}
                                <span style={{ fontWeight: 'normal', fontSize: '0.9rem', color: 'var(--text-secondary)', marginLeft: '0.5rem' }}>
                                  (Tú eres el {tx.idDemandante === user.id ? 'Solicitante' : 'Proveedor'})
                                </span>
                              </h4>
                              <p style={{ color: 'var(--text-tertiary)', fontSize: '0.875rem', marginBottom: '0' }}>
                                ID: {tx.idTransaccion.split('-')[0].toUpperCase()} | Contraparte: {usersMap[tx.idDemandante === user.id ? tx.idOfertante : tx.idDemandante] || 'Usuario'} | Costo: {tx.creditosRetenidos > 0 ? tx.creditosRetenidos : (pubsMap[tx.idPublicacion]?.precioCreditos ?? '-')} créditos
                              </p>
                            </div>
                            <div style={{ textAlign: 'right' }}>
                            <span style={{ 
                              padding: '0.25rem 0.5rem', 
                              borderRadius: '1rem', 
                              fontSize: '0.75rem', 
                              fontWeight: 'bold',
                              backgroundColor: tx.estado === 'INICIADA' || tx.estado === 'PENDIENTE' ? 'var(--color-yellow-100)' : tx.estado === 'FINALIZADA' ? 'var(--color-green-100)' : 'var(--color-red-100)',
                              color: tx.estado === 'INICIADA' || tx.estado === 'PENDIENTE' ? 'var(--color-orange-600)' : tx.estado === 'FINALIZADA' ? 'var(--color-green-700)' : 'var(--color-red-600)'
                            }}>
                              {tx.estado}
                            </span>
                          </div>
                        </div>
                      </div>
                    ))}
                    </>
                  )}
                </div>
              </div>
            )}

            {sectionModalOpen === 'pujas' && (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2 style={{ fontSize: '1.25rem' }}>Pujas Realizadas</h2>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {myProposals.length === 0 ? (
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-tertiary)', fontStyle: 'italic', textAlign: 'center', padding: '1rem 0' }}>
                      No has participado en subastas aún.
                    </div>
                  ) : (
                    myProposals.map((prop, idx) => {
                      const esRetirable = prop.estadoSubasta === 'ACTIVA';
                      return (
                        <div key={idx} style={{ border: '1px solid var(--border-color)', borderRadius: '0.5rem', padding: '1rem' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                              <h4 style={{ marginBottom: '0.25rem', fontSize: '1rem' }}>
                                {prop.nombreActivo || 'Subasta'}
                              </h4>
                              <p style={{ color: 'var(--text-tertiary)', fontSize: '0.8rem', margin: '0 0 0.25rem' }}>
                                {prop.bienesOfrecidos?.map(b => `${b.cantidad} x ${b.nombre}`).join(', ') || 'Sin bienes'}
                              </p>
                              <span style={{
                                padding: '0.15rem 0.4rem', borderRadius: '0.25rem', fontSize: '0.7rem', fontWeight: 'bold',
                                backgroundColor: prop.estadoPropuesta === 'EN_EVALUACION' ? 'var(--color-yellow-100)' : 'var(--color-green-100)',
                                color: prop.estadoPropuesta === 'EN_EVALUACION' ? 'var(--color-orange-600)' : 'var(--color-green-700)'
                              }}>
                                {prop.estadoPropuesta || 'EN_EVALUACION'}
                              </span>
                              <span style={{ marginLeft: '0.5rem', padding: '0.15rem 0.4rem', borderRadius: '0.25rem', fontSize: '0.7rem', fontWeight: 'bold', backgroundColor: 'var(--bg-secondary)', color: 'var(--text-secondary)' }}>
                                {prop.estadoSubasta}
                              </span>
                            </div>
                            {esRetirable && (
                              <button
                                onClick={async () => {
                                  const ok = await confirm("Retirar Puja", "¿Estás seguro de retirar tu puja? Esta acción no se puede deshacer.");
                                  if (!ok) return;
                                  try {
                                    const r = await controladorSubasta.retirarPropuesta(prop.idSubasta, prop.idPropuesta);
                                    if (r) {
                                      setAlertMessage("Puja retirada con éxito.");
                                      const p = await controladorSubasta.obtenerHistorialPropuestas();
                                      setMyProposals(p);
                                    } else {
                                      setAlertMessage("Error al retirar la puja.");
                                    }
                                  } catch (e) {
                                    setAlertMessage("Error de conexión.");
                                  }
                                }}
                                style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem', backgroundColor: '#dc2626', color: '#fff', border: 'none', borderRadius: '0.25rem', cursor: 'pointer', whiteSpace: 'nowrap' }}
                              >
                                Retirar Puja
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      , document.body)}

      {reportModalOpen && createPortal(
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
      , document.body)}

      {cancelReportModalOpen && createPortal(
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
      , document.body)}

      {appealModalOpen && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem' }}>
            <h3 style={{ marginBottom: '1rem' }}>Defenderse</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              El demandante ha reportado una incidencia en esta transacción. Presenta tu defensa explicando tu versión de los hechos. El administrador revisará ambos lados y tomará una decisión.
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
      , document.body)}

      {editModalOpen && createPortal(
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
                    min="0" step="1"
                    value={editData.precio}
                    onChange={(e) => {
                      const val = parseInt(e.target.value, 10);
                      if (val >= 0) setEditData({ ...editData, precio: val });
                      else if (e.target.value === '') setEditData({ ...editData, precio: '' });
                    }}
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
      , document.body)}

  {transaccionDetalleModalOpen && selectedTxDetalle && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '600px', padding: '2rem', position: 'relative' }}>
            <button onClick={() => { setTransaccionDetalleModalOpen(false); setSelectedTxDetalle(null); }} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-tertiary)' }}>&times;</button>
            <h3 style={{ marginBottom: '1rem' }}>Detalles de la Transacción</h3>
            <div style={{ marginBottom: '1.5rem' }}>
              <p><strong>Publicación:</strong> {selectedTxDetalle.publicacion?.titulo || selectedTxDetalle.idPublicacion}</p>
              <p><strong>Costo:</strong> {selectedTxDetalle.oferta?.precio || selectedTxDetalle.creditosRetenidos || 0} créditos</p>
              <p><strong>Estado:</strong> {selectedTxDetalle.estado || 'Activa'}</p>
            </div>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
              <div className="card" style={{ padding: '1rem', backgroundColor: 'var(--bg-secondary)' }}>
                <h4 style={{ marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Solicitante</h4>
                <p><strong>Nombre:</strong> {usersMap[selectedTxDetalle.idDemandante] || 'Usuario'}</p>
              </div>
              <div className="card" style={{ padding: '1rem', backgroundColor: 'var(--bg-secondary)' }}>
                <h4 style={{ marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Proveedor</h4>
                <p><strong>Nombre:</strong> {usersMap[selectedTxDetalle.idOfertante] || 'Usuario'}</p>
              </div>
            </div>

            {selectedTxDetalle.estado === 'FINALIZADA' ? (
              <>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                  Esta transacción ha sido completada. Los créditos han sido transferidos al ofertante.
                </p>
                {selectedTxDetalle.idDemandante === user.id && !selectedTxDetalle.resena && (
                  <button 
                    className="btn-primary" 
                    style={{ width: '100%', padding: '0.75rem' }}
                    onClick={() => {
                      setCalificacionTxId(selectedTxDetalle.idTransaccion);
                      setCalificacionTx(selectedTxDetalle);
                      setCalificacion(0);
                      setCalificarModalOpen(true);
                    }}
                  >
                    Calificar Servicio
                  </button>
                )}
                {selectedTxDetalle.idDemandante === user.id && selectedTxDetalle.resena && (
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                    Calificaste este servicio con {selectedTxDetalle.resena.calificacion} / 5 estrellas.
                  </p>
                )}
              </>
            ) : selectedTxDetalle.estado === 'RECHAZADA' ? (
              <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                Esta transacción fue cancelada.
              </p>
            ) : (
              <>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                  {selectedTxDetalle.estado === 'PENDIENTE' || selectedTxDetalle.estado === 'INICIADA'
                    ? 'Una vez concluido el servicio, confirma la finalización para liberar los créditos. Si ocurre algún problema, puedes reportar una incidencia o solicitar la cancelación.'
                    : 'El intercambio está en proceso.'}
                </p>

                <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                  {selectedTxDetalle.idOfertante === user.id && !selectedTxDetalle.confirmacionOfertante ? (
                    <button 
                      className="btn-primary" 
                      style={{ flex: '1 1 auto', padding: '0.75rem' }}
                      onClick={() => {
                        handleConfirmEntrega(selectedTxDetalle.idTransaccion);
                      }}
                    >
                      Confirmar Entrega
                    </button>
                  ) : selectedTxDetalle.idDemandante === user.id && !selectedTxDetalle.confirmacionDemandante ? (
                    <button 
                      className="btn-primary" 
                      style={{ flex: '1 1 auto', padding: '0.75rem' }}
                      onClick={() => {
                        handleConfirmRecepcion(selectedTxDetalle.idTransaccion);
                      }}
                    >
                      Confirmar Recepción
                    </button>
                  ) : (selectedTxDetalle.estado === 'PENDIENTE' || selectedTxDetalle.estado === 'INICIADA') ? (
                    <button 
                      className="btn-primary" 
                      style={{ flex: '1 1 auto', padding: '0.75rem', opacity: 0.6, cursor: 'not-allowed' }}
                      disabled
                    >
                      Esperando confirmación de la otra parte...
                    </button>
                  ) : null}

                  {(selectedTxDetalle.estado === 'PENDIENTE' || selectedTxDetalle.estado === 'INICIADA' || selectedTxDetalle.estado === 'EN_DISPUTA') && (
                    <button 
                      className="btn-primary" 
                      style={{ flex: '1 1 auto', padding: '0.75rem', backgroundColor: '#ea580c', color: '#fff', border: 'none' }}
                      onClick={() => {
                        const esDemandante = selectedTxDetalle.idDemandante === user.id;
                        const contraparteId = esDemandante ? selectedTxDetalle.idOfertante : selectedTxDetalle.idDemandante;
                        setReportData({
                          ...reportData,
                          idPublicacion: selectedTxDetalle.idTransaccion,
                          tituloPublicacion: pubsMap[selectedTxDetalle.idPublicacion]?.nombreServicio || selectedTxDetalle.idPublicacion,
                          idUsuarioInvolucrado: contraparteId,
                          nombreContraparte: usersMap[contraparteId] || contraparteId,
                          descripcionProblema: '',
                          fotosEvidenciaBase64: [],
                          esDemandante: esDemandante
                        });
                        setTransaccionDetalleModalOpen(false);
                        setReportModalOpen(true);
                      }}
                    >
                      Reportar Incidencia
                    </button>
                  )}

                  {(selectedTxDetalle.estado === 'PENDIENTE' || selectedTxDetalle.estado === 'INICIADA') && (
                    <button 
                      className="btn-primary" 
                      style={{ flex: '1 1 auto', padding: '0.75rem', backgroundColor: '#dc2626', color: '#fff', border: 'none' }}
                      onClick={() => {
                        setCancelTransaccionModalOpen(true);
                        setTransaccionDetalleModalOpen(false);
                      }}
                    >
                      Cancelar Transacción
                    </button>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      , document.body)}

      {calificarModalOpen && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '400px', padding: '2rem', position: 'relative', textAlign: 'center' }}>
            <button onClick={() => { setCalificarModalOpen(false); setCalificacion(0); marcarYaCalificada(calificacionTxId); }} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-tertiary)' }}>&times;</button>
            <h3 style={{ marginBottom: '1rem' }}>Calificar Servicio</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
              Transacción: <strong>{calificacionTx?.idTransaccion || calificacionTxId}</strong>
            </p>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              Proveedor: <strong>{usersMap[calificacionTx?.idOfertante] || 'Usuario'}</strong>
            </p>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              ¿Cómo calificas el servicio recibido?
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginBottom: '2rem' }}>
              {[1, 2, 3, 4, 5].map(star => (
                <button
                  key={star}
                  onClick={() => setCalificacion(star)}
                  style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: '0.25rem' }}
                >
                  <Star size={36} fill={star <= calificacion ? '#f59e0b' : 'none'} color={star <= calificacion ? '#f59e0b' : 'var(--text-tertiary)'} />
                </button>
              ))}
            </div>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button 
                type="button" 
                onClick={() => { setCalificarModalOpen(false); setCalificacion(0); marcarYaCalificada(calificacionTxId); }} 
                style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}
              >
                Cancelar
              </button>
              <button 
                type="button" 
                className="btn-primary" 
                style={{ flex: 1, padding: '0.75rem' }}
                disabled={calificacion === 0}
                onClick={handleCalificar}
              >
                Enviar Calificación
              </button>
            </div>
          </div>
        </div>
      , document.body)}

      {cancelTransaccionModalOpen && selectedTxDetalle && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px', padding: '2rem', position: 'relative' }}>
            <button onClick={() => setCancelTransaccionModalOpen(false)} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-tertiary)' }}>&times;</button>
            <h3 style={{ marginBottom: '1rem' }}>Solicitar Cancelación</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              Por favor, selecciona el motivo por el cual deseas cancelar esta transacción. Se enviará una notificación a la contraparte para que acepte o rechace (y reporte una incidencia).
            </p>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="motivoCancelacion" 
                  value="EQUIVOCACION" 
                  checked={cancelMotivoSeleccionado === 'EQUIVOCACION'}
                  onChange={(e) => setCancelMotivoSeleccionado(e.target.value)}
                  style={{ width: '1.2rem', height: '1.2rem' }}
                />
                Me equivoqué al enviar la solicitud / oferta.
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="motivoCancelacion" 
                  value="YA_NO_NECESITO" 
                  checked={cancelMotivoSeleccionado === 'YA_NO_NECESITO'}
                  onChange={(e) => setCancelMotivoSeleccionado(e.target.value)}
                  style={{ width: '1.2rem', height: '1.2rem' }}
                />
                Ya no necesito este servicio / Ya resolví mi necesidad.
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="motivoCancelacion" 
                  value="SIN_ACUERDO" 
                  checked={cancelMotivoSeleccionado === 'SIN_ACUERDO'}
                  onChange={(e) => setCancelMotivoSeleccionado(e.target.value)}
                  style={{ width: '1.2rem', height: '1.2rem' }}
                />
                No pudimos llegar a un acuerdo en el horario o los detalles.
              </label>
            </div>

            <div style={{ display: 'flex', gap: '1rem' }}>
              <button 
                type="button" 
                onClick={() => setCancelTransaccionModalOpen(false)} 
                style={{ flex: 1, padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', background: 'transparent', cursor: 'pointer' }}
              >
                Volver
              </button>
              <button 
                type="button" 
                className="btn-primary" 
                style={{ flex: 1, padding: '0.75rem' }}
                disabled={!cancelMotivoSeleccionado}
                onClick={async () => {
                  try {
                    setAlertMessage('Solicitud de cancelación enviada a la contraparte.');
                    setCancelTransaccionModalOpen(false);
                    setSelectedTxDetalle(null);
                    setCancelMotivoSeleccionado('');
                  } catch (error) {
                    setAlertMessage('Error al procesar la solicitud');
                  }
                }}
              >
                Enviar Solicitud
              </button>
            </div>
          </div>
        </div>
      , document.body)}

      {/* Modal Adjudicar - Contactos */}
      {adjudicarModalOpen && adjudicarData && createPortal(
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }}>
          <div className="card animate-in" style={{ width: '90%', maxWidth: '440px', padding: '2rem' }}>
            <button
              onClick={() => { setAdjudicarModalOpen(false); setAdjudicarData(null); }}
              style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'transparent', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-tertiary)' }}
            >&times;</button>
            <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-primary)' }}>Subasta Adjudicada</h3>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              A continuación los datos de contacto de las partes involucradas:
            </p>

            <div style={{ marginBottom: '1.5rem', padding: '1rem', backgroundColor: 'var(--bg-secondary)', borderRadius: '0.5rem' }}>
              <h4 style={{ marginBottom: '0.5rem', color: 'var(--accent-primary)', fontSize: '0.95rem' }}>Ganador</h4>
              <p style={{ margin: '0 0 0.25rem', fontSize: '0.9rem', color: 'var(--text-primary)' }}><strong>Nombre:</strong> {adjudicarData.ganador.nombre}</p>
              {adjudicarData.ganador.correoElectronico && <p style={{ margin: '0 0 0.25rem', fontSize: '0.9rem', color: 'var(--text-primary)' }}><strong>Correo:</strong> {adjudicarData.ganador.correoElectronico}</p>}
              {adjudicarData.ganador.telefono && <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-primary)' }}><strong>Teléfono:</strong> {adjudicarData.ganador.telefono}</p>}
            </div>

            <div style={{ marginBottom: '1.5rem', padding: '1rem', backgroundColor: 'var(--bg-secondary)', borderRadius: '0.5rem' }}>
              <h4 style={{ marginBottom: '0.5rem', color: 'var(--accent-secondary)', fontSize: '0.95rem' }}>Propietario (Tú)</h4>
              <p style={{ margin: '0 0 0.25rem', fontSize: '0.9rem', color: 'var(--text-primary)' }}><strong>Nombre:</strong> {adjudicarData.propietario.nombre}</p>
              {adjudicarData.propietario.correoElectronico && <p style={{ margin: '0 0 0.25rem', fontSize: '0.9rem', color: 'var(--text-primary)' }}><strong>Correo:</strong> {adjudicarData.propietario.correoElectronico}</p>}
              {adjudicarData.propietario.telefono && <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-primary)' }}><strong>Teléfono:</strong> {adjudicarData.propietario.telefono}</p>}
            </div>

            <button className="btn-primary" style={{ width: '100%', padding: '0.75rem' }} onClick={() => { setAdjudicarModalOpen(false); setAdjudicarData(null); }}>
              Cerrar
            </button>
          </div>
        </div>,
        document.body
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
