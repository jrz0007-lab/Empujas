# EMpujas - Información del Proyecto

## Figma Design

**URL del diseño en Figma:** https://www.figma.com/file/azeenMiKmaZLfQAISscuoE/Retos-en-l%C3%ADnea-app

### Diseño Visual

El proyecto sigue un diseño moderno y funcional basado en:

- **Paleta de colores:** Púrpura (#7C3AED) como color principal, verde (#10B981) para estados completados, blanco y grises para fondos y tarjetas
- **Tipografía:** Arial, Helvetica, sans-serif
- **Diseño responsive:** Adaptado para escritorio, tableta y móvil
- **Componentes:** Tarjetas de retos, barras de progreso, paneles de usuario, formularios, modales de donación

### Wireframes (Pantallas)

1. **Landing Page** (085912) - Página principal con hero section y tarjetas de retos
2. **Inicio de Sesión** (090006) - Formulario de login con email y contraseña
3. **Detalle de Reto Activo** (090028) - Vista de reto con progreso de financiación
4. **Detalle de Reto Completado** (090045) - Reto completado con video de verificación
5. **Crear Nuevo Reto** (090102) - Formulario de creación de reto
6. **Panel de Usuario** (090113) - Dashboard con resumen y tabs
7. **Modal de Donación** (090128) - Ventana modal para donar
8. **Panel de Usuario Vacío** (090145) - Dashboard con estados vacíos

## Tecnologías (Examen)

- Frontend: HTML5, CSS3 (Flexbox/Grid), JavaScript (Fetch API + Promesas)
- Backend: Jakarta EE 10 (Servlets)
- Base de datos: MySQL 8.0
- Contenedores: Docker, Docker Compose
- Despliegue: Railway (Free Plan)

## Estructura MVC

```
challenge-app/
├── src/main/java/com/challenge/
│   ├── controller/       (Servlets - 9 controladores)
│   └── model/           (Modelos - 12 clases)
├── src/main/webapp/
│   ├── index.html        (Página principal)
│   ├── login.html        (Inicio de sesión)
│   ├── register.html     (Registro)
│   ├── create-challenge.html (Crear reto)
│   ├── dashboard.html    (Panel de usuario)
│   ├── css/style.css
│   ├── js/app.js
│   └── WEB-INF/web.xml
└── pom.xml
```

## Despliegue Local (Docker)

```bash
docker compose up --build
# Acceder: http://localhost:8080
# phpMyAdmin: http://localhost:8081
```

## Despliegue en Railway (Examen)

1. Crear cuenta en railway.app
2. Conectar repositorio GitHub
3. Añadir plugin MySQL
4. Configurar variables de entorno
5. Desplegar con Docker Compose
