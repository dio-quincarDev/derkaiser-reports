
# 📋 Sistema de Informes Diarios - Infoplazas AIP

Sistema web de gestión de informes diarios para colaboradores de Infoplazas AIP (Asociación de Interés Público), Sede Regional Chiriquí.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-green.svg)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-Private-red.svg)]()

---

## 📖 Descripción

Plataforma que permite a Enlaces Regionales, Facilitadores y Supervisores registrar, gestionar y generar informes de sus actividades diarias en formato Word y PDF.

### ✨ Características Principales

- ✅ **Autenticación Segura**: JWT + Refresh Tokens con rotación
- ✅ **Verificación de Email**: Obligatoria para activación de cuenta
- ✅ **Recuperación de Contraseña**: Flujo seguro con tokens de un solo uso
- ✅ **Gestión de Informes**: Diarios, semanales y mensuales
- ✅ **Generación de Documentos**: Exportación a Word (.docx) y PDF
- ✅ **Catálogo de Infoplazas**: 200+ ubicaciones de Chiriquí, Ngäbe-Buglé y Bocas del Toro
- ✅ **Sistema de Roles**: ADMIN y USER con permisos diferenciados
- ✅ **Rate Limiting**: Protección contra fuerza bruta

---

## 🏗️ Arquitectura

```
┌──────────────┐      HTTP/REST      ┌──────────────┐      JDBC      ┌──────────────┐
│              │  ←─────────────────→ │              │ ←─────────────→│              │
│   Frontend   │    JWT Bearer       │   Backend    │   Flyway       │  PostgreSQL  │
│  Vue 3 +     │                     │  Spring Boot │                │      15      │
│   Quasar     │                     │      3.2     │                │              │
│              │                     │              │                │              │
└──────────────┘                     └──────────────┘                └──────────────┘
   Puerto 8080                          Puerto 8081                     Puerto 5432
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

- **Docker** 20.x o superior
- **Docker Compose** 2.x o superior
- (Opcional) **Java 17** y **Node.js 18+** para desarrollo local

### Instalación con Docker

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-organizacion/informes-aip.git
cd informes-aip

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales

# 3. Levantar los servicios
docker-compose up -d

# 4. Verificar que todo esté corriendo
docker-compose ps
```

### Acceso a la Aplicación

