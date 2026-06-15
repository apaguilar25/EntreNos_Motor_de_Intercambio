export class ControladorMuro {
  constructor(servicioPublicacion) {
    this.servicioPublicacion = servicioPublicacion;
  }

  async obtenerPublicaciones(terminoBusqueda) {
    try {
      // Idealmente el backend soportaría el filtro por nombreServicio o descripcion, pero por ahora traemos todas y filtramos localmente o pasamos el término si la API lo soporta.
      const data = await this.servicioPublicacion.obtenerPublicaciones();
      let filtradas = data;
      
      if (terminoBusqueda) {
        const term = terminoBusqueda.toLowerCase();
        filtradas = data.filter(item => 
          (item.nombreServicio && item.nombreServicio.toLowerCase().includes(term)) ||
          (item.descripcion && item.descripcion.toLowerCase().includes(term)) ||
          (item.tipoPublicacion && item.tipoPublicacion.toLowerCase().includes(term))
        );
      }
      return filtradas;
    } catch (error) {
      console.error('Error en ControladorMuro obteniendo publicaciones:', error);
      return []; // Devolvemos array vacío en vez de lanzar error para no romper la UI
    }
  }
}
