import React, { useState, useEffect, useContext } from 'react';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';
import Pagination from '../componentes/ui/Pagination';
import './AdminDashboard.css';

function AdminDashboard() {
    const { controladorAdministrador, user } = useContext(AppContext);
    const [activeTab, setActiveTab] = useState('incidencias');
    const [currentPage, setCurrentPage] = useState(1);

    useEffect(() => {
        setCurrentPage(1);
    }, [activeTab]);

    const [incidencias, setIncidencias] = useState([]);
    const [usuarios, setUsuarios] = useState([]);
    const [usersMap, setUsersMap] = useState({});
    const [transaccionesMap, setTransaccionesMap] = useState({});
    const [correos, setCorreos] = useState([]);
    const [nuevoCorreo, setNuevoCorreo] = useState('');

    // Modal state for Reportes
    const [modalReportes, setModalReportes] = useState({
        isOpen: false, incidencia: null, transaccion: null,
        seleccion: null, sancionarReportante: false,
        mostrarDisclaimer: false,
        reclamanteExpandido: false, reclamadoExpandido: false
    });
    // Modal state for Creditos
    const [modalCreditos, setModalCreditos] = useState({ isOpen: false, idUsuario: null, cantidad: '' });

    const { addToast } = useContext(ToastContext);

    useEffect(() => {
        if (user && user.rol === 'ADMINISTRADOR') {
            cargarDatos();
        }
    }, [user]);

    const cargarDatos = async () => {
        try {
            const inc = await controladorAdministrador.cargarIncidencias();
            setIncidencias(inc || []);
            const usrs = await controladorAdministrador.cargarUsuarios();
            setUsuarios(usrs || []);
            const map = {};
            (usrs || []).forEach(u => map[u.id] = u.nombre);
            setUsersMap(map);

            const txRes = await fetch('http://localhost:8080/api/transacciones');
            if (txRes.ok) {
                const txs = await txRes.json();
                const txMap = {};
                (txs || []).forEach(tx => txMap[tx.idTransaccion] = tx);
                setTransaccionesMap(txMap);
            }

            const crs = await controladorAdministrador.cargarCorreosPermitidos();
            setCorreos(crs || []);
        } catch (error) {
            console.error("Error al cargar datos del admin dashboard", error);
        }
    };

    const cerrarModal = () => {
        setModalReportes({ isOpen: false, incidencia: null, transaccion: null, seleccion: null, sancionarReportante: false, mostrarDisclaimer: false, reclamanteExpandido: false, reclamadoExpandido: false });
    };

    const handleResolverClick = async (incidencia) => {
        const tx = transaccionesMap[incidencia.idTransaccion] || null;
        setModalReportes({
            isOpen: true, incidencia, transaccion: tx,
            seleccion: null, sancionarReportante: false,
            mostrarDisclaimer: false,
            reclamanteExpandido: false, reclamadoExpandido: false
        });
    };

    const handleSeleccionarGanador = (lado) => {
        setModalReportes({ ...modalReportes, seleccion: modalReportes.seleccion === lado ? null : lado, mostrarDisclaimer: false });
    };

    const confirmResolucion = async () => {
        if (!modalReportes.seleccion) {
            addToast("Debes seleccionar un ganador haciendo clic en el cuadrado verde de una de las partes.", "error");
            return;
        }
        if (!modalReportes.mostrarDisclaimer) {
            setModalReportes({ ...modalReportes, mostrarDisclaimer: true });
            return;
        }
        const inc = modalReportes.incidencia;
        const tx = modalReportes.transaccion;
        const esReclamante = modalReportes.seleccion === 'reclamante';
        const idGanador = esReclamante ? inc.idUsuarioReportante : inc.idUsuarioDefensor;
        const sancionarReportante = esReclamante ? false : modalReportes.sancionarReportante;
        const sancionarDefensor = esReclamante ? true : false;

        try {
            await controladorAdministrador.resolverIncidencia(
                inc.idIncidencia, idGanador,
                false, false, sancionarReportante, sancionarDefensor
            );
            addToast("Incidencia resuelta exitosamente.", "success");
            cerrarModal();
            cargarDatos();
        } catch (error) {
            addToast("Error: " + error.message, "error");
        }
    };

    const getRol = (idUsuario, tx) => {
        if (!tx) return '';
        if (idUsuario === tx.idOfertante) return 'Ofertante';
        if (idUsuario === tx.idDemandante) return 'Demandante';
        return '';
    };

    const handleAgregarCorreo = async () => {
        if (!nuevoCorreo.endsWith('@alameda.com')) {
            addToast("El correo debe terminar en @alameda.com", "error");
            return;
        }
        const confirmar = window.confirm("¿Estás seguro de que deseas permitir el registro de este correo?");
        if (!confirmar) return;
        try {
            await controladorAdministrador.agregarCorreo(nuevoCorreo);
            setNuevoCorreo('');
            addToast("Correo agregado con éxito.", "success");
            cargarDatos();
        } catch (error) {
            addToast("Error al agregar correo.", "error");
        }
    };

    const handleEliminarCorreo = async (correo) => {
        const confirmar = window.confirm("¿Estás seguro de que deseas eliminar este correo?");
        if (!confirmar) return;
        try {
            await controladorAdministrador.eliminarCorreo(correo);
            addToast("Correo eliminado exitosamente.", "success");
            cargarDatos();
        } catch (error) {
            addToast("Error al eliminar correo.", "error");
        }
    };

    const handleModificarCreditosClick = (idUsuario) => {
        setModalCreditos({ isOpen: true, idUsuario, cantidad: '' });
    };

    const confirmModificarCreditos = async () => {
        if (modalCreditos.cantidad === '') {
            addToast("Debes ingresar una cantidad.", "error");
            return;
        }
        try {
            await controladorAdministrador.modificarCreditos(modalCreditos.idUsuario, parseFloat(modalCreditos.cantidad));
            addToast("Créditos actualizados con éxito.", "success");
            setModalCreditos({ isOpen: false, idUsuario: null, cantidad: '' });
            cargarDatos();
        } catch (error) {
            addToast("Error actualizando créditos.", "error");
        }
    };

    const handlePerdonar = async (idUsuario) => {
        try {
            await controladorAdministrador.perdonarFaltas(idUsuario);
            addToast("Faltas perdonadas exitosamente.", "success");
            cargarDatos();
        } catch (error) {
            addToast("Error al perdonar faltas.", "error");
        }
    };

    if (!user || user.rol !== 'ADMINISTRADOR') {
        return <div>Acceso denegado. Solo administradores.</div>;
    }

    const inc = modalReportes.incidencia;
    const tx = modalReportes.transaccion;
    const rolReclamante = inc && tx ? getRol(inc.idUsuarioReportante, tx) : '';
    const rolReclamado = inc && tx && inc.idUsuarioDefensor ? getRol(inc.idUsuarioDefensor, tx) : '';
    const esReclamante = () => modalReportes.seleccion === 'reclamante';
    const esReclamado = () => modalReportes.seleccion === 'reclamado';
    const reclamanteFotos = inc ? (inc.fotosEvidencia || []) : [];
    const reclamadoFotos = inc ? (inc.fotosEvidenciaDefensa || []) : [];
    const tieneDefensa = inc && inc.descripcionDefensa && inc.descripcionDefensa.trim().length > 0;

    return (
        <div className="admin-dashboard">
            <h1>Panel de Administración</h1>
            <div className="admin-tabs">
                <button className={activeTab === 'incidencias' ? 'active' : ''} onClick={() => setActiveTab('incidencias')}>Incidencias / Disputas</button>
                <button className={activeTab === 'usuarios' ? 'active' : ''} onClick={() => setActiveTab('usuarios')}>Gestión de Usuarios</button>
                <button className={activeTab === 'correos' ? 'active' : ''} onClick={() => setActiveTab('correos')}>Correos Permitidos</button>
            </div>

            <div className="admin-content">
                {activeTab === 'incidencias' && (
                    <div>
                        <h2>Incidencias Reportadas</h2>
                        {incidencias.length === 0 ? <p>No hay incidencias.</p> : (
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Transacción</th>
                                        <th>Estado Transacción</th>
                                        <th>Estado Incidencia</th>
                                        <th>Reportante</th>
                                        <th>Defensor</th>
                                        <th>Descripción</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {incidencias.map(inc => (
                                        <tr key={inc.idIncidencia}>
                                            <td>{inc.idIncidencia}</td>
                                            <td>{inc.idTransaccion}</td>
                                            <td>{(transaccionesMap[inc.idTransaccion]?.estado) || 'N/A'}</td>
                                            <td>{inc.estado}</td>
                                            <td>{usersMap[inc.idUsuarioReportante] || inc.idUsuarioReportante}</td>
                                            <td>{inc.idUsuarioDefensor ? (usersMap[inc.idUsuarioDefensor] || inc.idUsuarioDefensor) : 'N/A'}</td>
                                            <td>{inc.descripcion}</td>
                                            <td>
                                                {inc.estado === 'ABIERTA' && (
                                                    <button className="btn-primary" style={{ backgroundColor: 'var(--color-green-700)', color: '#fff', fontSize: '0.75rem', padding: '0.25rem 0.5rem' }} onClick={() => handleResolverClick(inc)}>Resolver Disputa</button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                )}

                {activeTab === 'usuarios' && (
                    <div>
                        <h2>Lista de Usuarios</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Correo</th>
                                    <th>Estado</th>
                                    <th>Faltas</th>
                                    <th>Créditos</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {usuarios.map(u => (
                                    <tr key={u.id}>
                                        <td>{u.id}</td>
                                        <td>{u.nombre}</td>
                                        <td>{u.correoElectronico}</td>
                                        <td>{u.estado}</td>
                                        <td>{u.reportesFraudeValidados}</td>
                                        <td>{u.monedero?.creditosDisponibles}</td>
                                        <td>
                                            <button style={{ marginRight: '0.5rem', marginBottom: '0.5rem' }} className="btn-primary" onClick={() => handleModificarCreditosClick(u.id)}>Modificar Créditos</button>
                                            {u.reportesFraudeValidados > 0 && (
                                                <button className="btn-primary" style={{ backgroundColor: 'var(--color-orange-600)', marginRight: '0.5rem', marginBottom: '0.5rem' }} onClick={() => handlePerdonar(u.id)}>Perdonar Faltas</button>
                                            )}
                                            {u.estado === 'SUSPENDIDO_FRAUDE' && (
                                                <button className="btn-primary" style={{ backgroundColor: 'var(--color-purple-600)' }} onClick={() => handlePerdonar(u.id)}>Desbloquear Cuenta</button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {activeTab === 'correos' && (
                    <div>
                        <h2>Correos Permitidos para Registro</h2>
                        <div className="add-correo">
                            <input type="email" placeholder="nuevo@alameda.com" value={nuevoCorreo} onChange={e => setNuevoCorreo(e.target.value)} />
                            <button onClick={handleAgregarCorreo}>Agregar</button>
                        </div>
                        <ul>
                            {correos.map(c => (
                                <li key={c.correo}>
                                    {c.correo} <button onClick={() => handleEliminarCorreo(c.correo)}>Eliminar</button>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            {/* Modal de Gestión de Reportes */}
            {modalReportes.isOpen && inc && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }} onClick={cerrarModal}>
                    <div style={{ backgroundColor: 'var(--bg-primary)', padding: '2rem', borderRadius: '1rem', width: '90%', maxWidth: '600px', maxHeight: '80vh', overflowY: 'auto', boxShadow: '0 10px 25px rgba(0,0,0,0.2)', position: 'relative' }} onClick={(e) => e.stopPropagation()}>
                        {/* X button */}
                        <button onClick={cerrarModal} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: 'var(--text-tertiary)', lineHeight: 1 }}>×</button>

                        <h2 style={{ marginTop: 0, color: 'var(--color-green-700)', marginBottom: '1.5rem' }}>Gestión de Reportes</h2>

                        {/* Reclamante card */}
                        <div style={{ marginBottom: '1rem' }}>
                            <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>Reclamante <span style={{ fontWeight: 'normal', color: 'var(--text-tertiary)' }}>({rolReclamante || 'Desconocido'})</span></h4>
                            <div style={{ border: '2px solid var(--border-color)', borderRadius: '5%', padding: '0.75rem', display: 'flex', alignItems: 'flex-start', gap: '0.75rem', flexWrap: 'nowrap' }}>
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    <button onClick={() => setModalReportes({ ...modalReportes, reclamanteExpandido: !modalReportes.reclamanteExpandido })} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--text-primary)', fontWeight: 'bold', fontSize: '0.85rem' }}>
                                        <span style={{ display: 'inline-block', transition: 'transform 0.2s', transform: modalReportes.reclamanteExpandido ? 'rotate(90deg)' : 'rotate(0deg)' }}>▶</span>
                                        {usersMap[inc.idUsuarioReportante] || inc.idUsuarioReportante}
                                    </button>
                                    {modalReportes.reclamanteExpandido && (
                                        <div style={{ marginTop: '0.5rem', paddingLeft: '1.5rem' }}>
                                            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '0 0 0.5rem 0' }}>{inc.descripcion}</p>
                                            {reclamanteFotos.length > 0 && (
                                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                                    {reclamanteFotos.map((foto, idx) => (
                                                        <img key={idx} src={foto} alt={`Evidencia ${idx + 1}`} style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '0.25rem', cursor: 'pointer' }} onClick={() => window.open(foto, '_blank')} />
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    )}
                                </div>
                                <button onClick={() => handleSeleccionarGanador('reclamante')} style={{ flexShrink: 0, width: '40px', height: '40px', borderRadius: '10%', border: '2px solid var(--color-green-700)', backgroundColor: esReclamante() ? 'var(--color-green-700)' : 'transparent', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.2s' }}>
                                    {esReclamante() && <span style={{ color: '#fff', fontSize: '1.2rem' }}>✓</span>}
                                </button>
                            </div>
                        </div>

                        {/* Reclamado card */}
                        <div style={{ marginBottom: '1rem' }}>
                            <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>Reclamado <span style={{ fontWeight: 'normal', color: 'var(--text-tertiary)' }}>({rolReclamado || 'Desconocido'})</span></h4>
                            <div style={{ border: '2px solid var(--border-color)', borderRadius: '5%', padding: '0.75rem', display: 'flex', alignItems: 'flex-start', gap: '0.75rem', flexWrap: 'nowrap' }}>
                                <div style={{ flex: 1, minWidth: 0 }}>
                                    {inc.idUsuarioDefensor ? (
                                        <>
                                            <button onClick={() => setModalReportes({ ...modalReportes, reclamadoExpandido: !modalReportes.reclamadoExpandido })} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.25rem', color: 'var(--text-primary)', fontWeight: 'bold', fontSize: '0.85rem' }}>
                                                <span style={{ display: 'inline-block', transition: 'transform 0.2s', transform: modalReportes.reclamadoExpandido ? 'rotate(90deg)' : 'rotate(0deg)' }}>▶</span>
                                                {usersMap[inc.idUsuarioDefensor] || inc.idUsuarioDefensor}
                                            </button>
                                            {modalReportes.reclamadoExpandido && (
                                                <div style={{ marginTop: '0.5rem', paddingLeft: '1.5rem' }}>
                                                    {tieneDefensa ? (
                                                        <>
                                                            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '0 0 0.5rem 0' }}>{inc.descripcionDefensa}</p>
                                                            {reclamadoFotos.length > 0 && (
                                                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                                                                    {reclamadoFotos.map((foto, idx) => (
                                                                        <img key={idx} src={foto} alt={`Defensa ${idx + 1}`} style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '0.25rem', cursor: 'pointer' }} onClick={() => window.open(foto, '_blank')} />
                                                                    ))}
                                                                </div>
                                                            )}
                                                        </>
                                                    ) : (
                                                        <p style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)', fontStyle: 'italic', margin: 0 }}>El reclamado no ha adjuntado una defensa</p>
                                                    )}
                                                </div>
                                            )}
                                        </>
                                    ) : (
                                        <p style={{ fontSize: '0.8rem', color: 'var(--text-tertiary)', fontStyle: 'italic', margin: 0, padding: '0.25rem' }}>El reclamado no ha adjuntado una defensa</p>
                                    )}
                                </div>
                                {inc.idUsuarioDefensor && (
                                    <button onClick={() => handleSeleccionarGanador('reclamado')} style={{ flexShrink: 0, width: '40px', height: '40px', borderRadius: '10%', border: '2px solid var(--color-green-700)', backgroundColor: esReclamado() ? 'var(--color-green-700)' : 'transparent', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', transition: 'all 0.2s' }}>
                                        {esReclamado() && <span style={{ color: '#fff', fontSize: '1.2rem' }}>✓</span>}
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* Checkbox de sanción (solo cuando se selecciona Reclamado) */}
                        {esReclamado() && (
                            <div style={{ marginBottom: '1rem', padding: '0.5rem 0.75rem', backgroundColor: '#fff3cd', border: '1px solid #ffc107', borderRadius: '0.5rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.85rem' }}>
                                    <input type="checkbox" checked={modalReportes.sancionarReportante} onChange={(e) => setModalReportes({ ...modalReportes, sancionarReportante: e.target.checked })} />
                                    <span>Sancionar al Reclamante por reporte falso (Suma falta)</span>
                                </label>
                                <p style={{ margin: '0.25rem 0 0 1.5rem', fontSize: '0.75rem', color: 'var(--text-tertiary)' }}>
                                    Activa esta casilla si consideras que la queja del Reclamante fue maliciosa, falsa o con intención explícita de engaño.
                                </p>
                            </div>
                        )}

                        {/* Disclaimer */}
                        {modalReportes.mostrarDisclaimer && (
                            <div style={{ marginBottom: '1rem', padding: '0.75rem', backgroundColor: '#f8f9fa', border: '1px solid #dee2e6', borderRadius: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                                {esReclamante() ? (
                                    <p style={{ margin: 0 }}><strong>Consecuencias:</strong> Al elegir al Reclamante como ganador, los créditos retenidos serán liberados a su favor. El Reclamado recibirá <strong>1 falta por fraude</strong> (sanción automática). Al acumular 2 faltas, el usuario será suspendido.</p>
                                ) : (
                                    <p style={{ margin: 0 }}><strong>Consecuencias:</strong> Al elegir al Reclamado como ganador, los créditos retenidos serán devueltos o liberados según corresponda.{modalReportes.sancionarReportante ? ' El Reclamante recibirá 1 falta por reporte falso.' : ' No se aplicarán sanciones a menos que actives la casilla de sanción por reporte falso.'}</p>
                                )}
                            </div>
                        )}

                        {/* Botones */}
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                            <button className="btn-primary" style={{ backgroundColor: 'var(--color-green-700)', color: '#fff', opacity: modalReportes.seleccion ? 1 : 0.5, cursor: modalReportes.seleccion ? 'pointer' : 'not-allowed' }} disabled={!modalReportes.seleccion} onClick={confirmResolucion}>
                                {modalReportes.mostrarDisclaimer ? 'Confirmar Resolución' : 'Confirmar Elección'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {modalCreditos.isOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div style={{ backgroundColor: 'var(--bg-primary)', padding: '2rem', borderRadius: '1rem', width: '90%', maxWidth: '400px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>
                        <h2 style={{ marginTop: 0, color: 'var(--color-green-700)' }}>Modificar Créditos</h2>
                        <p style={{ color: 'var(--text-secondary)' }}>Ingresa la nueva cantidad exacta de créditos disponibles para este usuario.</p>
                        
                        <input 
                            type="number" min="0" step="1"
                            style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)', marginBottom: '1.5rem', boxSizing: 'border-box' }}
                            value={modalCreditos.cantidad}
                            onChange={(e) => setModalCreditos({ ...modalCreditos, cantidad: e.target.value })}
                            placeholder="Ej. 100"
                        />

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                            <button className="btn-primary" style={{ backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-primary)' }} onClick={() => setModalCreditos({ isOpen: false, idUsuario: null, cantidad: '' })}>Cancelar</button>
                            <button className="btn-primary" style={{ backgroundColor: 'var(--color-green-700)', color: '#fff' }} onClick={confirmModificarCreditos}>Guardar</button>
                        </div>
                    </div>
                </div>
            )}

        </div>
    );
}

export default AdminDashboard;
