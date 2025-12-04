document.addEventListener("DOMContentLoaded", () => {
    // 1. Verificar si hay usuario logueado. Si no, patada al login.
    const usuarioLocal = localStorage.getItem("usuario");
    if (!usuarioLocal) {
        window.location.href = "/index.html";
        return;
    }

    cargarUsuario();
    cargarPublicaciones();

    // 2. Configurar botones
    const publicarBtn = document.getElementById("publicarBtn");
    if (publicarBtn) {
        publicarBtn.addEventListener("click", publicar);
    }

    const logoutBtn = document.getElementById("logout");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", cerrarSesion);
    }

    const perfilBtn = document.getElementById("perfilBtn");
    if (perfilBtn) {
        perfilBtn.addEventListener("click", () => {
            window.location.href = "/html/perfil.html";
        });
    }
});

// ---------------------------------------------------------
// MOSTRAR DATOS DEL USUARIO EN LA BARRA LATERAL / CABECERA
// ---------------------------------------------------------
function cargarUsuario() {
    try {
        const usuarioGuardado = JSON.parse(localStorage.getItem("usuario"));
        if (usuarioGuardado) {
            // Nombre de usuario
            const nombreEl = document.getElementById("nombreUsuario");
            if (nombreEl) nombreEl.textContent = usuarioGuardado.nombreUsuario || "Usuario";

            // Opcional: Si tienes una imagen de perfil en la barra lateral
            // const fotoEl = document.getElementById("miFotoSidebar");
            // if (fotoEl && usuarioGuardado.fotoPerfil) fotoEl.src = usuarioGuardado.fotoPerfil;
        }
    } catch (e) {
        console.error("Error leyendo usuario local:", e);
    }
}

// ---------------------------------------------------------
// PUBLICAR NUEVO MENSAJE
// ---------------------------------------------------------
async function publicar() {
    const textoInput = document.getElementById("postTexto");
    const imagenInput = document.getElementById("postImagen");

    const contenido = textoInput.value;
    const archivo = imagenInput.files[0];

    if (!contenido && !archivo) {
        alert("Escribe algo o selecciona una imagen.");
        return;
    }

    const formData = new FormData();
    formData.append("contenido", contenido);
    if (archivo) {
        formData.append("archivo", archivo);
    }

    try {
        const response = await fetch('/api/publicaciones/crear', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            textoInput.value = "";
            imagenInput.value = "";
            cargarPublicaciones(); // Recargar el muro para ver el nuevo post
        } else {
            if (response.status === 401) {
                window.location.href = "/index.html";
            } else {
                const msg = await response.text();
                alert("Error al publicar: " + msg);
            }
        }
    } catch (error) {
        console.error("Error:", error);
        alert("Error de conexión.");
    }
}

// ---------------------------------------------------------
// CARGAR EL MURO (FEED)
// ---------------------------------------------------------
async function cargarPublicaciones() {
    try {
        const response = await fetch('/api/publicaciones/todas');

        if (response.status === 401) {
            window.location.href = "/index.html";
            return;
        }

        const publicaciones = await response.json();
        const feed = document.getElementById("feed");
        feed.innerHTML = ""; // Limpiar antes de pintar

        // Ordenar: más recientes primero
        publicaciones.sort((a, b) => new Date(b.fechaPublicacion) - new Date(a.fechaPublicacion));

        if (publicaciones.length === 0) {
            feed.innerHTML = "<p style='text-align:center; padding:20px;'>No hay publicaciones aún. ¡Sé el primero!</p>";
            return;
        }

        // Obtenemos ID del usuario actual para saber si mostrar el botón de borrar
        const usuarioActual = JSON.parse(localStorage.getItem("usuario"));
        const miId = usuarioActual ? usuarioActual.id : null;

        publicaciones.forEach(pub => {
            const postDiv = document.createElement("div");
            postDiv.className = "post";

            const fecha = new Date(pub.fechaPublicacion).toLocaleString();

            // --- LÓGICA DE FOTO DE PERFIL ---
            let avatarUrl = "";

            // 1. Si el autor del post tiene foto en BD, la usamos
            if (pub.usuario && pub.usuario.fotoPerfil) {
                avatarUrl = pub.usuario.fotoPerfil;
            }
            // 2. Si no, usamos avatar de letras (ui-avatars)
            else {
                const nombreParaAvatar = pub.usuario ? pub.usuario.nombreUsuario : "A";
                avatarUrl = `https://ui-avatars.com/api/?name=${nombreParaAvatar}&background=random&rounded=true`;
            }

            // Crear el HTML de la imagen redonda pequeña
            const avatarHtml = `<img src="${avatarUrl}" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover; margin-right: 10px; border: 1px solid #ddd;">`;

            // Botón de borrar (solo si es mi post)
            let botonBorrar = "";
            if (pub.usuario && pub.usuario.id === miId) {
                botonBorrar = `<button onclick="borrarPublicacion(${pub.id})" class="borrar-btn" style="background:none; border:none; cursor:pointer;">🗑️</button>`;
            }

            // Imagen del contenido del post (si la hay)
            let imagenPostHtml = "";
            if (pub.imagenUrl) {
                imagenPostHtml = `<img src="${pub.imagenUrl}" class="post-img" style="max-width:100%; margin-top:10px; border-radius: 8px;">`;
            }

            postDiv.innerHTML = `
                <div class="post-header" style="display:flex; align-items: center; justify-content:space-between; margin-bottom: 10px;">
                    <div style="display:flex; align-items:center;">
                        ${avatarHtml}
                        <div>
                            <strong style="display:block; line-height:1;">${pub.usuario ? pub.usuario.nombreUsuario : "Anónimo"}</strong>
                            <small style="color: #666; font-size: 0.8em;">${fecha}</small>
                        </div>
                    </div>
                    ${botonBorrar}
                </div>
                <div class="post-content">
                    <p style="margin: 0; white-space: pre-wrap;">${pub.contenido}</p>
                    ${imagenPostHtml}
                </div>
                <hr style="margin-top: 15px; border: 0; border-top: 1px solid #eee;">
            `;
            feed.appendChild(postDiv);
        });

    } catch (error) {
        console.error("Error cargando feed:", error);
    }
}

// ---------------------------------------------------------
// BORRAR PUBLICACIÓN
// ---------------------------------------------------------
async function borrarPublicacion(id) {
    if(!confirm("¿Seguro que quieres borrar esta publicación?")) return;

    try {
        const response = await fetch(`/api/publicaciones/eliminar/${id}`, { method: 'DELETE' });
        if (response.ok) {
            cargarPublicaciones(); // Recargar lista
        } else {
            alert("No se pudo eliminar.");
        }
    } catch (e) {
        console.error(e);
        alert("Error de conexión");
    }
}

// ---------------------------------------------------------
// CERRAR SESIÓN (SOLUCIÓN AL ERROR WHITELABEL)
// ---------------------------------------------------------
function cerrarSesion(event) {
    if(event) event.preventDefault(); // Evita que el botón recargue la página si es un form

    // 1. Limpiamos datos locales inmediatamente
    localStorage.removeItem("usuario");

    // 2. Intentamos avisar al servidor
    fetch('/api/usuarios/logout', {
        method: 'POST'
    })
        .finally(() => {
            // 3. Pase lo que pase (éxito o error 404), redirigimos al login
            window.location.href = "/html/login.html";
        });
}