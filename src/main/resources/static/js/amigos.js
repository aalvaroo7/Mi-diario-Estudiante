// Configuración base
const API_BASE_URL = 'http://localhost:8080'; // Tu puerto es 8080

// Inicializar cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', async function() {
    try {
        // Cargar datos iniciales
        await loadAllData();

        // Configurar eventos
        setupEventListeners();

        // Añadir efecto ripple a todos los botones
        addRippleEffects();

    } catch (error) {
        console.error('Error al inicializar:', error);
        showNotification('Error al cargar los datos', 'error');
    }
});

// ========== FUNCIONES PRINCIPALES ==========

async function loadAllData() {
    await loadFriendRequests();
    await loadFriends();
}

async function loadFriendRequests() {
    const requestsContainer = document.getElementById('friendRequests');

    try {
        const response = await fetch(`${API_BASE_URL}/api/solicitudes/pendientes`, {
            credentials: 'include' // Importante para enviar cookies de sesión
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) throw new Error('Error al cargar solicitudes');

        const requests = await response.json();

        if (requests.length === 0) {
            requestsContainer.innerHTML = '<div class="empty-message">No tienes solicitudes pendientes</div>';
            return;
        }

        let html = '';
        for (const request of requests) {
            const solicitante = request.solicitante;

            html += `
                <div class="request-item" data-request-id="${request.id}">
                    <div>
                        <strong>${solicitante.nombre} ${solicitante.apellidos}</strong>
                        <div style="font-size: 0.9em; color: #6b7280;">
                            @${solicitante.nombreUsuario} • ${solicitante.email}
                        </div>
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button class="btn btn-primary" onclick="acceptRequest(${request.id})">Aceptar</button>
                        <button class="btn btn-danger" onclick="declineRequest(${request.id})">Rechazar</button>
                    </div>
                </div>
            `;
        }

        requestsContainer.innerHTML = html;

    } catch (error) {
        console.error('Error cargando solicitudes:', error);
        requestsContainer.innerHTML = '<div class="empty-message">Error al cargar solicitudes</div>';
    }
}

async function loadFriends() {
    const friendsContainer = document.getElementById('friendsList');

    try {
        const response = await fetch(`${API_BASE_URL}/api/amigos/lista`, {
            credentials: 'include'
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) throw new Error('Error al cargar amigos');

        const amigos = await response.json();

        if (amigos.length === 0) {
            friendsContainer.innerHTML = '<div class="empty-message">Aún no tienes amigos. ¡Busca y agrega algunos!</div>';
            return;
        }

        let html = '';
        for (const amigo of amigos) {
            html += `
                <div class="friend-item" data-user-id="${amigo.id}">
                    <div>
                        <strong>${amigo.nombre} ${amigo.apellidos}</strong>
                        <div style="font-size: 0.9em; color: #6b7280;">
                            @${amigo.nombreUsuario} • ${amigo.email}
                        </div>
                        ${amigo.descripcion ? `<div style="font-size: 0.85em; margin-top: 0.25rem; color: #4b5563;">${amigo.descripcion}</div>` : ''}
                    </div>
                    <button class="btn btn-danger" onclick="removeFriend(${amigo.id})">Eliminar</button>
                </div>
            `;
        }

        friendsContainer.innerHTML = html;

    } catch (error) {
        console.error('Error cargando amigos:', error);
        friendsContainer.innerHTML = '<div class="empty-message">Error al cargar amigos</div>';
    }
}

async function searchUsers() {
    const searchInput = document.getElementById('searchInput').value.trim();
    const resultsContainer = document.getElementById('searchResults');

    if (!searchInput) {
        resultsContainer.innerHTML = '<div class="empty-message">Escribe en el buscador para encontrar amigos</div>';
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/usuarios/buscar?q=${encodeURIComponent(searchInput)}`, {
            credentials: 'include'
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) throw new Error('Error en la búsqueda');

        const users = await response.json();

        if (users.length === 0) {
            resultsContainer.innerHTML = '<div class="empty-message">No se encontraron usuarios</div>';
            return;
        }

        // Filtrar y verificar estado de cada usuario
        let html = '';
        for (const user of users) {
            // Verificar si ya son amigos
            const esAmigo = await checkIfFriends(user.id);
            // Verificar si hay solicitud pendiente
            const tieneSolicitud = await checkPendingRequest(user.id);

            html += `
                <div class="search-item" data-user-id="${user.id}">
                    <div>
                        <strong>${user.nombre} ${user.apellidos}</strong>
                        <div style="font-size: 0.9em; color: #6b7280;">
                            @${user.nombreUsuario} • ${user.email}
                        </div>
                        ${user.descripcion ? `<div style="font-size: 0.85em; margin-top: 0.25rem; color: #4b5563;">${user.descripcion}</div>` : ''}
                    </div>
                    ${esAmigo ?
                '<button class="btn" disabled>Ya son amigos</button>' :
                tieneSolicitud ?
                    '<button class="btn" disabled>Solicitud enviada</button>' :
                    `<button class="btn btn-secondary" onclick="sendFriendRequest(${user.id})">Agregar</button>`
            }
                </div>
            `;
        }

        resultsContainer.innerHTML = html;

    } catch (error) {
        console.error('Error buscando usuarios:', error);
        resultsContainer.innerHTML = '<div class="empty-message">Error en la búsqueda</div>';
    }
}

// ========== FUNCIONES DE ACCIÓN ==========

async function sendFriendRequest(friendId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/solicitudes/enviar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                destinatario_id: friendId
            })
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Error al enviar solicitud');
        }

        // Actualizar búsqueda
        await searchUsers();

        // Mostrar confirmación
        showNotification('Solicitud enviada correctamente', 'success');

    } catch (error) {
        console.error('Error enviando solicitud:', error);
        showNotification(error.message || 'Error al enviar solicitud', 'error');
    }
}

async function acceptRequest(requestId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/solicitudes/${requestId}/aceptar`, {
            method: 'PUT',
            credentials: 'include'
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) throw new Error('Error al aceptar solicitud');

        // Actualizar las vistas
        await loadFriendRequests();
        await loadFriends();

        // Mostrar confirmación
        showNotification('Solicitud aceptada', 'success');

    } catch (error) {
        console.error('Error aceptando solicitud:', error);
        showNotification('Error al aceptar solicitud', 'error');
    }
}

async function declineRequest(requestId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/solicitudes/${requestId}/rechazar`, {
            method: 'PUT',
            credentials: 'include'
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) throw new Error('Error al rechazar solicitud');

        // Actualizar vista
        await loadFriendRequests();

        // Mostrar confirmación
        showNotification('Solicitud rechazada', 'success');

    } catch (error) {
        console.error('Error rechazando solicitud:', error);
        showNotification('Error al rechazar solicitud', 'error');
    }
}

async function removeFriend(friendId) {
    if (!confirm('¿Estás seguro de que quieres eliminar a este amigo?')) {
        return;
    }

    try {
        // Primero necesitamos obtener el ID de la amistad
        const amistadId = await findFriendshipId(friendId);

        if (!amistadId) {
            showNotification('No se encontró la amistad', 'error');
            return;
        }

        const response = await fetch(`${API_BASE_URL}/api/amigos/${amistadId}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }

        if (!response.ok) throw new Error('Error al eliminar amigo');

        // Actualizar vista
        await loadFriends();

        // Mostrar confirmación
        showNotification('Amigo eliminado', 'success');

    } catch (error) {
        console.error('Error eliminando amigo:', error);
        showNotification('Error al eliminar amigo', 'error');
    }
}

