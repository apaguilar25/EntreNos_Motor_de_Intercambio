
- Cambios en Habilidad y Necesidad:
  - Habilidad ya no tendra precio asociado sino que se le asignara desde cada publicacion
  - Habilidad que ofrezco y necesito seran la misma clase, estaran divididos por contexto
  - Es mucho mas intuitivo y simplifica logica para la persistencia
  - Se le agrego atributo Categoria por los tags del ERS
  - Tiene descripcionCapacidad
    - Al ofrecer, es la experiencia que tiene en el tema
    - Al necesitar, es la descripcion del problema por el cual necesita el servicio

-------------------------------------  *  -------------------------------------  *  -------------------------------------
GestionCatalogo eliminada, cambiar logica para simplificar

Resumen de Archivos de Persistencias

Persistencia de Usuarios: usuarios.json
 Mantiene el estado permanente de todas las identidades del sistema.
 Guarda en cada Usuario su Monedero y Catalogo Personalizado (Necesidad, Habilidad, precio, descripcion)
 La editan:
    GestionUsuario (Registrar nuevo usuario, inicializar capital semilla, modificar datos de perfil usuario)

 La leen:
    GestionUsuario (Buscar Perfiles)
    GestionCatalogo (Agregar habilidad a usuario)
    Muro (Lee Habilidades y Necesidades para armar muro)


Persistencia de Opciones de Catalogo: catalogo.json
 Datos estaticos
 Desde aca los usuarios seleccionan las opciones de un menu
  desplegable para crear su catalogo
 La editan:
    Nadie. Es manualmente precargada
 La leen:
    GestionCatalogo al agregar/mostrar habilidades/necesidades al el usuario
``

Persistencia de Solicitudes de Intercambio: solicitudes.json
 Registra historico de intercambios entre miembros (HU2)
 La editan:
    GestionSolicitudIntercambio (Cambios de estado de intercambio, Aceptado/Rechazado/Expirado)
La leen:
    GestionSolicitudIntercambio (Limpieza de expirados, tambien para otra cosa que no recuedo)


Persistencia de Transacciones:
 Registra las transacciones que ya han sido aceptadas por ambos miembros
 Guarda Toda la estructura de transacciones incluyendo Servicio
 La edita:
    GestionTransaccion (Crear transaccion, cambios de estado)
 La lee:
    GestionTransaccion (Ver si ambas partes confirmaron transaccion)
    GestionUsuario (Lee para calcular reputacion total)


Persistencia Notificaciones: notificaciones.json
 Almacena tod.o tipo de alertas, notificaciones.
   Ojo, Notificacion no puede enviarse a si misma, casi siempre sera mandada por una Gestion
 Tambien incluye notificaciones que necesiten decision (Aceptar, rechazar)
 La editan:
    GestionSolicitudIntercambio
    GestionTransaccion
 La leen:
    GestionNotificacion (Creado para el Angular, para marcar como leido las notif)

