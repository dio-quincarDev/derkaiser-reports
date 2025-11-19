# Módulo de Autenticación (Auth) - Backend

Este módulo gestiona todo lo relacionado con la autenticación y autorización de usuarios en el sistema. Implementa un sistema seguro de autenticación basado en JWT con soporte completo para registro, verificación de email, recuperación de contraseña y gestión de sesiones.

## Tecnologías

![Java 17](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.11-green?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-blue?style=for-the-badge&logo=spring-security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-red?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue?style=for-the-badge&logo=postgresql&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-v1.18-purple?style=for-the-badge&logo=lombok&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=for-the-badge&logo=apache-maven&logoColor=white)
![BCrypt](https://img.shields.io/badge/BCrypt-Password%20Hashing-yellow?style=for-the-badge&logo=bcrypt&logoColor=white)

## Características Clave

-   **Registro de Usuarios:** Permite a nuevos usuarios crear una cuenta con validación de datos (nombre, apellido, cargo, email, contraseña).
-   **Inicio de Sesión (Login):** Autenticación de usuarios mediante credenciales, generando Access Tokens (JWT de 15 minutos) y Refresh Tokens (JWT de 7 días).
-   **Verificación de Email:** Proceso de verificación de cuenta por correo electrónico con tokens expirables para activar el acceso.
-   **Reenvío de Verificación:** Funcionalidad para reenviar el correo de verificación si el usuario no lo recibió o expiró.
-   **Recuperación de Contraseña:** Flujo completo para que los usuarios puedan resetear su contraseña de forma segura.
-   **Refresco de Tokens:** Mecanismo para obtener nuevos Access Tokens utilizando Refresh Tokens, manteniendo la sesión activa sin necesidad de re-autenticación.
-   **Cierre de Sesión (Logout):** Invalidación de Refresh Tokens para terminar sesiones de forma segura.
-   **Seguridad Robusta:** Implementación de Spring Security con filtros JWT, manejo de excepciones de autenticación/autorización y codificación de contraseñas (BCrypt).
-   **API RESTful:** Endpoints claros y bien definidos para cada operación de autenticación.
-   **Gestión de Roles:** Soporte para roles de usuario (USER, ADMIN) con autorización basada en roles.
-   **Limpieza Automática:** Tarea programada para eliminar tokens expirados cada día a las 2 AM.

## Endpoints API

### Autenticación

| Método | Endpoint | Descripción | Requiere Autenticación |
|--------|----------|-------------|------------------------|
| `POST` | `/api/v1/auth/register` | Registrar un nuevo usuario | No |
| `POST` | `/api/v1/auth/login` | Iniciar sesión | No |
| `GET` | `/api/v1/auth` | Obtener información del usuario autenticado | Sí |
| `GET` | `/api/v1/auth/verify` | Verificar email con token | No |
| `POST` | `/api/v1/auth/resend-verification` | Reenviar correo de verificación | No |
| `POST` | `/api/v1/auth/forgot-password` | Solicitar restablecimiento de contraseña | No |
| `POST` | `/api/v1/auth/reset-password` | Restablecer contraseña con token | No |
| `POST` | `/api/v1/auth/refresh` | Refrescar token de acceso | No |
| `POST` | `/api/v1/auth/logout` | Cerrar sesión | No |

### Ejemplos de Solicitud

#### Registro de Usuario
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "cargo": "Desarrollador",
    "email": "juan.perez@example.com",
    "password": "miContraseña123"
  }'
```

#### Inicio de Sesión
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan.perez@example.com",
    "password": "miContraseña123"
  }'
```

#### Obtener Información del Usuario
```bash
curl -X GET http://localhost:8080/api/v1/auth \
  -H "Authorization: Bearer <access_token>"
```

## Estructura del Módulo

El módulo `auth` sigue una arquitectura modular con las siguientes capas:

