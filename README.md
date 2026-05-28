# Scrum MVC Project (React Frontend + JSON Persistence Backend)

Este es un proyecto completo estructurado bajo la arquitectura **Model-View-Controller (MVC)** tanto para el Frontend como para el Backend.

## Estructura del Proyecto

El proyecto está organizado de la siguiente manera:

```text
EntreNos_Motor_de_Intercambio/
│
├── backend/                             # API REST en Spring Boot (Java)
│   ├── pom.xml                          # Dependencias de Maven (No se traduce)
│   └── src/main/java/es/ucab/entrenos/  # Ruta base de Java (Convención estándar)
│       │
│       ├── AplicacionEntreNos.java      # Clase principal (Punto de entrada)
│       │
│       ├── configuracion/               # Configuraciones globales
│       │   ├── ConfiguracionGson.java   # Configuración de lectura/escritura JSON
│       │   └── ConfiguracionWeb.java    # Configuración de CORS para React
│       │
│       ├── nucleo/                      # ÉPICA 5: Persistencia y Seguridad 
│       │   ├── base_de_datos/
│       │   │   └── ManejadorArchivoJson.java # Lógica para leer/escribir/bloquear archivos
│       │   └── seguridad/               # Pendiente por cualquier cosa que haya que validar
│       │
│       └── modulos/                     # Módulos del negocio (Separados por Épicas)
│           │
│           ├── identidad/               # ÉPICA 1: Usuarios y Monedero 
│           │   ├── modelos/
│           │   │   ├── Usuario.java
│           │   │   ├── Billetera.java   # Saldo estricto de créditos
│           │   │   └── Habilidad.java   # Catálogo de talentos
│           │   ├── dto/
│           │   │   └── PerfilUsuarioDTO.java # Objeto de transferencia desnormalizado
│           │   ├── controladores/
│           │   │   └── ControladorUsuario.java
│           │   ├── servicios/
│           │   │   └── ServicioUsuario.java
│           │   └── repositorios/
│           │       ├── IRepositorioUsuario.java
│           │       └── RepositorioUsuarioJson.java
│           │
│           ├── reputacion/              # ÉPICA 2: Reputación 
│           │   ├── modelos/
│           │   │   └── Resena.java      # Calificación (estrellas) post-transacción
│           │   ├── controladores/
│           │   │   └── ControladorReputacion.java
│           │   ├── servicios/
│           │   │   └── ServicioReputacion.java
│           │   └── repositorios/
│           │       ├── IRepositorioReputacion.java
│           │       └── RepositorioReputacionJson.java
│           │
│           ├── publicacion/             # ÉPICA 3: Muro y Créditos 
│           │   ├── modelos/
│           │   │   ├── Publicacion.java # Oferta/Demanda de servicios
│           │   │   └── Transaccion.java # Historial de movimiento de créditos
│           │   ├── controladores/
│           │   │   └── ControladorPublicacion.java
│           │   ├── servicios/
│           │   │   └── ServicioPublicacion.java
│           │   └── repositorios/
│           │       ├── IRepositorioPublicacion.java
│           │       ├── IRepositorioTransaccion.java
│           │       ├── RepositorioPublicacionJson.java
│           │       └── RepositorioTransaccionJson.java
│           │
│           └── subasta/                 # ÉPICA 4: Subastas Físicas 
│               ├── modelos/
│               │   ├── Activo.java      # Bien físico a subastar (Ej. Microondas)
│               │   ├── Puja.java        # Oferta en bienes de consumo (Alimentos/Insumos)
│               │   └── Subasta.java     # Estado de la subasta y tiempos
│               ├── controladores/
│               │   └── ControladorSubasta.java
│               ├── servicios/
│               │   └── ServicioSubasta.java # Valida pujas válidas (solo bienes, no créditos)
│               └── repositorios/
│                   ├── IRepositorioSubasta.java
│                   ├── IRepositorioPuja.java
│                   ├── RepositorioSubastaJson.java
│                   └── RepositorioPujaJson.java
│
├── datos/                               # Persistencia - "Base de Datos" JSON
│   ├── usuarios.json                    # Datos críticos y saldos
│   ├── publicaciones.json               # Muro general de servicios
│   ├── transacciones.json               # Historial inmutable de movimientos
│   ├── subastas.json                    # Activos físicos en subasta
│   ├── pujas.json                       # Ofertas de bienes por cada subasta
│   ├── reputacion.json                  # Reseñas y calificaciones
│   └── firmas_seguridad.hash            # Archivo de control de modificaciones (Checksum)
│
└── frontend/                            # Interfaz de Usuario en React
    ├── package.json                     # Dependencias de Node (No se traduce)
    └── src/
        ├── App.jsx                      # Enrutador principal (React Router)
        ├── main.jsx                     # Punto de anclaje
        │
        ├── recursos/                    # Imágenes, logos, fuentes (assets)
        │
        ├── contextos/                   # Manejo de estado global
        │   └── ContextoAutenticacion.jsx # Mantiene la sesión del usuario
        │
        ├── servicios_api/               # Llamadas HTTP al Backend (Axios/Fetch)
        │   ├── clienteHttp.js           # Configuración base de Axios
        │   ├── servicioUsuario.js
        │   ├── servicioSubasta.js
        │   └── servicioPublicacion.js
        │
        ├── componentes/                 # Componentes reutilizables
        │   ├── estructura/              # Componentes de diseño base (Layout)
        │   │   ├── BarraNavegacion.jsx  # Barra superior con saldo de créditos
        │   │   └── BarraLateral.jsx
        │   ├── ui/                      # Elementos genéricos
        │   │   ├── Boton.jsx
        │   │   ├── VentanaModal.jsx
        │   │   └── CalificacionEstrellas.jsx 
        │   └── subasta/                 # Específicos de la variante 3
        │       ├── ListaBienesPuja.jsx  # Lista de alimentos ofrecidos en una puja
        │       └── TarjetaSubasta.jsx   # Tarjeta con la foto del activo
        │
        └── vistas/                      # Pantallas completas (Pages)
            ├── IniciarSesion.jsx        # Login
            ├── RegistroUsuario.jsx      # Valida dominio de correo comunitario
            ├── PanelPerfil.jsx          # Dashboard del usuario
            ├── MuroServicios.jsx        # Épica 3 (Intercambio por créditos)
            └── TableroSubastas.jsx      # Épica 4 (Intercambio físico por bienes)
```

## Espacio de Trabajo Recomendado

Para comenzar a trabajar, se recomienda establecer la carpeta de este proyecto (`scrum-mvc-project`) como el espacio de trabajo activo en tu editor o entorno.

---
*Creado para la fase 1 del proyecto centrado en el Frontend.*
