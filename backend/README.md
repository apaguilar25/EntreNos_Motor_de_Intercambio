# Backend (Persistencia JSON + MVC)

Esta es la carpeta asignada al Backend. Aunque su implementación final está por definirse, utilizará persistencia basada en archivos **JSON**.

## Estructura de Carpetas (Patrón MVC)

- **`src/models/`**: Define los esquemas de datos y se encarga de leer/escribir en los archivos JSON de la base de datos local.
- **`src/controllers/`**: Contiene los controladores que reciben las peticiones HTTP, aplican la lógica de negocio necesaria y devuelven la respuesta adecuada.
- **`src/views/`**: En el contexto de un API Backend, representa las rutas/endpoints, formateadores de respuesta o serializadores de datos.
- **`src/data/`**: Contiene los archivos `.json` que servirán como persistencia de datos (nuestra base de datos local ligera).
