# EMpujas — Documentación completa

## Índice

1. [Descripción general](#1-description-general)
2. [Arquitectura](#2-arquitectura)
3. [Flujo de operaciones](#3-flujo-de-operaciones)
   - 3.1 Registro de usuario
   - 3.2 Inicio de sesión
   - 3.3 Creación de reto
   - 3.4 Listado de retos
   - 3.5 Detalle de reto
   - 3.6 Donación
   - 3.7 Finalización de reto
   - 3.8 Favoritos (toggle)
   - 3.9 Listado de favoritos
   - 3.10 Reporte de reto
   - 3.11 Perfil de usuario
   - 3.12 Panel de administración
   - 3.13 Eliminación de reto (admin)
   - 3.14 Baneo de usuario (admin)
   - 3.15 Desbaneo de usuario (admin)
   - 3.16 Inicialización de base de datos
4. [Diccionario alfabético por lenguaje](#4-diccionario-alfabetico-por-lenguaje)
   - 4.1 Java
   - 4.2 JavaScript
   - 4.3 SQL
   - 4.4 CSS
   - 4.5 HTML
5. [Base de datos](#5-base-de-datos)
6. [Despliegue](#6-despliegue)

---

## 1. Descripción general

**EMpujas** es una aplicación web Jakarta EE 10 (MVC) para crear retos personales con financiación colectiva. Los usuarios crean retos con una meta económica, reciben donaciones de otros usuarios, y al alcanzar la meta publican un video de logro. Incluye panel de administración con gestión de reportes, baneos y notificaciones.

- **Frontend:** HTML5, CSS3, JavaScript vanilla (fetch + Promises)
- **Backend:** Jakarta Servlet 6.0, MySQL 8.0, Gson 2.11
- **Build:** Maven 3.9, Java 21, empaquetado WAR
- **Runtime:** Apache Tomcat 10.1
- **Infraestructura:** Docker Compose (app + mysql + phpmyadmin), Railway

---

## 2. Arquitectura

```
NAVEGADOR (HTML + CSS + JS)
       |
       | fetch() JSON
       v
SERVLETS (14 controladores)  @WebServlet
       |
       v
MODELOS (18 clases DAO/de negocio)
       |
       v
MySQL 8.0 (6 tablas)
```

- **Sin frameworks JS** — JavaScript vanilla con `fetch()` y Promises
- **Sin JPA/Hibernate** — JDBC directo desde `ConexionBD.java`
- **Sin XML de servlets** — `@WebServlet` annotations
- **Session:** HTTP session guarda `userId` y `username`; frontend replica en `sessionStorage`
- **JSON en todas las respuestas** — `Gson` para serialización
- **Admin automático** — emails terminados en `@pujas.com` se registran como admin

---

## 3. Flujo de operaciones

### 3.1 Registro de usuario

```
Inicio: register.html -> formulario #registerForm
  1. Usuario completa username, email, password, confirm
  2. JavaScript valida que password == confirm
  3. fetch POST /api/register con JSON {username, email, password}
  4. RegisterServlet.doPost()
     -> Lee JSON con Gson
     -> UserRegistrar.registrar(username, email, password)
        -> INSERT INTO users (username, email, password, is_admin)
           VALUES (?, ?, ?, email.endsWith("@pujas.com") ? 1 : 0)
        -> RETURN_GENERATED_KEYS para obtener id
     -> Construye JSON {ok:true, user:{id, username, email, isAdmin}}
  5. app.js recibe respuesta:
     -> sessionStorage.setItem('userId', ...)
     -> sessionStorage.setItem('username', ...)
     -> sessionStorage.setItem('userEmail', ...)
     -> sessionStorage.setItem('isAdmin', ...)
     -> window.location.href = '/dashboard.html'
  6. DatabaseInitializer.contextInitialized() crea tablas si no existen
Fin: dashboard.html cargado con sesión activa
```

### 3.2 Inicio de sesión

```
Inicio: login.html -> formulario #loginForm
  1. Usuario introduce email y password
  2. fetch POST /api/login con JSON {email, password}
  3. LoginServlet.doPost()
     -> UserAuthenticator.autenticar(email, password)
        -> SELECT * FROM users WHERE email=? AND password=?
        -> Si banned=1 -> devuelve null (LoginServlet responde error)
        -> Si no -> devuelve User
     -> Si ok: req.getSession().setAttribute("userId", user.id)
              req.getSession().setAttribute("username", user.username)
     -> JSON {ok:true, user:{id, username, email, isAdmin}}
  4. app.js guara en sessionStorage y redirige a /dashboard.html
Fin: dashboard.html cargado
```

### 3.3 Creación de reto

```
Inicio: create-challenge.html -> formulario #createChallengeForm
  1. Usuario rellena título, descripción, video URL, imagen URL, meta €
  2. Live preview se actualiza con cada input (updatePreview())
  3. Submit:
     -> Valida title, description, goalAmount > 0
     -> Verifica getUserId() existe
     -> fetch POST /api/create-challenge con JSON
 4. CreateChallengeServlet.doPost()
     -> UserManager.estaBaneado(creatorId) -> 403 si baneado
     -> ChallengeCreator.crear(title, description, goalAmount, creatorId, videoUrl, imageUrl)
        -> INSERT INTO challenges (...) VALUES (...)
        -> RETURN_GENERATED_KEYS
     -> JSON {ok:true, challenge:{id, title, ...}}
  5. app.js muestra éxito, redirige a /dashboard.html tras 1.5s
Fin: dashboard.html con nuevo reto visible
```

### 3.4 Listado de retos

```
Inicio: index.html carga
  1. DOMContentLoaded -> updateNavbar() -> cargarRetos()
  2. fetch GET /api/challenges?userId=X
  3. ListChallengesServlet.doGet()
     -> Parámetros: ?status=, ?creatorId=, ?userId=
     -> ChallengeLister.listarTodos(userId)
        -> SELECT c.*, u.username AS creator_name,
                    (SELECT COUNT(*) FROM donations WHERE challenge_id=c.id) AS supporter_count,
                    (SELECT COUNT(*) FROM favorites WHERE challenge_id=c.id AND user_id=?) AS favorited,
                    (SELECT COUNT(*) FROM donations WHERE challenge_id=c.id AND user_id=?) AS has_donated
           FROM challenges c JOIN users u ON c.creator_id = u.id
           ORDER BY c.created_at DESC
     -> Si ?status=: WHERE c.status=?
     -> Si ?creatorId=: WHERE c.creator_id=?
     -> JSON {ok:true, total:N, resultados:[Challenge...]}
  4. app.js separa activeList / completedList según challenge.status
  5. createChallengeCard() genera HTML para cada reto:
     -> Imagen, favorito ♡/♥, título, creador, fecha, descripción
     -> Barra de progreso, stats, botones
     -> Botones admin si getIsAdmin(): "Eliminar", "Bloquear"
     -> Adjunta event listeners a cada botón
  6. Si hay pendingChallengeId en sessionStorage, abre openDetail()
Fin: Retos visibles en #activeChallengesGrid y #completedChallengesGrid
```

### 3.5 Detalle de reto

```
Inicio: click en "Ver Detalles" de una tarjeta
  1. openDetail(challengeId)
  2. fetch GET /api/challenge?id=X&userId=Y
  3. ChallengeDetailServlet.doGet()
     -> ChallengeFinder.buscarPorId(id, userId)
        -> SELECT c.*, u.username AS creator_name FROM challenges c JOIN users u ...
        -> Consulta favorited y has_donated igual que ChallengeLister
     -> ChallengeFinder.buscarDonaciones(challengeId)
        -> SELECT * FROM donations WHERE challenge_id=? ORDER BY created_at DESC
     -> JSON {ok:true, challenge:..., donations:[...], hasDonated:bool}
  4. Renderiza .detail-layout:
     -> .detail-info: título, creador, imagen, descripción, video, donaciones
     -> Si completado y (hasDonated OR admin OR creator):
        -> Video de logro, mensaje de agradecimiento
        -> Si creator y faltan datos: botón "Añadir Video de Logro"
        -> Si creator y ya hay datos: botón "Editar Video / Agradecimiento"
     -> Si completado y no ha donado ni es admin/creator: candado
     -> .detail-panel: progreso, stats, botón "Apoyar" (si activo y no creator)
     -> Admin: botones "Eliminar" y "Bloquear"
  5. Adjunta event listeners a botones del panel
Fin: Modal #detailModal visible con toda la info
```

### 3.6 Donación

```
Inicio: click en "Apoyar" de una tarjeta o detalle
  1. openDonate(challengeId, title)
  2. Modal #donateModal se muestra con formulario
  3. Usuario rellena nombre, cantidad €, método pago (ficticio)
  4. fetch POST /api/donate con JSON {challengeId, donorName, amount, userId}
  5. DonateServlet.doPost()
     -> Valida amount >= 1
     -> Valida que userId != creator_id del reto (no auto-donación)
     -> UserManager.estaBaneado(userId) -> 403 si baneado
     -> DonationProcessor.donar(challengeId, donorName, amount, userId)
        -> Connection.setAutoCommit(false) -- TRANSACCIÓN
        -> 1. SELECT goal_amount, current_amount FROM challenges WHERE id=? FOR UPDATE
        -> 2. Valida current_amount + amount <= goal_amount
        -> 3. INSERT INTO donations (challenge_id, donor_name, amount, user_id) VALUES (...)
        -> 4. UPDATE challenges SET current_amount = current_amount + ? WHERE id=?
        -> 5. Si current_amount >= goal_amount:
              UPDATE challenges SET status='completed' WHERE id=?
        -> 6. Si userId != null:
              INSERT IGNORE INTO favorites (user_id, challenge_id) VALUES (?, ?)
        -> Connection.commit()
     -> JSON {ok:true, donation:{...}, completed:bool}
  6. app.js muestra éxito, cierra modal tras 1.5s, recarga retos
Fin: Donación registrada, reto actualizado
```

### 3.7 Finalización de reto

```
Inicio: desde detalle de reto completado -> botón "Añadir/Editar Video"
  1. abrirCompletionModal(challengeId, currentVideoUrl, currentMessage, detailModal)
     -> Crea dinámicamente #completionModal si no existe
     -> Muestra formulario con video URL y mensaje de agradecimiento
  2. Submit -> guardarCompletion(challengeId, modal, detailModal)
     -> Valida al menos video o mensaje presente
     -> fetch POST /api/complete-challenge con JSON:
        {challengeId, userId, completionVideoUrl, thankYouMessage}
  3. CompleteChallengeServlet.doPost()
     -> Verifica que el reto pertenece al userId
     -> Verifica que el reto está en status='completed'
     -> UPDATE challenges SET completion_video_url=?, thank_you_message=? WHERE id=?
     -> Verifica banned
     -> JSON {ok:true, challenge:{...}}
  4. app.js cierra modales, recarga retos
Fin: Video y mensaje guardados
```

### 3.8 Favoritos (toggle)

```
Inicio: click en ♡/♥ de una tarjeta de reto
  1. toggleFav(challengeId, btn)
     -> Si no userId -> redirige a /login.html
     -> fetch POST /api/toggle-favorite con JSON {userId, challengeId}
  2. ToggleFavoriteServlet.doPost()
     -> UserManager.estaBaneado(userId) -> 403
     -> FavoriteManager.toggle(userId, challengeId)
        -> Verifica si existe: SELECT COUNT(*) FROM favorites WHERE user_id=? AND challenge_id=?
        -> Si existe: DELETE FROM favorites WHERE user_id=? AND challenge_id=?
        -> Si no: INSERT INTO favorites (user_id, challenge_id) VALUES (?, ?)
     -> JSON {ok:true, favorited:bool}
  3. app.js alterna clase .fav-active y contenido ♡/♥
Fin: Favorito añadido o eliminado
```

### 3.9 Listado de favoritos

```
Inicio: dashboard.html -> tab "Favoritos"
  1. cargarRetosDashboard('favorites', userId)
  2. fetch GET /api/favorites?userId=X
  3. ListFavoritesServlet.doGet()
     -> UserManager.estaBaneado(userId) -> 403
     -> ChallengeLister.listarFavoritos(userId)
        -> SELECT c.*, u.username AS creator_name
           FROM challenges c
           JOIN users u ON c.creator_id = u.id
           JOIN favorites f ON c.id = f.challenge_id
           WHERE f.user_id = ?
           ORDER BY f.created_at DESC
     -> JSON {ok:true, total:N, resultados:[Challenge...]}
  4. app.js renderiza tarjetas en #dashboardGrid
Fin: Grid de favoritos visible
```

### 3.10 Reporte de reto

```
Inicio: click en "Reportar" de una tarjeta de reto
  1. abrirReporteModal(challengeId, title)
     -> Muestra #reportModal con textarea para motivo
  2. Submit -> fetch POST /api/report-challenge con JSON
     {challengeId, userId, reason}
  3. ReportChallengeServlet.doPost()
     -> UserManager.estaBaneado(userId) -> 403
     -> ReportManager.reportar(challengeId, reporterId, reason)
        -> INSERT INTO reports (challenge_id, reporter_id, reason) VALUES (?, ?, ?)
     -> JSON {ok:true}
  4. app.js muestra éxito, cierra modal tras 2s
Fin: Reporte guardado en BD
```

### 3.11 Perfil de usuario

```
Inicio: dashboard.html carga
  1. cargarDashboard() se ejecuta desde DOMContentLoaded
  2. fetch GET /api/profile?userId=X
  3. ProfileServlet.doGet()
     -> UserManager.estaBaneado(userId) -> 403
     -> ProfileLoader.cargarResumen(userId)
        -> SELECT COUNT(*) AS totalCreated FROM challenges WHERE creator_id=?
        -> SELECT COUNT(*) AS totalCompleted FROM challenges WHERE creator_id=? AND status='completed'
        -> SELECT COALESCE(SUM(amount), 0) AS totalRaised FROM donations WHERE challenge_id IN (SELECT id FROM challenges WHERE creator_id=?)
     -> JSON {ok:true, resumen:{totalCreated, totalCompleted, totalRaised}}
  4. fetch GET /api/favorites?userId=X (paralelo)
     -> JSON {ok:true, total:N}
  5. app.js actualiza summary cards
  6. Activa tab y carga retos del dashboard
Fin: Panel de usuario completo
```

### 3.12 Panel de administración

```
Inicio: admin.html carga
  1. admin.js DOMContentLoaded:
     -> Verifica getUserId() y getIsAdmin()
     -> Si no es admin: redirige a /
     -> updateNavbar()
     -> cargarReportesAdmin() (tab activo por defecto)
  2. Tabs: click en botones [Reportes] [Baneados] [Notificaciones]
     -> Alterna clase btn-primary / btn-outline
     -> Carga contenido según data-tab
  3. AdminServlet.doGet()
     -> /api/admin/reports:
        AdminActionManager.obtenerReportes()
        -> SELECT r.*, u.username AS reporter_name, u.email AS reporter_email,
                  c.title AS challenge_title
           FROM reports r
           JOIN users u ON r.reporter_id = u.id
           JOIN challenges c ON r.challenge_id = c.id
           ORDER BY r.created_at DESC LIMIT 100
     -> /api/admin/banned-users:
        UserManager.obtenerBaneados()
        -> SELECT id, username, email, ban_reason FROM users WHERE banned=1
     -> /api/admin/actions:
        AdminActionManager.obtenerAcciones()
        -> SELECT a.*, u.username AS admin_name
           FROM admin_actions a JOIN users u ON a.admin_id = u.id
           ORDER BY a.created_at DESC LIMIT 100
  4. Renderizado:
     -> app.js renderiza listas con .admin-list (cards)
     -> admin.js renderiza tablas con .admin-table (tablas con thead/tbody)
  5. Búsqueda en vivo: filtra baneados por username/email
Fin: Panel admin operativo
```

### 3.13 Eliminación de reto (admin)

```
Inicio: admin click en "Eliminar" (tarjeta o detalle)
  1. openReasonModal('delete', creatorName)
     -> Muestra #reasonModal con prompt obligatorio
     -> Promise se resuelve con el texto del motivo
  2. eliminarReto(challengeId, reason)
     -> fetch POST /api/delete-challenge con JSON
        {challengeId, userId, reason}
  3. DeleteChallengeServlet.doPost()
     -> UserManager.esAdmin(userId) -> 403 si no es admin
     -> ChallengeDeleter.eliminarComoAdmin(challengeId)
        -> DELETE FROM challenges WHERE id=?
     -> AdminActionManager.registrarAccion(adminId, "delete", targetUserId,
        targetUserEmail, challengeId, reason)
        -> INSERT INTO admin_actions (...) VALUES (...)
     -> JSON {ok:true}
  4. app.js recarga retos y dashboard
Fin: Reto eliminado, acción registrada
```

### 3.14 Baneo de usuario (admin)

```
Inicio: admin click en "Bloquear" (tarjeta o detalle)
  1. openReasonModal('ban', creatorName)
     -> Muestra #reasonModal
  2. banearUsuario(targetUserId, reason)
     -> fetch POST /api/ban-user con JSON
        {adminUserId, targetUserId, reason}
  3. BanUserServlet.doPost()
     -> UserManager.esAdmin(adminUserId) -> 403
     -> Impide auto-baneo (adminUserId == targetUserId)
     -> UserManager.banear(adminUserId, targetUserId, reason)
        -> UserManager.eliminarRetosDeUsuario(targetUserId)
           -> DELETE FROM challenges WHERE creator_id=?
        -> UPDATE users SET banned=1, ban_reason=? WHERE id=?
        -> UserManager.obtenerEmailPorId(targetUserId)
        -> AdminActionManager.registrarAccion(...)
     -> JSON {ok:true, mensaje:"Usuario baneado correctamente"}
  4. app.js alert(), recarga retos y dashboard
  5. En el frontend del usuario baneado, todas las mutaciones devuelven 403
     con banned:true, checkBanned() limpia sessionStorage y redirige
Fin: Usuario baneado, sus retos eliminados, acción registrada
```

### 3.15 Desbaneo de usuario (admin)

```
Inicio: admin panel -> tab Baneados -> click "Desbanear"
  1. confirm() y si acepta -> desbanearUsuario(targetUserId)
  2. fetch POST /api/admin/unban con JSON {adminUserId, targetUserId}
  3. AdminServlet.doPost()
     -> UserManager.esAdmin(adminUserId) -> 403
     -> UserManager.desbanear(adminUserId, targetUserId)
        -> UserManager.esAdmin(adminUserId)
        -> UPDATE users SET banned=0, ban_reason=NULL WHERE id=?
     -> JSON {ok:true}
  4. admin.js alert() y recarga tabla de baneados
Fin: Usuario desbaneado
```

### 3.16 Inicialización de base de datos

```
Inicio: Tomcat arranca la aplicación
  1. DatabaseInitializer.contextInitialized() se ejecuta (@WebListener)
  2. ConexionBD.getConnection()
     -> jdbc:mysql://DB_HOST:DB_PORT/DB_NAME?useSSL=false&...
     -> Variables de entorno: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
     -> Fallbacks: mysql, 3306, challenge_db, root, root
  3. CREATE TABLE IF NOT EXISTS para cada tabla (users, challenges, donations,
     favorites, reports, admin_actions) con schema completo
  4. ALTER TABLE IF NOT EXISTS para columnas añadidas (migración)
  5. INSERT IGNORE INTO para seed data:
     -> 3 usuarios (demo_user, challenger99, admin)
     -> 3 retos (guitar, marathon, books)
     -> 8 donaciones
  6. contextDestroyed() -> sin operación
Fin: BD lista para usar
```

---

## 4. Diccionario alfabético por lenguaje

### 4.1 Java

#### Clases (por nombre alfabético)

| Clase | Paquete | Tipo | Ruta/Rol |
|-------|---------|------|----------|
| `AdminActionManager` | `com.challenge.model` | DAO | Gestión de acciones de admin y reportes |
| `AdminServlet` | `com.challenge.controller` | Servlet | `/api/admin/*` — reportes, baneados, acciones, unban |
| `BanUserServlet` | `com.challenge.controller` | Servlet | `POST /api/ban-user` — banea usuario |
| `Challenge` | `com.challenge.model` | Entity | Modelo de reto con 16 campos |
| `ChallengeCreator` | `com.challenge.model` | DAO | Crea retos en BD |
| `ChallengeDeleter` | `com.challenge.model` | DAO | Elimina retos (propietario o admin) |
| `ChallengeDetailServlet` | `com.challenge.controller` | Servlet | `GET /api/challenge` — detalle de reto |
| `ChallengeFinder` | `com.challenge.model` | DAO | Busca reto por ID con donaciones |
| `ChallengeLister` | `com.challenge.model` | DAO | Lista retos con filtros y flags |
| `ChallengeUpdater` | `com.challenge.model` | DAO | Actualiza reto a completado |
| `CompleteChallengeServlet` | `com.challenge.controller` | Servlet | `POST /api/complete-challenge` |
| `ConexionBD` | `com.challenge.model` | Util | Pool/gestor de conexión JDBC |
| `CreateChallengeServlet` | `com.challenge.controller` | Servlet | `POST /api/create-challenge` |
| `DatabaseInitializer` | `com.challenge.model` | Listener | `@WebListener` — crea tablas y seed al inicio |
| `DeleteChallengeServlet` | `com.challenge.controller` | Servlet | `POST /api/delete-challenge` |
| `DonateServlet` | `com.challenge.controller` | Servlet | `POST /api/donate` |
| `Donation` | `com.challenge.model` | Entity | Modelo de donación con 6 campos |
| `DonationProcessor` | `com.challenge.model` | DAO | Procesa donación transaccional |
| `FavoriteManager` | `com.challenge.model` | DAO | Toggle y add de favoritos |
| `ListChallengesServlet` | `com.challenge.controller` | Servlet | `GET /api/challenges` |
| `ListFavoritesServlet` | `com.challenge.controller` | Servlet | `GET /api/favorites` |
| `LoginServlet` | `com.challenge.controller` | Servlet | `POST /api/login` |
| `ProfileLoader` | `com.challenge.model` | DAO | Carga resumen de perfil |
| `ProfileServlet` | `com.challenge.controller` | Servlet | `GET /api/profile` |
| `RegisterServlet` | `com.challenge.controller` | Servlet | `POST /api/register` |
| `ReportChallengeServlet` | `com.challenge.controller` | Servlet | `POST /api/report-challenge` |
| `ReportManager` | `com.challenge.model` | DAO | Inserta reportes |
| `ToggleFavoriteServlet` | `com.challenge.controller` | Servlet | `POST /api/toggle-favorite` |
| `User` | `com.challenge.model` | Entity | Modelo de usuario con 6 campos |
| `UserAuthenticator` | `com.challenge.model` | DAO | Autentica usuario contra BD |
| `UserManager` | `com.challenge.model` | DAO | Ban, unban, listar baneados, verificar admin/baneado |
| `UserRegistrar` | `com.challenge.model` | DAO | Registra usuario en BD |

#### Métodos públicos (orden alfabético por clase)

**AdminActionManager:**
- `obtenerAcciones()` → `List<Map<String, Object>>`
- `obtenerReportes()` → `List<Map<String, Object>>`
- `registrarAccion(int adminId, String actionType, Integer targetUserId, String targetUserEmail, Integer targetChallengeId, String reason)` → `void`

**Challenge:**
- `getProgressPercent()` → `double`
- Getters y setters para 16 campos

**ChallengeCreator:**
- `crear(String title, String description, double goalAmount, int creatorId, String videoUrl, String imageUrl)` → `Challenge`

**ChallengeDeleter:**
- `eliminar(int challengeId, int creatorId)` → `void`
- `eliminarComoAdmin(int challengeId)` → `void`

**ChallengeFinder:**
- `buscarPorId(int id)` → `Challenge`
- `buscarPorId(int id, Integer userId)` → `Challenge`
- `buscarDonaciones(int challengeId)` → `List<Donation>`

**ChallengeLister:**
- `listarTodos(Integer userId)` → `List<Challenge>`
- `listarTodos()` → `List<Challenge>`
- `listarPorCreador(int creatorId, Integer userId)` → `List<Challenge>`
- `listarPorEstado(String status, Integer userId)` → `List<Challenge>`
- `listarPorEstado(String status)` → `List<Challenge>`
- `listarFavoritos(int userId)` → `List<Challenge>`

**ChallengeUpdater:**
- `completar(int challengeId, String videoUrl)` → `void`

**ConexionBD:**
- `getConnection()` → `Connection`

**DatabaseInitializer:**
- `contextInitialized(ServletContextEvent event)` → `void`
- `contextDestroyed(ServletContextEvent event)` → `void`

**DonationProcessor:**
- `donar(int challengeId, String donorName, double amount, Integer userId)` → `void`

**FavoriteManager:**
- `toggle(int userId, int challengeId)` → `void`
- `addFavorite(int userId, int challengeId)` → `void`

**ProfileLoader:**
- `cargarResumen(int userId)` → `Map<String, Object>`

**ReportManager:**
- `reportar(int challengeId, int reporterId, String reason)` → `void`

**UserAuthenticator:**
- `autenticar(String email, String password)` → `User`

**UserManager:**
- `banear(int adminUserId, int targetUserId, String reason)` → `void`
- `desbanear(int adminUserId, int targetUserId)` → `void`
- `eliminarRetosDeUsuario(int userId)` → `void`
- `esAdmin(int userId)` → `boolean`
- `estaBaneado(int userId)` → `boolean`
- `obtenerBaneados()` → `List<Map<String, Object>>`
- `obtenerEmailPorId(int userId)` → `String`

**UserRegistrar:**
- `registrar(String username, String email, String password)` → `User`

#### Servlets por ruta

| Ruta | Servlet | Método | Parámetros |
|------|---------|--------|------------|
| `POST /api/login` | LoginServlet | `doPost` | `{email, password}` |
| `POST /api/register` | RegisterServlet | `doPost` | `{username, email, password}` |
| `GET /api/challenges` | ListChallengesServlet | `doGet` | `?status=&creatorId=&userId=` |
| `GET /api/challenge` | ChallengeDetailServlet | `doGet` | `?id=&userId=` |
| `POST /api/create-challenge` | CreateChallengeServlet | `doPost` | `{title, description, goalAmount, creatorId, videoUrl, imageUrl}` |
| `POST /api/donate` | DonateServlet | `doPost` | `{challengeId, donorName, amount, userId}` |
| `POST /api/complete-challenge` | CompleteChallengeServlet | `doPost` | `{challengeId, userId, completionVideoUrl, thankYouMessage}` |
| `POST /api/toggle-favorite` | ToggleFavoriteServlet | `doPost` | `{userId, challengeId}` |
| `GET /api/favorites` | ListFavoritesServlet | `doGet` | `?userId=` |
| `POST /api/report-challenge` | ReportChallengeServlet | `doPost` | `{challengeId, userId, reason}` |
| `GET /api/profile` | ProfileServlet | `doGet` | `?userId=` |
| `POST /api/delete-challenge` | DeleteChallengeServlet | `doPost` | `{challengeId, userId, reason}` |
| `POST /api/ban-user` | BanUserServlet | `doPost` | `{adminUserId, targetUserId, reason}` |
| `GET /api/admin/reports` | AdminServlet | `doGet` | — |
| `GET /api/admin/banned-users` | AdminServlet | `doGet` | — |
| `GET /api/admin/actions` | AdminServlet | `doGet` | — |
| `POST /api/admin/unban` | AdminServlet | `doPost` | `{adminUserId, targetUserId}` |

---

### 4.2 JavaScript

#### Funciones globales (app.js) — orden alfabético

| Función | Parámetros | Descripción |
|---------|-----------|-------------|
| `abrirAdminPanel()` | — | Abre el modal de admin (modal antiguo) |
| `abrirCompletionModal(challengeId, currentVideoUrl, currentMessage, detailModal)` | `number, string, string, element` | Crea o muestra modal de video/logro |
| `abrirReporteModal(challengeId, title)` | `number, string` | Abre modal de reporte |
| `banearUsuario(targetUserId, reason)` | `number, string` | POST `/api/ban-user` |
| `cargarAccionesAdmin()` | — | GET `/api/admin/actions` -> renderiza notificaciones |
| `cargarBaneadosAdmin()` | — | GET `/api/admin/banned-users` -> renderiza baneados |
| `cargarDashboard()` | — | Carga perfil + favoritos + retos del dashboard |
| `cargarReportesAdmin()` | — | GET `/api/admin/reports` -> renderiza reportes |
| `cargarRetos()` | — | GET `/api/challenges` -> grids activos/completados |
| `cargarRetosDashboard(tab, userId)` | `string, number` | Carga retos por tab (active/completed/favorites) |
| `checkBanned(data)` | `object` | Si data.banned=true, limpia session y redirige |
| `clearResultado(elementId)` | `string` | Vacía innerHTML de un elemento |
| `createChallengeCard(challenge)` | `object` | Construye DOM de tarjeta de reto |
| `desbanearUsuario(targetUserId)` | `number` | POST `/api/admin/unban` |
| `eliminarReto(challengeId, reason)` | `number, string` | POST `/api/delete-challenge` |
| `escapeHtml(str)` | `string` | Escapa HTML para XSS (en admin.js) |
| `formatCurrency(amount)` | `number` | Formatea como `€X.XX` |
| `formatDate(dateStr)` | `string` | Formatea fecha locale es-ES |
| `getIsAdmin()` | — | `sessionStorage.getItem('isAdmin') === 'true'` |
| `getUserId()` | — | `sessionStorage.getItem('userId')` |
| `getUsername()` | — | `sessionStorage.getItem('username')` |
| `guardarCompletion(challengeId, modal, detailModal)` | `number, element, element` | POST `/api/complete-challenge` |
| `openDetail(challengeId)` | `number` | GET `/api/challenge?id=X` -> renderiza modal detalle |
| `openDonate(challengeId, title)` | `number, string` | Abre modal de donación |
| `openReasonModal(actionType, targetName)` | `string, string` | Promise: muestra modal de motivo obligatorio |
| `showError(elementId, message)` | `string, string` | Muestra error con clase .resultado.error |
| `showSuccess(elementId, message)` | `string, string` | Muestra éxito con clase .resultado.success |
| `toggleFav(challengeId, btn)` | `number, element` | POST `/api/toggle-favorite`, alterna ♡/♥ |
| `updateNavbar()` | — | Actualiza enlaces del nav según login/admin |
| `verRetoAdmin(challengeId)` | `number` | Redirige a index.html con pendingChallengeId |

#### Funciones globales (admin.js) — orden alfabético

| Función | Parámetros | Descripción |
|---------|-----------|-------------|
| `cargarAccionesAdmin()` | — | GET `/api/admin/actions` -> tabla de notificaciones |
| `cargarBaneadosAdmin()` | — | GET `/api/admin/banned-users` -> tabla de baneados |
| `cargarReportesAdmin()` | — | GET `/api/admin/reports` -> tabla de reportes |
| `desbanearUsuario(targetUserId)` | `number` | POST `/api/admin/unban` |
| `escapeHtml(str)` | `string` | Escapa HTML (XSS prevention) |
| `fetchWithTimeout(url, options)` | `string, object` | fetch con AbortController de 15s |
| `filtrarBaneados(query)` | `string` | Filtra baneadosData por username/email |
| `renderBaneadosTable(usuarios)` | `array` | Renderiza tabla de baneados |

#### Variables globales

| Variable | Archivo | Tipo | Valor |
|----------|---------|------|-------|
| `API_BASE` | app.js | `string` | `window.location.origin` |
| `baneadosData` | admin.js | `array` | `[]` (cache de baneados) |
| `FETCH_TIMEOUT_MS` | admin.js | `number` | `15000` |

#### Event listeners registrados

**app.js (DOMContentLoaded):**
- `#loginForm` submit → login flow
- `#registerForm` submit → register flow
- `#createChallengeForm` submit → create challenge flow
- `#challengeTitle`, `#challengeDesc`, `#challengeGoal`, `#challengeImage` input → `updatePreview()`
- `input[name="payment"]` change → toggle `.card-details` visibility
- `#reportForm` submit → report flow
- `#donateForm` submit → donate flow
- `#closeDetail`, `#closeDonate`, `#cancelDonate`, `#closeReport`, `#cancelReport` click → hide modal
- `#detailModal`, `#donateModal`, `#reportModal` click → hide on overlay click

**admin.js (DOMContentLoaded):**
- Tab buttons click → toggle `btn-primary`/`btn-outline`, load tab content
- `#adminSearchInput` input → `filtrarBaneados()`

---

### 4.3 SQL

#### Tablas (orden alfabético)

| Tabla | Base de datos | Descripción |
|-------|--------------|-------------|
| `admin_actions` | `challenge_db` | Registro de acciones administrativas (ban/delete) |
| `challenges` | `challenge_db` | Retos creados por usuarios |
| `donations` | `challenge_db` | Donaciones realizadas a retos |
| `favorites` | `challenge_db` | Favoritos de usuarios (relación M:N) |
| `reports` | `challenge_db` | Reportes de usuarios sobre retos |
| `users` | `challenge_db` | Usuarios registrados |

#### Columnas por tabla

**admin_actions:**
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| `id` | INT | AUTO_INCREMENT PK |
| `admin_id` | INT | NOT NULL, FK → users.id ON DELETE CASCADE |
| `action_type` | VARCHAR(30) | NOT NULL |
| `target_user_id` | INT | NULLABLE |
| `target_user_email` | VARCHAR(255) | NULLABLE |
| `target_challenge_id` | INT | NULLABLE |
| `reason` | TEXT | NOT NULL |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**challenges:**
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| `id` | INT | AUTO_INCREMENT PK |
| `title` | VARCHAR(255) | NOT NULL |
| `description` | TEXT | NOT NULL |
| `goal_amount` | DECIMAL(10,2) | NOT NULL |
| `current_amount` | DECIMAL(10,2) | DEFAULT 0 |
| `creator_id` | INT | NOT NULL, FK → users.id ON DELETE CASCADE |
| `status` | ENUM('active','completed') | DEFAULT 'active' |
| `video_url` | VARCHAR(500) | NULLABLE |
| `image_url` | VARCHAR(500) | NULLABLE |
| `completion_video_url` | TEXT | DEFAULT NULL |
| `thank_you_message` | TEXT | DEFAULT NULL |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**donations:**
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| `id` | INT | AUTO_INCREMENT PK |
| `challenge_id` | INT | NOT NULL, FK → challenges.id ON DELETE CASCADE |
| `donor_name` | VARCHAR(100) | NOT NULL |
| `amount` | DECIMAL(10,2) | NOT NULL |
| `user_id` | INT | DEFAULT NULL, FK → users.id ON DELETE SET NULL |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**favorites:**
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| `id` | INT | AUTO_INCREMENT PK |
| `user_id` | INT | NOT NULL, FK → users.id ON DELETE CASCADE |
| `challenge_id` | INT | NOT NULL, FK → challenges.id ON DELETE CASCADE |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| — | UNIQUE KEY | `(user_id, challenge_id)` |

**reports:**
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| `id` | INT | AUTO_INCREMENT PK |
| `challenge_id` | INT | NOT NULL, FK → challenges.id ON DELETE CASCADE |
| `reporter_id` | INT | NOT NULL, FK → users.id ON DELETE CASCADE |
| `reason` | TEXT | NOT NULL |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

**users:**
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| `id` | INT | AUTO_INCREMENT PK |
| `username` | VARCHAR(100) | NOT NULL |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | NOT NULL |
| `is_admin` | TINYINT(1) | DEFAULT 0 |
| `banned` | TINYINT(1) | DEFAULT 0 |
| `ban_reason` | TEXT | DEFAULT NULL |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

#### Seed data (INSERT IGNORE)

**users:**
- `(1, 'demo_user', 'demo@example.com', '1234', 0, 0, NULL, NOW())`
- `(2, 'challenger99', 'challenger@example.com', '1234', 0, 0, NULL, NOW())`
- `(99, 'admin', 'admin@pujas.com', 'admin123', 1, 0, NULL, NOW())`

**challenges:**
- `(1, 'I will learn to play guitar in 30 days', ..., 500.00, 320.00, 1, 'active', NULL, NULL, NULL, NULL, NOW())`
- `(2, 'Marathon of 42km in 3 months', ..., 300.00, 450.00, 2, 'completed', 'https://www.youtube.com/embed/dQw4w9WgXcQ', NULL, NULL, NULL, NOW())`
- `(3, 'Read 20 books in 6 months', ..., 200.00, 150.00, 1, 'active', NULL, NULL, NULL, NULL, NOW())`

**donations:** 8 donaciones con montos entre 50€ y 200€.

---

### 4.4 CSS

#### Clases (orden alfabético)

| Clase | Propósito | Archivo:línea |
|-------|-----------|---------------|
| `.admin-actions-panel` | Contenedor de acciones admin en detalle | style.css:1230 |
| `.admin-empty` | Mensaje vacío para tablas admin | style.css:1499 |
| `.admin-list` | Lista vertical de items admin | style.css:1269 |
| `.admin-list-actions` | Botones dentro de item admin | style.css:1294 |
| `.admin-list-admin` | Nombre del admin que realizó acción | style.css:1355 |
| `.admin-list-badge` | Badge tipo de acción admin | style.css:1323 |
| `.admin-list-badge-ban` | Badge rojo para baneo | style.css:1331 |
| `.admin-list-body` | Cuerpo del item admin | style.css:1340 |
| `.admin-list-date` | Fecha en item admin | style.css:1335 |
| `.admin-list-email` | Item con borde izquierdo morado | style.css:1351 |
| `.admin-list-header` | Cabecera del item admin | style.css:1316 |
| `.admin-list-item` | Item individual de admin list | style.css:1275 |
| `.admin-list-separator` | Separador decorativo entre items | style.css:1307 |
| `.admin-page` | Contenedor página admin | style.css:1384 |
| `.admin-search` | Contenedor de búsqueda admin | style.css:1388 |
| `.admin-table` | Tabla admin | style.css:1416 |
| `.admin-table tbody tr` | Hover en filas de tabla | style.css:1445 |
| `.admin-table td` | Celdas de tabla admin | style.css:1438 |
| `.admin-table th` | Cabeceras de tabla admin | style.css:1427 |
| `.admin-table thead` | Cabecera de tabla admin | style.css:1422 |
| `.admin-table .cell-actions` | Celda de acciones | style.css:1481 |
| `.admin-table .cell-date` | Celda de fecha | style.css:1475 |
| `.admin-table .cell-email` | Celda de email | style.css:1465 |
| `.admin-table .cell-id` | Celda de ID | style.css:1453 |
| `.admin-table .cell-reason` | Celda de motivo | style.css:1470 |
| `.admin-table .cell-title` | Celda de título | style.css:1459 |
| `.admin-table .cell-type` | Celda de tipo | style.css:1485 |
| `.admin-table .cell-user` | Celda de usuario | style.css:1489 |
| `.admin-table-container` | Contenedor scrollable de tabla | style.css:1408 |
| `.admin-tab-content` | Contenido del tab admin | style.css:1263 |
| `.admin-tabs` | Barra de tabs admin | style.css:1256 |
| `.auth-card` | Tarjeta de autenticación | style.css:610 |
| `.auth-card h1` | Título de auth-card | style.css:626 |
| `.auth-container` | Contenedor centrado auth | style.css:602 |
| `.auth-icon` | Icono en auth-card | style.css:621 |
| `.auth-link` | Enlace de auth (registrarse/login) | style.css:636 |
| `.auth-link a` | Enlace dentro de auth-link | style.css:642 |
| `.auth-subtitle` | Subtítulo en auth-card | style.css:631 |
| `.back-link` | Enlace volver atrás | style.css:948 |
| `.btn` | Botón base | style.css:68 |
| `.btn-danger` | Botón rojo | style.css:1187 |
| `.btn-full` | Botón ancho completo | style.css:123 |
| `.btn-lg` | Botón grande | style.css:118 |
| `.btn-outline` | Botón outline morado | style.css:94 |
| `.btn-outline-danger` | Botón outline rojo | style.css:1200 |
| `.btn-outline:hover` | Hover de outline | style.css:100 |
| `.btn-primary` | Botón relleno morado | style.css:81 |
| `.btn-primary:hover` | Hover de primary | style.css:87 |
| `.btn-shield` | Botón admin rojo en navbar | style.css:1236 |
| `.btn-sm` | Botón pequeño | style.css:1211 |
| `.btn-success` | Botón verde | style.css:105 |
| `.btn-success:hover` | Hover de success | style.css:111 |
| `.card-details` | Detalles de tarjeta de crédito | style.css:782 |
| `.card-image` | Contenedor imagen en tarjeta | style.css:267 |
| `.card-image img` | Imagen dentro de card-image | style.css:275 |
| `.challenge-actions` | Acciones de tarjeta de reto | style.css:417 |
| `.challenge-actions .btn` | Botones dentro de actions | style.css:423 |
| `.challenge-actions .btn-outline.report-challenge` | Botón reportar | style.css:1216 |
| `.challenge-card` | Tarjeta de reto | style.css:232 |
| `.challenge-card h3` | Título en tarjeta | style.css:316 |
| `.challenge-card:hover` | Hover de tarjeta | style.css:261 |
| `.challenge-card.completed` | Tarjeta completada | style.css:257 |
| `.challenge-card.completed .progress-fill` | Barra verde en completados | style.css:379 |
| `.challenge-card::before` | Línea superior degradado | style.css:246 |
| `.challenge-creator` | Creador y fecha | style.css:324 |
| `.challenge-desc` | Descripción en tarjeta | style.css:330 |
| `.challenge-progress` | Contenedor progreso | style.css:342 |
| `.challenge-stats` | Estadísticas de reto | style.css:390 |
| `.challenge-status` | Badge de estado | style.css:399 |
| `.challenges-grid` | Grid de tarjetas | style.css:216, 224 |
| `.challenges-grid .estado` | Estado dentro de grid | style.css:220 |
| `.challenges-section` | Sección de retos | style.css:195 |
| `.challenges-section h2` | Título de sección | style.css:199 |
| `.completed-badge` | Badge de completado | style.css:532 |
| `.completed-confirmation` | Confirmación de completado | style.css:543 |
| `.completion-locked` | Candado para no-donantes | style.css:1552 |
| `.completion-pending` | Pendiente de completar | style.css:1537 |
| `.completion-pending p` | Texto pendiente | style.css:1546 |
| `.container` | Contenedor ancho máximo | style.css:24 |
| `.dashboard` | Página dashboard | style.css:826 |
| `.detail-desc` | Descripción en detalle | style.css:526 |
| `.detail-image` | Imagen en detalle | style.css:512 |
| `.detail-image img` | Imagen dentro de detalle | style.css:520 |
| `.detail-info h2` | Título en detalle | style.css:500 |
| `.detail-layout` | Layout grid 2 columnas | style.css:494 |
| `.detail-meta` | Metadatos en detalle | style.css:506 |
| `.detail-panel` | Panel lateral de detalle | style.css:572 |
| `.detail-panel h3` | Título de panel | style.css:580 |
| `.donate-resultado` | Resultado de donación | style.css:804 |
| `.donate-subtitle` | Subtítulo de donación | style.css:808 |
| `.estado` | Mensaje de estado | style.css:429 |
| `.fav-btn` | Botón favorito | style.css:286 |
| `.fav-btn.fav-active` | Favorito activo (♥ rojo) | style.css:311 |
| `.fav-btn:hover` | Hover de favorito | style.css:306 |
| `.field-hint` | Ayuda de campo formulario | style.css:1024 |
| `.footer-content` | Contenido del footer | style.css:1105 |
| `.footer-content p` | Texto del footer | style.css:1110 |
| `.form-actions` | Acciones de formulario | style.css:700 |
| `.form-actions-center` | Acciones centradas | style.css:707 |
| `.form-group` | Grupo de formulario | style.css:652 |
| `.form-group input`, `textarea`, `select` | Inputs base | style.css:665 |
| `.form-group input:focus`, `textarea:focus` | Focus de inputs | style.css:678 |
| `.form-group label` | Label de formulario | style.css:657 |
| `.form-page` | Página de formulario | style.css:711 |
| `.form-page h1` | Título de form-page | style.css:715 |
| `.form-row` | Fila de formulario | style.css:690 |
| `.form-row-3` | Fila 2 columnas | style.css:696 |
| `.form-section` | Sección de formulario | style.css:991 |
| `.header-subtitle` | Subtítulo de cabecera | style.css:731 |
| `.hero` | Sección hero | style.css:127 |
| `.hero h1` | Título hero | style.css:155 |
| `.hero::before` | Decoración fondo hero | style.css:136 |
| `.hero-actions` | Botones hero | style.css:173 |
| `.hero-actions .btn-outline` | Outline en hero | style.css:180 |
| `.hero-content` | Contenido hero | style.css:148 |
| `.hero-subtitle` | Subtítulo hero | style.css:163 |
| `.input-group` | Grupo con prefijo | style.css:1032 |
| `.input-group input` | Input dentro de grupo | style.css:1048 |
| `.input-prefix` | Prefijo (€) | style.css:1037 |
| `.kickstarter-form .form-section` | Sección de formulario creativo | style.css:991 |
| `.logo-icon` | Icono del logo | style.css:59 |
| `.logo-text` | Texto del logo | style.css:60 (inline) |
| `.modal-close` | Botón cerrar modal | style.css:471 |
| `.modal-close:hover` | Hover de cerrar | style.css:489 |
| `.modal-content` | Contenido de modal | style.css:454 |
| `.modal-lg` | Modal grande | style.css:467 |
| `.modal-overlay` | Overlay de modal | style.css:435 |
| `.modal-overlay.hidden` | Modal oculto | style.css:450 |
| `.modal-sm` | Modal pequeño | style.css:1252 |
| `.nav-container` | Contenedor nav | style.css:40 |
| `.nav-links` | Enlaces del nav | style.css:63 |
| `.nav-logo` | Logo en nav | style.css:49 |
| `.navbar` | Barra de navegación | style.css:30 |
| `.note` | Nota ficticia de pago | style.css:789 |
| `.page-header` | Cabecera de página | style.css:721 |
| `.page-header h1` | Título de cabecera | style.css:726 |
| `.panel-progress` | Progreso en panel | style.css:586 |
| `.panel-stat` | Estadística en panel | style.css:590 |
| `.payment-option` | Opción de pago | style.css:760 |
| `.payment-option:hover` | Hover de opción | style.css:772 |
| `.payment-option:has(input:checked)` | Opción seleccionada | style.css:777 |
| `.payment-options` | Opciones de pago | style.css:754 |
| `.preview-card` | Card de preview | style.css:1056 |
| `.preview-card h3` | Título de preview | style.css:1078 |
| `.preview-desc` | Descripción de preview | style.css:1083 |
| `.preview-image-container` | Contenedor imagen preview | style.css:1064 |
| `.preview-image-container img` | Imagen de preview | style.css:1072 |
| `.preview-meta` | Meta de preview | style.css:1090 |
| `.preview-section` | Sección de preview | style.css:1052 |
| `.progress-bar` | Barra de progreso | style.css:346 |
| `.progress-fill` | Relleno de barra | style.css:355 |
| `.progress-fill::after` | Animación shimmer | style.css:363 |
| `.progress-info` | Info de progreso | style.css:383 |
| `.report-challenge` | Botón reportar | style.css:1216 |
| `.resultado` | Mensaje de resultado | style.css:796 |
| `.resultado.error` | Error resultado | style.css:814 |
| `.resultado.success` | Éxito resultado | style.css:820 |
| `.search-input` | Input de búsqueda | style.css:1392 |
| `.search-input:focus` | Focus de búsqueda | style.css:1402 |
| `.section-content` | Contenido de sección | style.css:1014 |
| `.section-content h2` | Título de sección | style.css:1018 |
| `.section-icon` | Icono de sección | style.css:1006 |
| `.section-label` | Etiqueta de sección | style.css:207 |
| `.site-footer` | Footer | style.css:1096 |
| `.status-active` | Badge activo (amarillo) | style.css:407 |
| `.status-completed` | Badge completado (verde) | style.css:412 |
| `.summary-card` | Card de resumen | style.css:868 |
| `.summary-card:nth-child(n)` | Colores por posición | style.css:893-906 |
| `.summary-card:hover` | Hover de summary | style.css:879 |
| `.summary-card::before` | Línea superior | style.css:884 |
| `.summary-cards` | Grid de summary | style.css:861 |
| `.summary-icon` | Icono de summary | style.css:909 |
| `.summary-label` | Label de summary | style.css:942 |
| `.summary-number` | Número de summary | style.css:936 |
| `.tab` | Tab del dashboard | style.css:964 |
| `.tab.active` | Tab activo | style.css:982 |
| `.tab:hover` | Hover de tab | style.css:978 |
| `.tab-content` | Contenido de tab | style.css:987 |
| `.tabs` | Barra de tabs | style.css:956 |
| `.thank-you-author` | Autor del agradecimiento | style.css:1530 |
| `.thank-you-icon` | Icono de agradecimiento | style.css:1518 |
| `.thank-you-message` | Mensaje de agradecimiento | style.css:1509 |
| `.thank-you-text` | Texto de agradecimiento | style.css:1523 |
| `.unban-btn` | Botón desbanear | style.css:1363 |
| `.user-avatar` | Avatar de usuario | style.css:842 |
| `.user-avatar-sm` | Avatar pequeño | style.css:1494 |
| `.user-info` | Info de usuario | style.css:830 |
| `.user-info h2` | Nombre de usuario | style.css:853 |
| `.user-info p` | Email de usuario | style.css:857 |
| `.video-container` | Contenedor video responsive | style.css:554 |
| `.video-container iframe` | Iframe responsivo | style.css:564 |
| `.view-challenge-btn` | Botón ver reto admin | style.css:1302 |
| `.warning-banner` | Banner de advertencia | style.css:736 |
| `.warning-icon` | Icono de warning | style.css:749 |

#### Media queries

| Breakpoint | Cambios |
|-----------|---------|
| `@media (max-width: 768px)` | Hero más pequeño, grid 1 columna, layout detalle 1 columna, summary 2 columnas, nav más pequeño, form-section vertical, tabla admin más compacta |
| `@media (max-width: 480px)` | Hero mínimo, botones más pequeños, hero-actions vertical, summary 1 columna, challenge-actions vertical, form-actions vertical |

#### Animaciones

| Animación | Propósito |
|-----------|-----------|
| `@keyframes fadeInUp` | Entrada suave de tarjetas y modales |
| `@keyframes shimmer` | Efecto de brillo en barra de progreso |

---

### 4.5 HTML

#### Páginas (orden alfabético)

| Página | Archivo | Propósito |
|--------|---------|-----------|
| `admin.html` | `src/main/webapp/admin.html` | Panel de administración (reportes, baneados, notificaciones) |
| `create-challenge.html` | `src/main/webapp/create-challenge.html` | Formulario de creación de reto con preview |
| `dashboard.html` | `src/main/webapp/dashboard.html` | Panel de usuario con resumen y tabs |
| `index.html` | `src/main/webapp/index.html` | Landing page con hero y grids de retos |
| `login.html` | `src/main/webapp/login.html` | Formulario de inicio de sesión |
| `register.html` | `src/main/webapp/register.html` | Formulario de registro |

#### Modales compartidos

| Modal | ID | Presente en |
|-------|-----|------------|
| Reason Modal | `#reasonModal` | index.html, dashboard.html, create-challenge.html, admin.html |
| Detail Modal | `#detailModal` | index.html |
| Donate Modal | `#donateModal` | index.html |
| Report Modal | `#reportModal` | index.html |
| Completion Modal | `#completionModal` | Creado dinámicamente por `abrirCompletionModal()` |

#### Elementos por página

**admin.html:**
- `<div class="admin-search">` — input de búsqueda `#adminSearchInput`
- `<div class="admin-tabs">` — 3 botones: Reportes, Baneados, Notificaciones
- `<div id="adminTabContent">` — contenido dinámico de tab
- `<div id="reasonModal">` — modal de motivo obligatorio

**create-challenge.html:**
- `<form id="createChallengeForm">` — formulario principal
- Secciones: título, descripción, video, imagen, meta €
- `<div id="previewSection">` — preview en vivo
- `<div id="createResultado">` — mensajes de resultado

**dashboard.html:**
- `<div class="user-info">` — avatar, nombre, email
- `<div class="summary-cards">` — 4 cards: creados, completados, recaudados, favoritos
- `<div class="tabs">` — 3 tabs: Activos, Completados, Favoritos
- `<div id="dashboardGrid">` — grid dinámico de tarjetas

**index.html:**
- `<section class="hero">` — hero con CTA
- `<section id="challenges">` — `#activeChallengesGrid` y `#completedChallengesGrid`
- `<div id="detailModal">` — modal de detalle de reto
- `<div id="donateModal">` — modal de donación con métodos de pago
- `<div id="reportModal">` — modal de reporte

**login.html:**
- `<form id="loginForm">` — email + password
- `<div id="loginResultado">` — mensajes

**register.html:**
- `<form id="registerForm">` — username + email + password + confirm
- `<div id="registerResultado">` — mensajes

#### Footer común

Todas las páginas incluyen:
```html
<footer class="site-footer">
  <div class="footer-content">
    <p>&copy; 2026 Empujas. Aplicación web utilizada para que usuarios
       puedan realizar retos a cambio de algo de dinero, todo dentro de
       los marcos legales.</p>
  </div>
</footer>
```

---

## 5. Base de datos

### Esquema entidad-relación

```
users 1---* challenges (creator_id)
users 1---* donations (user_id, nullable)
users 1---* favorites (user_id)
users 1---* reports (reporter_id)
users 1---* admin_actions (admin_id)
challenges 1---* donations (challenge_id)
challenges 1---* favorites (challenge_id)
challenges 1---* reports (challenge_id)
```

### Variables de entorno para conexión

| Variable | Fallback | Descripción |
|----------|----------|-------------|
| `DB_HOST` | `mysql` | Host de MySQL |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_NAME` | `challenge_db` | Nombre de base de datos |
| `DB_USER` | `root` | Usuario MySQL |
| `DB_PASSWORD` | `root` | Contraseña MySQL |

### Seed data

3 usuarios, 3 retos, 8 donaciones precargadas con INSERT IGNORE para evitar duplicados en reinicios.

---

## 6. Despliegue

### Docker Compose (local)

```yaml
services:
  app:        # Tomcat 10.1 + WAR construido con Maven
  mysql:      # MySQL 8.0 con init script
  phpmyadmin: # phpMyAdmin en puerto 8081
```

Comandos:
```
docker-compose up --build
```

### Railway (producción)

- **Build:** Dockerfile (Maven builder + Tomcat runtime)
- **Deploy:** 1 réplica, restart on failure, max 10 retries
- **App URL:** `https://app-production-d046.up.railway.app`
- **Repositorio:** `github.com/jrz0007-lab/Empujas` (rama `main`)
- **Admin cuenta:** `javie.recuero.z@pujas.com` / `1234` (id: 101)

### Archivos de configuración

| Archivo | Propósito |
|---------|-----------|
| `pom.xml` | Dependencias y build Maven |
| `Dockerfile` | Build multi-stage para Railway |
| `docker-compose.yml` | Orquestación local 3 servicios |
| `railway.json` | Configuración de despliegue Railway |
| `mysql/init/01-schema.sql` | Schema + seed data |

---

*Documentación generada el 2026-05-22 para EMpujas (challenge-app)*