// ========== FUNCIONES AUXILIARES ==========

async function findFriendshipId(friendId) {
    try {
        // En un sistema real, necesitarías un endpoint específico para esto
        // Por ahora, usamos este método temporal
        const response = await fetch(`${API_BASE_URL}/api/amigos/lista`, {
            credentials: 'include'
        });

        if (!response.ok) return null;

        const amigos = await response.json();

        // Buscar si el usuario es amigo
        for (const amigo of amigos) {
            if (amigo.id === friendId) {
                // Necesitamos el ID de la amistad, no del usuario
                // Esto es temporal - necesitarás crear un endpoint específico
                return friendId; // Temporal: usar el ID del usuario
            }
        }

        return null;

    } catch (error) {
        console.error('Error buscando amistad:', error);
        return null;
    }
}

async function checkIfFriends(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/amigos/check/${userId}`, {
            credentials: 'include'
        });

        if (!response.ok) return false;

        const data = await response.json();
        return data.esAmigo || false;

    } catch (error) {
        console.error('Error verificando amistad:', error);
        return false;
    }
}

async function checkPendingRequest(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/solicitudes/check/${userId}`, {
            credentials: 'include'
        });

        if (!response.ok) return false;

        const data = await response.json();
        return data.tieneSolicitud || false;

    } catch (error) {
        console.error('Error verificando solicitud:', error);
        return false;
    }
}

function setupEventListeners() {
    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');

    if (searchBtn) {
        searchBtn.addEventListener('click', searchUsers);
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') searchUsers();
        });
    }
}

function addRippleEffects() {
    document.querySelectorAll('button').forEach(button => {
        button.addEventListener('click', function(e) {
            createRippleEffect(e, this);
        });
    });
}

function createRippleEffect(event, element) {
    const ripple = document.createElement('span');
    const rect = element.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;

    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = x + 'px';
    ripple.style.top = y + 'px';
    ripple.classList.add('ripple');

    element.appendChild(ripple);

    setTimeout(() => {
        ripple.remove();
    }, 750);
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.textContent = message;

    const bgColor = type === 'error'
        ? 'linear-gradient(135deg, #f44336, #e91e63)'
        : 'linear-gradient(135deg, #4CAF50, #3f51b5)';

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${bgColor};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 12px;
        box-shadow: 0 15px 35px rgba(63, 81, 181, 0.25);
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Añadir estilos de animación para notificaciones (ESTA ES LA PARTE QUE FALTABA)
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);