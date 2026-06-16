# Contexto del Proyecto: EntreNos (Motor de Intercambio)

Este documento sirve como referencia rápida para que cualquier inteligencia artificial o desarrollador nuevo pueda comprender al instante el estado del proyecto, su arquitectura y lo que se ha avanzado.

## Arquitectura General
El proyecto se divide en dos grandes bloques bajo un patrón Cliente-Servidor:
1. **Backend (Java / Spring Boot)**:
   - Persistencia de datos basada en archivos JSON (`backend/data/`).
   - Implementa seguridad y autenticación vía JWT (JSON Web Tokens).
   - Estructurado en módulos (Autenticación, Gamificación, Publicaciones, etc.).
   - Expone una API REST bajo el prefijo `/api/`.
2. **Frontend (React / Vite)**:
   - Implementa una arquitectura **MVC (Modelo-Vista-Controlador)** estricta para separar responsabilidades.
   - **Vistas (`src/vistas/`)**: Componentes de React puros que solo manejan la interfaz de usuario. Delegan toda la lógica a los controladores.
   - **Controladores (`src/controladores/`)**: Clases o módulos (como `ControladorAutenticacion.js`, `ControladorMuro.js`, etc.) que manejan la lógica de negocio, actualizan el estado global y llaman a los servicios.
   - **Servicios API (`src/servicios_api/`)**: Clases (como `ServicioGamificacion.js`, `clienteHttp.js`) responsables de la comunicación exclusiva vía Fetch con la API de Spring Boot. Interceptan tokens y manejan respuestas (ej. 204 No Content).
   - **Estado Global (`App.jsx`)**: Actúa como contenedor de inyección de dependencias y maneja el estado general (usuario, balance, etc.).

## Estado de Desarrollo Actual (Fin del Sprint 2)

### Logros Completados Recientemente:
- **Autenticación e Integración:**
  - Contraseñas almacenadas de forma segura (Bcrypt hash) en el archivo JSON local `usuarios.json`.
  - Frontend intercepta automáticamente peticiones inyectando el `Authorization: Bearer <token>`.
- **Muro y Catálogo:**
  - Lógica para evaluar si un usuario ha llenado o no su catálogo (habilidades/necesidades) e instarlo a hacerlo.
  - Implementación visual de publicaciones e interacciones.
- **Módulo de Gamificación:**
  - Sistema de logros (medallas) visibles en el Perfil de usuario.
  - Sistema de **Podio Semanal** visible permanentemente en el lateral del Muro de Publicaciones.
  - Se corrigió el flujo de la API para que apunte correctamente a `/api/podio` y maneje respuestas vacías (HTTP 204) sin romper el frontend.
- **Arquitectura Limpia (MVC):**
  - Se completó la refactorización para extraer toda la lógica y el "fetch" de los componentes visuales de React hacia los Servicios y Controladores.
- **Trazabilidad (Modelo C4):**
  - Se generó el mapeo entre las Historias de Usuario, Componentes Frontend, y los Servicios/Controladores.

## Historias de Usuario del Sprint 2 Implementadas:
- **HU6**: Sugerencias de publicaciones que coinciden con habilidades (Pestaña "Para Ti" en el Muro).
- **HU7**: Validación de sesión por correo y contraseña protegiendo el ecosistema local.
- **HU8**: Reporte de incidentes abarcado, permitiendo al usuario poner una transacción en disputa mediante un modal dentro del Perfil.
- **HU9**: Presentar una vista en el perfil personal para visualizar los logros (Medallas y progreso).
- **HU10**: Mostrar el podio semanal para destacar a usuarios que aportan valor al sistema.

### Desarrollos Adicionales Clave:
- **Subastas (HU5 Completa):** Sistema robusto para ofrecer un bien y recibir múltiples ofertas. Destaca automáticamente la **"Mejor Oferta"** basándose en la suma total de bienes ofrecidos en la puja.
- **Edición del Catálogo:** Implementado el CRUD completo en el `Profile.jsx` para modificar precios o descripciones, y borrar publicaciones (ofertas y necesidades).
- **Sincronización Transparente:** Script y validaciones que aseguran que las publicaciones (`publicaciones.json`) siempre reflejen correctamente el contenido del catálogo de los usuarios (`usuarios.json`), arreglando el problema del muro vacío.

## Próximos Pasos (Pendientes)
*(Esta sección se puede ir actualizando según se inicie el Sprint 3 o tareas subsecuentes).*
- Implementación de notificaciones de Chat en tiempo real si aplican.
- Validar despliegue de arquitectura o refinamientos visuales finales para presentación.

---
**Nota para la IA:** Al leer este archivo en una sesión nueva, toma este contexto como la base ineludible de la estructura del proyecto y no propongas romper el patrón MVC en React que ya se ha instaurado.
