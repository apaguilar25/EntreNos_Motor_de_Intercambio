export class ServicioAdministrador {
    constructor(clienteHttp) {
        this.clienteHttp = clienteHttp;
    }

    async listarIncidencias() {
        return await this.clienteHttp.get('/admin/incidencias');
    }

    async resolverIncidencia(idIncidencia, idUsuarioGanadorCreditos, sancionarOfertante, sancionarDemandante) {
        return await this.clienteHttp.post(`/admin/incidencias/${idIncidencia}/resolver`, {
            idUsuarioGanadorCreditos,
            sancionarOfertante,
            sancionarDemandante
        });
    }

    async listarCorreosPermitidos() {
        return await this.clienteHttp.get('/admin/correos');
    }

    async agregarCorreoPermitido(correo) {
        return await this.clienteHttp.post('/admin/correos', { correo });
    }

    async eliminarCorreoPermitido(correo) {
        return await this.clienteHttp.delete(`/admin/correos/${correo}`);
    }

    async listarUsuarios() {
        return await this.clienteHttp.get('/admin/usuarios');
    }

    async modificarCreditosUsuario(idUsuario, creditos) {
        return await this.clienteHttp.put(`/admin/usuarios/${idUsuario}/creditos`, { creditos });
    }

    async perdonarFaltas(idUsuario) {
        return await this.clienteHttp.put(`/admin/usuarios/${idUsuario}/perdonar-faltas`, {});
    }
}
