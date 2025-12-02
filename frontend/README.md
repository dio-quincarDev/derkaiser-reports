# Frontend de la Aplicación de Informes

Este directorio contiene el código fuente para el frontend de la aplicación de informes, construido con el framework Quasar.

![Quasar](https://img.shields.io/badge/Quasar-v2.x-blue.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-v3.x-green.svg)
![Pinia](https://img.shields.io/badge/Pinia-v2.x-yellow.svg)
![Axios](https://img.shields.io/badge/Axios-Networking-red.svg)

## Descripción

El frontend proporciona la interfaz de usuario para interactuar con el sistema de informes. Incluye un sistema de autenticación completo, navegación protegida y una base para futuros módulos de la aplicación.

---

## Paleta de Colores

La aplicación utiliza la siguiente paleta de colores para mantener una identidad visual consistente:

| Color           | Hex Code  | Uso Principal        |
| --------------- | --------- | -------------------- |
| **Primary**     | `#6366F1` | Botones, links, UI   |
| **Primary Dark**| `#4F46E5` | Variación oscura     |
| **Primary Light**| `#818CF8` | Variación clara      |
| **Secondary**   | `#EC4899` | Elementos secundarios|
| **Accent**      | `#14B8A6` | Énfasis, acentos     |
| **Success**     | `#10B981` | Notificaciones éxito |
| **Danger**      | `#F43F5E` | Alertas de error     |
| **Warning**     | `#F59E0B` | Advertencias         |
| **Info**        | `#3B82F6` | Mensajes informativos|

---

## Primeros Pasos

Sigue estas instrucciones para poner en marcha el entorno de desarrollo local.

### Prerrequisitos

-   [Node.js](https://nodejs.org/) (versión 16.x o superior)
-   [NPM](https://www.npmjs.com/) o [Yarn](https://yarnpkg.com/)

### Instalación

1.  Navega a la carpeta `frontend`:
    ```bash
    cd frontend
    ```
2.  Instala las dependencias del proyecto:
    ```bash
    npm install
    ```
    o si usas Yarn:
    ```bash
    yarn
    ```

### Ejecutar en Modo Desarrollo

Para iniciar el servidor de desarrollo con recarga en caliente:

```bash
npx quasar dev
```

La aplicación estará disponible en `http://localhost:9000`.

---

## Build para Producción

Para compilar la aplicación para un entorno de producción:

```bash
npx quasar build
```

Esto generará una carpeta `dist/spa` con los archivos estáticos listos para ser desplegados en un servidor web.

---

## Estructura del Proyecto

El proyecto sigue la estructura estándar de Quasar, con algunas convenciones clave:

-   `src/boot/`: Archivos de inicialización de Quasar. Aquí se configura `axios`.
-   `src/stores/`: Contiene los módulos de estado de Pinia. `auth-module.js` gestiona todo el estado de autenticación.
-   `src/service/`: Servicios que encapsulan la comunicación con la API del backend. `auth-service.js` contiene todas las llamadas a los endpoints de autenticación.
-   `src/router/`: Contiene la configuración de Vue Router. `guard.js` protege las rutas que requieren autenticación.
-   `src/layouts/`: Componentes de layout principales (`MainLayout.vue` para usuarios autenticados y `AuthLayout.vue` para las páginas de login/registro).
-   `src/pages/`: Las páginas (vistas) de la aplicación. Las páginas de autenticación se encuentran en `src/pages/auth/`.
-   `src/css/`: Archivos de estilo. `quasar.variables.scss` define la paleta de colores de la aplicación.
-   `src/components/`: Componentes de Vue reutilizables.

---

## Funcionalidades Implementadas

-   **Sistema de Autenticación Completo**:
    -   Inicio de sesión y registro.
    -   Cierre de sesión.
    -   Manejo de tokens JWT (Access y Refresh).
    -   Refresco automático de tokens de acceso expirados mediante interceptores de `axios`.
-   **Protección de Rutas**: El archivo `router/guard.js` asegura que solo los usuarios autenticados puedan acceder a las rutas protegidas.
-   **Gestión de Estado Centralizada**: Uso de Pinia para un manejo de estado predecible y centralizado.