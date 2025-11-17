# 📚 Especificación Técnica del Sistema: Mi Diario Estudiante

---

## 🔐 Módulo A: Autenticación Completa

Este documento describe la implementación del Módulo A, correspondiente a los requisitos funcionales del sistema Mi Diario Estudiante relacionados con autenticación y gestión de cuenta. El objetivo es definir claramente las funcionalidades, endpoints, validaciones, lógica de negocio y dependencias necesarias.

### Requisitos Funcionales y No Funcionales (RF/RNF)
* **RF-01**: Registro de usuario
* **RF-02**: Autenticación (Login y Logout)
* **RF-21**: Cerrar sesión
* **RF-11**: Perfil de usuario (parcial, solo datos básicos para identificación)
* **RNF-01**: Contraseñas cifradas (BCrypt)
* **RNF-02**: Autenticación obligatoria
* **RNF-12**: Intentos fallidos limitados
* **RNF-14**: Evitar usuarios duplicados

### Lógica y Validaciones Clave
* **Registro**: Valida duplicidad de correo y nombre de usuario, cifra la contraseña usando BCrypt, asigna rol por defecto (usuario) y registra auditoría del intento.
* **Login**: Valida credenciales, compara contraseña con BCrypt, aplica contador de intentos, genera una sesión y registra la acción en auditoría.
* **Validaciones Críticas**: Correo único, nombre de usuario único, contraseña $>= 8$ caracteres, BCrypt obligatorio, y bloqueo temporal tras varios intentos fallidos.
* **Modelo de Datos Afectado**: Las tablas afectadas incluyen `usuarios` y `roles`. Se garantiza la consistencia de claves primarias `BIGINT`, así como la correcta relación `FK` entre `usuarios.rol_id` y `roles.id`.

### Endpoints Propuestos
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/auth/registro` | Registrar un nuevo usuario |
| `POST` | `/api/auth/login` | Iniciar sesión |
| `POST` | `/api/auth/logout` | Cerrar sesión |
| `GET` | `/api/usuarios/perfil/{id}` | Obtener perfil básico |

---

## 📢 Módulo B: Publicaciones y Feed

Este módulo describe el sistema relacionado con publicaciones y la visualización del contenido en el muro, incluyendo creación, edición, eliminación y visibilidad.

### Requisitos Funcionales Incluidos
* **RF-03**: Crear publicación
* **RF-04**: Editar publicación
* **RF-04**: Eliminar publicación
* **RF-05**: Privacidad de publicaciones
* **RF-06**: Ver publicaciones propias
* **RF-07**: Ver publicaciones públicas y de amigos
* **RF-13**: Buscador de publicaciones
* **RF-24**: Filtro de publicaciones por amigos

### Lógica de Visibilidad
El módulo permite que la creación de publicaciones tenga contenido, imagen (opcional), fecha automática y nivel de privacidad.
* **Públicas**: Visibles para todos.
* **Privadas**: Visibles solo para el autor.
* **Amigos**: Visibles solo para usuarios conectados por amistad aceptada.

### Modelo de Datos Afectado
El módulo afecta la tabla `publicaciones` y requiere su relación con `usuarios`, así como la utilización de la tabla `amistades` para filtrar el contenido visible.

### Endpoints Propuestos
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/publicaciones` | Crear una nueva publicación |
| `PUT` | `/api/publicaciones/{id}` | Editar una publicación existente |
| `DELETE` | `/api/publicaciones/{id}` | Eliminar una publicación |
| `GET` | `/api/publicaciones/mias` | Obtener publicaciones propias |
| `GET` | `/api/publicaciones/feed` | Obtener el muro/feed (públicas y de amigos) |
| `GET` | `/api/publicaciones/buscar` | Buscar publicaciones |

---

## 🛠️ Módulo C: Roles, Moderación y Mantenimiento

Este módulo especifica las funciones relacionadas con los roles del sistema (administrador y técnico), bloqueo de usuarios, modo mantenimiento y eliminación de cuentas.

### Requisitos Funcionales Incluidos
* **RF-17**: Rol Administrador
* **RF-18**: Rol Técnico
* **RF-20**: Bloquear usuarios
* **RF-25**: Eliminar usuarios (admin)
* **RF-19**: Modo mantenimiento
* **RF-22**: Ver perfil ajeno (control de permisos)

### Roles y Permisos
| Rol | Permisos Clave |
| :--- | :--- |
| **Administrador** | Controla usuarios, elimina cuentas, cambia roles. |
| **Técnico** | Puede activar o desactivar modo mantenimiento. |
| **Usuario** | Sin permisos especiales. |

### Modo Mantenimiento
Cuando está activado, solo administrador y técnico pueden acceder al sistema. El resto recibe un mensaje de servicio no disponible.

### Modelo de Datos Afectado
Este módulo utiliza las tablas: `roles`, `usuarios`, `auditoria` y `configuracion_sistema`. Permite controlar permisos y acciones restringidas.

### Endpoints Propuestos (Admin/Sistema)
| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/admin/bloquear/{id}` | Bloquear a un usuario |
| `POST` | `/api/admin/desbloquear/{id}` | Desbloquear a un usuario |
| `DELETE` | `/api/admin/eliminar/{id}` | Eliminar la cuenta de un usuario |
| `POST` | `/api/sistema/mantenimiento/activar` | Activar modo mantenimiento |
| `POST` | `/api/sistema/mantenimiento/desactivar` | Desactivar modo mantenimiento |
| `GET` | `/api/sistema/mantenimiento/estado` | Obtener el estado del modo mantenimiento |
