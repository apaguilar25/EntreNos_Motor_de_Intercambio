import React, { useState, useEffect, useContext } from 'react';
import { AppContext } from '../App';
import './AdminDashboard.css';

function AdminDashboard() {
    const { controladorAdministrador, user } = useContext(AppContext);
    const [activeTab, setActiveTab] = useState('incidencias');

    const [incidencias, setIncidencias] = useState([]);
    const [usuarios, setUsuarios] = useState([]);
    const [correos, setCorreos] = useState([]);
    const [nuevoCorreo, setNuevoCorreo] = useState('');

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
            const crs = await controladorAdministrador.cargarCorreosPermitidos();
            setCorreos(crs || []);
        } catch (error) {
            console.error("Error al cargar datos del admin dashboard", error);
        }
    };

    const handleResolver = async (idIncidencia, idTransaccion, idOfertante, idDemandante) => {
        const idGanador = prompt(`Resolución de disputa. Ingresa el ID del usuario ganador:\nOfertante: ${idOfertante}\nDemandante: ${idDemandante}`);
        if (!idGanador) return;

        const sancionarOfertante = window.confirm("¿Sancionar al Ofertante por fraude?");
        const sancionarDemandante = window.confirm("¿Sancionar al Demandante por fraude?");

        try {
            await controladorAdministrador.resolverIncidencia(idIncidencia, idGanador, sancionarOfertante, sancionarDemandante);
            alert("Incidencia resuelta.");
            cargarDatos();
        } catch (error) {
            alert("Error: " + error.message);
        }
    };

    const handleAgregarCorreo = async () => {
        if (!nuevoCorreo.endsWith('@alameda.com')) {
            alert("El correo debe terminar en @alameda.com");
            return;
        }
        try {
            await controladorAdministrador.agregarCorreo(nuevoCorreo);
            setNuevoCorreo('');
            cargarDatos();
        } catch (error) {
            alert("Error al agregar correo.");
        }
    };

    const handleEliminarCorreo = async (correo) => {
        try {
            await controladorAdministrador.eliminarCorreo(correo);
            cargarDatos();
        } catch (error) {
            alert("Error al eliminar correo.");
        }
    };

    const handleModificarCreditos = async (idUsuario) => {
        const creditos = prompt("Ingresa la nueva cantidad de créditos exactos:");
        if (creditos !== null) {
            try {
                await controladorAdministrador.modificarCreditos(idUsuario, parseFloat(creditos));
                alert("Créditos actualizados");
                cargarDatos();
            } catch (error) {
                alert("Error actualizando créditos");
            }
        }
    };

    const handlePerdonar = async (idUsuario) => {
        try {
            await controladorAdministrador.perdonarFaltas(idUsuario);
            alert("Faltas perdonadas.");
            cargarDatos();
        } catch (error) {
            alert("Error al perdonar faltas.");
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
                                            <td>{inc.idUsuarioReportante} <br/><a href={inc.urlEvidencia} target="_blank" rel="noreferrer">Evidencia</a></td>
                                            <td>{inc.idUsuarioDefensor || 'N/A'} {inc.urlEvidenciaDefensa ? <a href={inc.urlEvidenciaDefensa} target="_blank" rel="noreferrer">Evidencia</a> : ''}</td>
                                            <td>{inc.descripcion}</td>
                                            <td>
                                                {inc.estado === 'ABIERTA' && (
                                                    <button onClick={() => handleResolver(inc.idIncidencia, inc.idTransaccion, inc.idUsuarioDefensor, inc.idUsuarioReportante)}>Resolver Disputa</button>
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
                                            <button onClick={() => handleModificarCreditos(u.id)}>Modificar Créditos</button>
                                            {u.reportesFraudeValidados > 0 && (
                                                <button onClick={() => handlePerdonar(u.id)}>Perdonar Faltas</button>
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
        </div>
    );
}

export default AdminDashboard;