- **Frontend**: [http://localhost:8080](http://localhost:8080)
- **Backend API**: [http://localhost:8081](http://localhost:8081)
- **Health Check**: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

---

## 📦 Estructura del Proyecto

```
informes-aip/
├── informes-aip-backend/       # API REST (Spring Boot)
│   ├── src/main/
│   │   ├── java/
│   │   │   └── com/derkaiser/auth/
│   │   │       ├── config/             # Configuraciones de seguridad
│   │   │       ├── commons/            # DTOs y entidades
│   │   │       ├── repository/         # Capa de datos
│   │   │       ├── service/            # Lógica de negocio
│   │   │       └── controller/         # Endpoints REST
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/           # Scripts Flyway
│   ├── Dockerfile
│   └── pom.xml
│
├── informes-aip-frontend/      # Interfaz Web (Vue 3 + Quasar)
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── stores/                     # Pinia state management
│   │   ├── services/                   # Llamadas API
│   │   └── router/
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🔐 Seguridad

### Medidas Implementadas

| Característica | Descripción |
|----------------|-------------|
| **Password Hashing** | BCrypt con strength 10 |
| **JWT** | Access token (15 min) + Refresh token (7 días) |
| **Token Blacklist** | Invalidación inmediata en logout |
| **Token Rotation** | Refresh tokens de un solo uso |
| **Email Verification** | Obligatoria antes de acceder al sistema |
| **Rate Limiting** | Máx. 5 intentos en 5 min (login, registro, reset) |
| **CORS** | Configurado para orígenes permitidos |
| **HTTPS** | Obligatorio en producción |

### Flujo de Autenticación

```
1. Registro → Email de verificación → Verificar cuenta
2. Login → Access Token + Refresh Token
3. Access Token expira (15 min) → Refresh automático
4. Logout → Tokens invalidados en blacklist
```

---

## 🛠️ Tecnologías

### Backend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje base |
| Spring Boot | 3.2.x | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | ORM |
| PostgreSQL | 15 | Base de datos |
| Flyway | Latest | Migraciones de BD |
| JWT (JJWT) | 0.12.3 | Tokens de autenticación |
| JavaMailSender | - | Envío de emails |
| Lombok | Latest | Reducción de boilerplate |

### Frontend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Vue.js | 3.x | Framework UI |
| Quasar | 2.x | Componentes UI |
| Pinia | Latest | State management |
| Axios | Latest | HTTP client |
| Vue Router | 4.x | Navegación |

### DevOps

- **Docker** & **Docker Compose**: Containerización
- **Maven**: Build backend
- **Vite**: Build frontend

---

## 📚 API Endpoints

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/register` | Registrar usuario | No |
| `GET` | `/api/auth/verify` | Verificar email | No |
| `POST` | `/api/auth/login` | Iniciar sesión | No |
| `POST` | `/api/auth/refresh` | Refrescar access token | No |
| `POST` | `/api/auth/logout` | Cerrar sesión | Sí |
| `POST` | `/api/auth/forgot-password` | Solicitar reset | No |
| `POST` | `/api/auth/reset-password` | Resetear contraseña | No |

### Informes (Próximamente)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/informes` | Crear informe | Sí |
| `GET` | `/api/informes` | Listar informes | Sí |
| `GET` | `/api/informes/{id}` | Obtener informe | Sí |
| `GET` | `/api/informes/{id}/word` | Descargar Word | Sí |
| `GET` | `/api/informes/{id}/pdf` | Descargar PDF | Sí |

**Documentación completa**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) *(Próximamente)*

---

## 🧪 Testing

```bash
# Backend - Tests unitarios
cd informes-aip-backend
mvn test

# Backend - Tests de integración
mvn verify

# Backend - Coverage report
mvn clean test jacoco:report
# Ver reporte: target/site/jacoco/index.html

# Frontend - Tests
cd informes-aip-frontend
npm run test:unit
```

---

## 🔧 Configuración

### Variables de Entorno

Crear archivo `.env` basado en `.env.example`:

```bash
# Database
POSTGRES_DB=informes_aip
POSTGRES_USER=aip_user
POSTGRES_PASSWORD=tu-password-seguro

# JWT
JWT_SECRET=tu-secreto-jwt-minimo-32-caracteres

# Email (Gmail)
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password

# Frontend
FRONTEND_URL=http://localhost:8080
```

**⚠️ Importante**: 
- NO commitear `.env` al repositorio
- Cambiar todos los secrets en producción
- Para Gmail, usar [App Passwords](https://support.google.com/accounts/answer/185833)

---

## 📝 Desarrollo Local

### Backend

```bash
cd informes-aip-backend

# Levantar solo PostgreSQL
docker-compose up -d postgres

# Ejecutar aplicación
mvn spring-boot:run

# Hot reload (con spring-boot-devtools)
# Los cambios se recargan automáticamente
```

### Frontend

```bash
cd informes-aip-frontend

# Instalar dependencias
npm install

# Modo desarrollo (hot reload)
npm run dev

# Build para producción
npm run build
```

---

## 🐛 Troubleshooting

### Error: "CORS policy blocked"

**Solución**: Verificar que el frontend URL esté en la lista de orígenes permitidos en `CorsConfig.java`

### Error: "JWT expired"

**Solución**: El frontend debe implementar refresh automático usando el refresh token

### Error: "Email no se envía"

**Solución**: 
1. Verificar credenciales SMTP en `.env`
2. Para Gmail, usar App Password (no contraseña normal)
3. Verificar logs en `docker-compose logs backend`

### Error: "Flyway checksum mismatch"

**Solución** (solo desarrollo):
```bash
docker-compose down -v
docker-compose up -d
```

---

## 📄 Licencia

Este proyecto es privado y de uso exclusivo para Infoplazas AIP.

---

## 👥 Equipo

**Desarrollador Principal**: German Castillero  
**Organización**: Infoplazas AIP - Sede Regional Chiriquí  
**Contacto**: german.castillero@aip.gob.pa

---

## 📞 Soporte

Para reportar problemas o solicitar nuevas funcionalidades:

1. Crear un issue en el repositorio
2. Contactar al equipo de desarrollo
3. Email: soporte@aip.gob.pa

---

## 🗓️ Roadmap

- [x] Sistema de autenticación completo
- [x] Verificación de email
- [x] Recuperación de contraseña
- [x] Rate limiting
- [ ] Gestión de informes diarios
- [ ] Generación de documentos Word/PDF
- [ ] Informes semanales y mensuales
- [ ] Dashboard con estadísticas
- [ ] Sistema de notificaciones
- [ ] Modo oscuro
- [ ] App móvil (PWA)

---

**Última actualización**: Diciembre 2024  
**Versión**: 1.0.0-beta
```

---

¿Necesitas que ajuste algo del README o lo hago más extenso/corto en alguna sección específica?
