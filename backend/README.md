# Módulo de Autenticación (Auth) - Backend

Este módulo gestiona todo lo relacionado con la autenticación y autorización de usuarios en el sistema.

## Tecnologías

![Java 17](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-blue?style=for-the-badge&logo=spring-security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-red?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue?style=for-the-badge&logo=postgresql&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-v1.18-purple?style=for-the-badge&logo=lombok&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=for-the-badge&logo=apache-maven&logoColor=white)

## Características Clave

-   **Registro de Usuarios:** Permite a nuevos usuarios crear una cuenta con validación de datos.
-   **Inicio de Sesión (Login):** Autenticación de usuarios mediante credenciales, generando Access Tokens (JWT) y Refresh Tokens.
-   **Verificación de Email:** Proceso de verificación de cuenta por correo electrónico para activar el acceso.
-   **Reenvío de Verificación:** Funcionalidad para reenviar el correo de verificación si el usuario no lo recibió o expiró.
-   **Recuperación de Contraseña:** Flujo completo para que los usuarios puedan resetear su contraseña de forma segura.
-   **Refresco de Tokens:** Mecanismo para obtener nuevos Access Tokens utilizando Refresh Tokens, manteniendo la sesión activa sin necesidad de re-autenticación.
-   **Cierre de Sesión (Logout):** Invalidación de Refresh Tokens para terminar sesiones de forma segura.
-   **Seguridad Robusta:** Implementación de Spring Security con filtros JWT, manejo de excepciones de autenticación/autorización y codificación de contraseñas (BCrypt).
-   **API RESTful:** Endpoints claros y bien definidos para cada operación de autenticación.

## Estructura del Módulo

El módulo `auth` sigue una arquitectura modular con las siguientes capas:

-   `commons/dto`: Objetos de Transferencia de Datos (DTOs) para peticiones y respuestas.
-   `commons/model/entity`: Entidades JPA para la persistencia de usuarios y tokens.
-   `repository`: Interfaces de repositorio para el acceso a datos.
-   `service`: Interfaces de servicio que definen la lógica de negocio.
-   `service/impl`: Implementaciones concretas de los servicios.
-   `config`: Clases de configuración de seguridad, incluyendo el filtro JWT y manejadores de excepciones.
-   `controller`: Controladores REST que exponen los endpoints de la API.

## Cómo Empezar

Para levantar el backend, asegúrate de tener configurada una base de datos PostgreSQL y las variables de entorno necesarias para JWT y el envío de correos. Luego, puedes usar Maven:

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

Para más detalles sobre la configuración de la base de datos y variables de entorno, consulta la documentación general del proyecto (ej. `GEMINI.md` o `application.properties`).
