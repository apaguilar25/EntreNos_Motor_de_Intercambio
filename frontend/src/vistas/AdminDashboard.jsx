import React, { useState, useEffect, useContext } from 'react';
import { AppContext } from '../App';
import { ToastContext } from '../contextos/ToastContext';
import './AdminDashboard.css';

function AdminDashboard() {
    const { controladorAdministrador, user } = useContext(AppContext);
    const [activeTab, setActiveTab] = useState('incidencias');

    const [incidencias, setIncidencias] = useState([]);
    const [usuarios, setUsuarios] = useState([]);
    const [usersMap, setUsersMap] = useState({});
    const [correos, setCorreos] = useState([]);
    const [nuevoCorreo, setNuevoCorreo] = useState('');

    // Modal state for Disputa
    const [modalDisputa, setModalDisputa] = useState({ isOpen: false, data: null, ganador: '', sancionarO: false, sancionarD: false });
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
            const crs = await controladorAdministrador.cargarCorreosPermitidos();
            setCorreos(crs || []);
        } catch (error) {
            console.error("Error al cargar datos del admin dashboard", error);
        }
    };

    const handleResolverClick = (incidencia) => {
        setModalDisputa({
            isOpen: true,
            data: incidencia,
            ganador: '',
            sancionarO: false,
            sancionarD: false
        });
    };

    const confirmResolver = async () => {
        if (!modalDisputa.ganador) {
            addToast("Debes seleccionar un usuario ganador o indicar 'empate'.", "error");
            return;
        }
        try {
            await controladorAdministrador.resolverIncidencia(
                modalDisputa.data.idIncidencia, 
                modalDisputa.ganador, 
                modalDisputa.sancionarO, 
                modalDisputa.sancionarD
            );
            addToast("Incidencia resuelta exitosamente.", "success");
            setModalDisputa({ isOpen: false, data: null, ganador: '', sancionarO: false, sancionarD: false });
            cargarDatos();
        } catch (error) {
            addToast("Error: " + error.message, "error");
        }
    };

    const handleAgregarCorreo = async () => {
        if (!nuevoCorreo.endsWith('@alameda.com')) {
            addToast("El correo debe terminar en @alameda.com", "error");
            return;
        }
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
                                        <th>Estado</th>
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
                                            <td>{inc.estado}</td>
                                            <td>{usersMap[inc.idUsuarioReportante] || inc.idUsuarioReportante} <br/><a href={inc.urlEvidencia} target="_blank" rel="noreferrer">Evidencia</a></td>
                                            <td>{inc.idUsuarioDefensor ? (usersMap[inc.idUsuarioDefensor] || inc.idUsuarioDefensor) : 'N/A'} {inc.urlEvidenciaDefensa ? <a href={inc.urlEvidenciaDefensa} target="_blank" rel="noreferrer">Evidencia</a> : ''}</td>
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
                                                <button className="btn-primary" style={{ backgroundColor: 'var(--color-orange-600)' }} onClick={() => handlePerdonar(u.id)}>Perdonar Faltas</button>
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

            {/* Modals */}
            {modalDisputa.isOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div style={{ backgroundColor: 'var(--bg-primary)', padding: '2rem', borderRadius: '1rem', width: '90%', maxWidth: '500px', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>
                        <h2 style={{ marginTop: 0, color: 'var(--color-green-700)' }}>Resolución de Disputa</h2>
                        <p style={{ color: 'var(--text-secondary)' }}>Selecciona al ganador de la disputa y las sanciones correspondientes.</p>
                        
                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>Usuario Ganador:</label>
                            <select 
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '0.5rem', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)' }}
                                value={modalDisputa.ganador} 
                                onChange={(e) => setModalDisputa({ ...modalDisputa, ganador: e.target.value })}
                            >
                                <option value="">-- Selecciona un ganador --</option>
                                <option value={modalDisputa.data?.idUsuarioReportante}>Reportante: {usersMap[modalDisputa.data?.idUsuarioReportante] || modalDisputa.data?.idUsuarioReportante}</option>
                                {modalDisputa.data?.idUsuarioDefensor && (
                                    <option value={modalDisputa.data?.idUsuarioDefensor}>Defensor: {usersMap[modalDisputa.data?.idUsuarioDefensor] || modalDisputa.data?.idUsuarioDefensor}</option>
                                )}
                            </select>
                        </div>

                        <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                                <input type="checkbox" checked={modalDisputa.sancionarD} onChange={(e) => setModalDisputa({ ...modalDisputa, sancionarD: e.target.checked })} />
                                <span>Sancionar Reportante por Fraude (Suma falta)</span>
                            </label>
                            {modalDisputa.data?.idUsuarioDefensor && (
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                                    <input type="checkbox" checked={modalDisputa.sancionarO} onChange={(e) => setModalDisputa({ ...modalDisputa, sancionarO: e.target.checked })} />
                                    <span>Sancionar Defensor por Fraude (Suma falta)</span>
                                </label>
                            )}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                            <button className="btn-primary" style={{ backgroundColor: 'var(--bg-tertiary)', color: 'var(--text-primary)' }} onClick={() => setModalDisputa({ isOpen: false, data: null, ganador: '', sancionarO: false, sancionarD: false })}>Cancelar</button>
                            <button className="btn-primary" style={{ backgroundColor: 'var(--color-green-700)', color: '#fff' }} onClick={confirmResolver}>Confirmar Resolución</button>
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
                            type="number" 
                            step="0.1"
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
