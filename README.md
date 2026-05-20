# Scrum MVC Project (React Frontend + JSON Persistence Backend)

Este es un proyecto completo estructurado bajo la arquitectura **Model-View-Controller (MVC)** tanto para el Frontend como para el Backend.

## Estructura del Proyecto

El proyecto está organizado de la siguiente manera:

```text
scrum-mvc-project/
├── backend/                  # Código del Backend (Persistencia JSON)
│   ├── src/
│   │   ├── controllers/      # Controladores: Lógica de negocio y manejo de peticiones
│   │   ├── models/           # Modelos: Definición de datos e interacción con la persistencia JSON
│   │   ├── views/            # Vistas/Rutas: Definición de endpoints y respuestas
│   │   └── data/             # Archivos JSON de persistencia (Base de Datos)
│   └── README.md
│
├── frontend/                 # Código del Frontend (React)
│   ├── src/
│   │   ├── controllers/      # Controladores: Hooks personalizados, manejadores de eventos y lógica de estado
│   │   ├── models/           # Modelos: Servicios de API, estado global, contextos y tipos de datos
│   │   └── views/            # Vistas: Componentes de React, páginas y elementos de UI
│   └── README.md
│
└── README.md                 # Documentación general del proyecto
```

## Espacio de Trabajo Recomendado

Para comenzar a trabajar, se recomienda establecer la carpeta de este proyecto (`scrum-mvc-project`) como el espacio de trabajo activo en tu editor o entorno.

---
*Creado para la fase 1 del proyecto centrado en el Frontend.*
