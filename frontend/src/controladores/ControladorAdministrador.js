export class ControladorAdministrador {
    constructor(servicioAdministrador) {
        this.servicioAdministrador = servicioAdministrador;
    }

    async cargarIncidencias() {
        try {
            return await this.servicioAdministrador.listarIncidencias();
        } catch (error) {
            console.error("Error cargando incidencias:", error);
            throw error;
        }
    }

    async resolverIncidencia(idIncidencia, idUsuarioGanadorCreditos, sancionarOfertante, sancionarDemandante, sancionarReportante, sancionarDefensor) {
        try {
            await this.servicioAdministrador.resolverIncidencia(idIncidencia, idUsuarioGanadorCreditos, sancionarOfertante, sancionarDemandante, sancionarReportante, sancionarDefensor);
        } catch (error) {
            console.error("Error resolviendo incidencia:", error);
            throw error;
        }
    }

    async cargarCorreosPermitidos() {
        try {
            return await this.servicioAdministrador.listarCorreosPermitidos();
        } catch (error) {
            console.error("Error cargando correos:", error);
            throw error;
        }
    }

    async agregarCorreo(correo) {
        try {
            await this.servicioAdministrador.agregarCorreoPermitido(correo);
        } catch (error) {
            console.error("Error agregando correo:", error);
            throw error;
        }
    }

    async eliminarCorreo(correo) {
        try {
            await this.servicioAdministrador.eliminarCorreoPermitido(correo);
        } catch (error) {
            console.error("Error eliminando correo:", error);
            throw error;
        }
    }

    async cargarUsuarios() {
        try {
            return await this.servicioAdministrador.listarUsuarios();
        } catch (error) {
            console.error("Error cargando usuarios:", error);
            throw error;
        }
    }

    async modificarCreditos(idUsuario, creditos) {
        try {
            await this.servicioAdministrador.modificarCreditosUsuario(idUsuario, creditos);
        } catch (error) {
            console.error("Error modificando creditos:", error);
            throw error;
        }
    }

    async perdonarFaltas(idUsuario) {
        try {
            await this.servicioAdministrador.perdonarFaltas(idUsuario);
        } catch (error) {
            console.error("Error perdonando faltas:", error);
            throw error;
        }
    }
}
