// /js/muro.js
console.log("muro.js cargado");

// Recuperamos el usuario guardado en login
const usuarioActivo = JSON.parse(localStorage.getItem("usuario"));

if (!usuarioActivo) {
    console.warn("Usuario no logueado → redirigiendo a login...");
    window.location.href = "/html/login.html";
}

// Referencias a elementos del DOM
const nombreUsuarioEl = document.getElementById("nombreUsuario");
const publicarBtn = document.getElementById("publicarBtn");
const logoutBtn = document.getElementById("logout");
const perfilBtn = document.getElementById("perfilBtn");
const postTexto = document.getElementById("postTexto");
const postImagen = document.getElementById("postImagen");

// Animación de entrada de la página
document.body.classList.add("page-loaded");

// Pintamos el nombre del usuario (si existe el elemento)
if (nombreUsuarioEl && usuarioActivo && usuarioActivo.nombreUsuario) {
    nombreUsuarioEl.textContent = usuarioActivo.nombreUsuario;
}

// Efecto header al hacer scroll
window.addEventListener("scroll", () => {
    const header = document.querySelector("header");
    if (!header) return;

    if (window.scrollY > 10) {
        header.classList.add("scrolled");
    } else {
        header.classList.remove("scrolled");
    }
});

// Efecto ripple en botones
[logoutBtn, perfilBtn, publicarBtn].filter(Boolean).forEach(btn => {
    btn.addEventListener("click", createRipple);
});

// Estilos de inputs
document
    .querySelectorAll(".crear-post textarea, .crear-post input")
    .forEach(field => {
        const toggleState = () => {
            if (field.value && field.value.trim() !== "") {
                field.classList.add("has-value");
            } else {
                field.classList.remove("has-value");
            }
        };

        toggleState();
        field.addEventListener("focus", () => field.classList.add("is-focused"));
        field.addEventListener("blur", () => field.classList.remove("is-focused"));
        field.addEventListener("input", toggleState);
    });

// Navegar al perfil
if (perfilBtn) {
    perfilBtn.addEventListener("click", () => {
        window.location.href = "/html/perfil.html";
    });
}

// Logout
if (logoutBtn) {
    logoutBtn.addEventListener("click", async () => {
        try {
            // Opcional: notificar al backend para auditoría/logout real
            await fetch("/auth/logout", { method: "POST" }).catch(() => {});
        } catch (_) {}

        localStorage.removeItem("usuario");
        window.location.href = "/html/login.html";
    });
}

// Crear publicación
if (publicarBtn) {
    publicarBtn.addEventListener("click", () => {
        if (!usuarioActivo || !usuarioActivo.nombreUsuario) {
            alert("Sesión inválida. Vuelve a iniciar sesión.");
            window.location.href = "/html/login.html";
            return;
        }

        const texto = postTexto.value.trim();
        const imagenInput = postImagen;

        if (!texto && (!imagenInput.files || !imagenInput.files.length)) {
            animateError(postTexto.closest(".crear-post"));
            alert("Escribe algo o selecciona una imagen para publicar.");
            return;
        }

        fetch("/api/publicaciones/crear", {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                contenido: texto
            })
        })
            .then(res => {
                if (!res.ok) {
                    throw new Error("Error al crear publicación");
                }
                return res.text();
            })
            .then(() => {
                postTexto.value = "";
                imagenInput.value = "";
                cargarPublicaciones({ highlightLatest: true });
            })
            .catch(err => {
                console.error(err);
                animateError(postTexto.closest(".crear-post"));
                alert("No se pudo crear la publicación.");
            });
    });
}

// Cargar publicaciones
function cargarPublicaciones({ highlightLatest } = {}) {
    fetch("/api/publicaciones/todas", { credentials: "same-origin" })
        .then(r => r.json())
        .then(publicaciones => {
            const feed = document.getElementById("feed");
            if (!feed) return;

            feed.innerHTML = "";

            if (!publicaciones.length) {
                feed.innerHTML = '<p class="empty-state">Sé el primero en compartir algo hoy ✨</p>';
                return;
            }

            publicaciones
                .sort((a, b) => new Date(b.fechaPublicacion) - new Date(a.fechaPublicacion))
                .forEach((pub, index) => {
                    const post = document.createElement("div");
                    post.className = "post";
                    post.style.setProperty("--delay", `${index * 0.07}s`);

                    const esAutor =
                        pub.autor &&
                        pub.autor.nombreUsuario &&
                        usuarioActivo &&
                        pub.autor.nombreUsuario === usuarioActivo.nombreUsuario;

                    post.innerHTML = `
                        <div class="post-header">
                          <h3>${pub.autor?.nombreUsuario ?? "Usuario"}</h3>
                          <small>${new Date(pub.fechaPublicacion).toLocaleString()}</small>
                          ${
                        esAutor
                            ? `<button class="borrar-btn" data-id="${pub.id}">🗑️</button>`
                            : ""
                    }
                        </div>
                        ${pub.contenido ? `<p>${pub.contenido}</p>` : ""}
                        ${
                        pub.imagenUrl
                            ? `<img src="${pub.imagenUrl}" class="post-img">`
                            : ""
                    }
                    `;
                    feed.appendChild(post);

                    if (highlightLatest && index === 0) {
                        post.classList.add("recent");
                        post.addEventListener(
                            "animationend",
                            event => {
                                if (event.animationName === "recentPulse") {
                                    post.classList.remove("recent");
                                }
                            },
                            { once: true }
                        );
                    }

                    post.querySelectorAll("button").forEach(btn =>
                        btn.addEventListener("click", createRipple)
                    );
                });

            document.querySelectorAll(".borrar-btn").forEach(btn => {
                btn.addEventListener("click", e =>
                    borrarPublicacion(e.currentTarget.dataset.id)
                );
            });
        })
        .catch(err => console.error("Error al cargar publicaciones:", err));
}

function borrarPublicacion(id) {
    fetch(`/api/publicaciones/eliminar/${id}`, {
        method: "DELETE",
        credentials: "same-origin"
    })
        .then(() => cargarPublicaciones())
        .catch(err => console.error("Error al borrar publicación:", err));
}

function animateError(element) {
    if (!element) return;
    element.classList.remove("shake");
    void element.offsetWidth; // fuerza reflow
    element.classList.add("shake");
    element.addEventListener(
        "animationend",
        () => element.classList.remove("shake"),
        { once: true }
    );
}

function createRipple(event) {
    const button = event.currentTarget;
    if (!button) return;

    const rect = button.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const ripple = document.createElement("span");
    ripple.classList.add("ripple");
    ripple.style.width = ripple.style.height = `${size}px`;
    ripple.style.left = `${event.clientX - rect.left}px`;
    ripple.style.top = `${event.clientY - rect.top}px`;

    button.appendChild(ripple);
    ripple.addEventListener("animationend", () => ripple.remove());
}

// Cargar el feed al entrar
cargarPublicaciones();