-   `commons/dto`: Objetos de Transferencia de Datos (DTO) para peticiones y respuestas.
    - `request/`: DTOs para las solicitudes entrantes
    - `response/`: DTOs para las respuestas salientes
-   `commons/model/entity`: Entidades JPA para la persistencia de usuarios y tokens.
    - `UserEntity`: Representa a un usuario con sus credenciales y roles
    - `RefreshToken`: Almacena tokens de refresco con expiración
    - `VerificationToken`: Tokens temporales para verificación de email
-   `repository`: Interfaces de repositorio para el acceso a datos.
-   `service`: Interfaces de servicio que definen la lógica de negocio.
-   `service/impl`: Implementaciones concretas de los servicios.
-   `config`: Clases de configuración de seguridad, incluyendo el filtro JWT y manejadores de excepciones.
-   `controller`: Controladores REST que exponen los endpoints de la API.

## Configuración de Variables de Entorno

Antes de ejecutar la aplicación, asegúrate de configurar las siguientes variables de entorno:

```bash
# Configuración de la base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=informes_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Configuración JWT
JWT_SECRET=clave_secreta_de_al_menos_32_caracteres_para_jwt

# Configuración del frontend (para enlaces de verificación)
APP_FRONTEND_URL=http://localhost:3000

# Configuración del correo electrónico
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
```

## Token Expiration Times

- **Access Token**: 15 minutos (900,000 ms) - para operaciones diarias
- **Refresh Token**: 7 días (604,800,000 ms) - para refrescar tokens de acceso

## Configuración de Seguridad

El sistema implementa las siguientes medidas de seguridad:

1. **Codificación de Contraseñas**: Usando BCrypt con hashing seguro
2. **Tokens JWT**: Firma segura con clave secreta y validación de expiración
3. **Filtro JWT**: Intercepta solicitudes para validar tokens antes de procesar
4. **Control de Acceso**: Verificación de roles y permisos para endpoints protegidos
5. **Verificación de Email**: Los usuarios deben verificar su correo antes de acceder
6. **Tokens de Refresco**: Almacenados en base de datos con expiración y limpieza automática

## Cómo Empezar

### Requisitos

- Java 17
- Maven 3.x
- PostgreSQL 14+
- Cliente SMTP (correo electrónico)

### Instalación

1. Clona el repositorio:

```bash
git clone <url_repositorio>
cd backend
```

2. Configura las variables de entorno como se indica arriba

3. Compila el proyecto:

```bash
mvn clean install
```

4. Ejecuta la aplicación:

```bash
mvn spring-boot:run
```

O crea un JAR ejecutable:

```bash
mvn clean package
java -jar target/derkaiser-0.0.1-SNAPSHOT.jar
```

### Verificación del Servicio

La API estará disponible en `http://localhost:8080`. Puedes acceder a la documentación de la API en Swagger UI:

- Documentación: `http://localhost:8080/swagger-ui.html`
- Documentación API: `http://localhost:8080/v3/api-docs`

### Documentación de la API

La API está completamente documentada con OpenAPI 3.0 y Swagger. La configuración incluye:
- Autenticación JWT con Bearer Token
- Descripciones detalladas de endpoints
- Modelos de datos documentados
- Parámetros y respuestas esperadas

## Pruebas

Para ejecutar las pruebas unitarias:

```bash
mvn test
```

Para ejecutar pruebas de integración:

```bash
mvn verify
```

## Contribución

1. Haz un fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/NombreDeTuFeature`)
3. Haz commit de tus cambios (`git commit -m 'Agrega feature X'`)
4. Haz push a la rama (`git push origin feature/NombreDeTuFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo `LICENSE` para más detalles.

## Soporte

Si tienes problemas o preguntas sobre la implementación de autenticación:

- Revisa la documentación en Swagger UI
- Consulta los logs de la aplicación
- Revisa los patrones de manejo de errores en `GlobalExceptionHandler`
