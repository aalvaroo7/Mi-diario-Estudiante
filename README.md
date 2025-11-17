# 📚 Especificación Técnica del Sistema: Mi Diario Estudiante

Este `README` consolida los requisitos funcionales, modelos de datos y la arquitectura de endpoints de los principales módulos del sistema **Mi Diario Estudiante**.

---

## 🔐 Módulo A: Autenticación Completa

[cite_start]Este módulo describe las funcionalidades de **autenticación** y **gestión de cuenta**, garantizando la seguridad y el control de acceso al sistema[cite: 53].

### Requisitos Funcionales y No Funcionales (RF/RNF)
* [cite_start]**RF-01**: Registro de usuario [cite: 56]
* [cite_start]**RF-02**: Autenticación (Login y Logout) [cite: 57]
* [cite_start]**RF-21**: Cerrar sesión [cite: 58]
* [cite_start]**RF-11**: Perfil de usuario (solo datos básicos para identificación) [cite: 59]
* [cite_start]**RNF-01**: Contraseñas cifradas (BCrypt) [cite: 60]
* [cite_start]**RNF-02**: Autenticación obligatoria [cite: 61]
* [cite_start]**RNF-12**: Intentos fallidos limitados [cite: 63]
* [cite_start]**RNF-14**: Evitar usuarios duplicados (correo y nombre de usuario) [cite: 65, 70]

### Lógica y Validaciones Clave
* [cite_start]**Registro**: Valida duplicidad de correo y nombre de usuario, cifra la contraseña usando BCrypt, y asigna el rol `usuario` por defecto[cite: 70].
* [cite_start]**Login**: Valida credenciales, compara contraseña con BCrypt, aplica contador de intentos y registra la acción en auditoría[cite: 72].
* [cite_start]**Validaciones Críticas**: Correo único, nombre de usuario único, contraseña $>= 8$ caracteres, y bloqueo temporal tras varios intentos fallidos[cite: 81, 82, 83, 85].
* [cite_start]**Modelo de Datos Afectado**: `usuarios` y `roles`[cite: 67].

### Endpoints Propuestos
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/auth/registro` | [cite_start]Registrar un nuevo usuario [cite: 76] |
| `POST` | `/api/auth/login` | [cite_start]Iniciar sesión [cite: 77] |
| `POST` | `/api/auth/logout` | [cite_start]Cerrar sesión [cite: 78] |
| `GET` | `/api/usuarios/perfil/{id}` | [cite_start]Obtener perfil básico [cite: 79] |

---

## 📢 Módulo B: Publicaciones y Feed

[cite_start]Este módulo describe la gestión de **publicaciones** y la visualización de contenido en el **muro (feed)**[cite: 3]. [cite_start]Incluye las funciones de creación, edición, eliminación y la gestión de visibilidad[cite: 4].

### Requisitos Funcionales Incluidos
* [cite_start]**RF-03**: Crear publicación [cite: 6]
* [cite_start]**RF-04**: Editar publicación / Eliminar publicación [cite: 7, 8]
* [cite_start]**RF-05**: Privacidad de publicaciones [cite: 9]
* [cite_start]**RF-06**: Ver publicaciones propias [cite: 10]
* [cite_start]**RF-07**: Ver publicaciones públicas y de amigos [cite: 11]
* [cite_start]**RF-13**: Buscador de publicaciones [cite: 12]
* [cite_start]**RF-24**: Filtro de publicaciones por amigos [cite: 13]

### Lógica de Visibilidad
[cite_start]La visibilidad se define por el nivel de privacidad de la publicación[cite: 19]:
* [cite_start]**Públicas**: Visibles para todos[cite: 20].
* [cite_start]**Privadas**: Visibles solo para el autor[cite: 20].
* [cite_start]**Amigos**: Visibles solo para usuarios conectados por amistad aceptada[cite: 20].

### Endpoints Propuestos
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/publicaciones` | [cite_start]Crear una nueva publicación [cite: 22] |
| `PUT` | `/api/publicaciones/{id}` | [cite_start]Editar una publicación existente [cite: 23] |
| `DELETE` | `/api/publicaciones/{id}` | [cite_start]Eliminar una publicación [cite: 24] |
| `GET` | `/api/publicaciones/mias` | [cite_start]Obtener publicaciones propias [cite: 25] |
| `GET` | `/api/publicaciones/feed` | [cite_start]Obtener el muro/feed (públicas y de amigos) [cite: 26] |
| `GET` | `/api/publicaciones/buscar` | [cite_start]Buscar publicaciones [cite: 27] |

---

## 🛠️ Módulo C: Roles, Moderación y Mantenimiento

[cite_start]Este módulo cubre la gestión de **roles de sistema** (administrador y técnico), **moderación de usuarios** (bloqueo) y la función de **modo mantenimiento**[cite: 30].

### Requisitos Funcionales Incluidos
* [cite_start]**RF-17**: Rol Administrador [cite: 32]
* [cite_start]**RF-18**: Rol Técnico [cite: 33]
* [cite_start]**RF-20**: Bloquear usuarios [cite: 34]
* [cite_start]**RF-25**: Eliminar usuarios (solo por Admin) [cite: 35]
* [cite_start]**RF-19**: Modo mantenimiento [cite: 36]

### Roles y Permisos
| Rol | Permisos Clave |
| :--- | :--- |
| **Administrador** | [cite_start]Controla usuarios, elimina cuentas, cambia roles[cite: 41]. |
| **Técnico** | [cite_start]Puede activar o desactivar el modo mantenimiento[cite: 41]. |
| **Usuario** | [cite_start]Sin permisos especiales[cite: 41]. |

### Modo Mantenimiento
[cite_start]Cuando está activado, solo el Administrador y el Técnico pueden acceder al sistema[cite: 43]. [cite_start]El resto de los usuarios recibe un mensaje de servicio no disponible[cite: 43].

### Endpoints Propuestos (Admin/Sistema)
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/admin/bloquear/{id}` | [cite_start]Bloquear a un usuario [cite: 45] |
| `DELETE` | `/api/admin/eliminar/{id}` | [cite_start]Eliminar la cuenta de un usuario [cite: 47] |
| `POST` | `/api/sistema/mantenimiento/activar` | [cite_start]Activar modo mantenimiento [cite: 48] |
| `POST` | `/api/sistema/mantenimiento/desactivar` | [cite_start]Desactivar modo mantenimiento [cite: 49] |
| `GET` | `/api/sistema/mantenimiento/estado` | [cite_start]Obtener el estado del modo mantenimiento [cite: 50] |
