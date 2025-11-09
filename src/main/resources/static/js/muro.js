const usuarioActivo = JSON.parse(localStorage.getItem("usuarioActivo"));
if (!usuarioActivo) window.location.href = "login.html";

const nombreUsuarioEl = document.getElementById("nombreUsuario");
const publicarBtn = document.getElementById("publicarBtn");
const logoutBtn = document.getElementById("logout");
const perfilBtn = document.getElementById("perfilBtn");
const postTexto = document.getElementById("postTexto");
const postImagen = document.getElementById("postImagen");

document.body.classList.add("page-loaded");

if (nombreUsuarioEl) {
  nombreUsuarioEl.textContent = usuarioActivo.nombreUsuario;
}

window.addEventListener("scroll", () => {
  const header = document.querySelector("header");
  if (!header) return;
  if (window.scrollY > 10) {
    header.classList.add("scrolled");
  } else {
    header.classList.remove("scrolled");
  }
});

[logoutBtn, perfilBtn, publicarBtn].filter(Boolean).forEach(btn => {
  btn.addEventListener("click", createRipple);
});

document.querySelectorAll(".crear-post textarea, .crear-post input").forEach(field => {
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

if (perfilBtn) {
  perfilBtn.addEventListener("click", () => {
    window.location.href = "/html/perfil.html";
  });
}

if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("usuarioActivo");
    window.location.href = "/html/login.html";
  });
}

if (publicarBtn) {
  publicarBtn.addEventListener("click", () => {
    const texto = postTexto.value.trim();
    const imagenInput = postImagen;

    if (!texto && (!imagenInput.files || !imagenInput.files.length)) {
      animateError(postTexto.closest(".crear-post"));
      alert("Escribe algo o selecciona una imagen para publicar.");
      return;
    }

    const fd = new FormData();
    fd.append("nombreUsuario", usuarioActivo.nombreUsuario);
    fd.append("contenido", texto);
    if (imagenInput.files.length > 0) {
      fd.append("imagen", imagenInput.files[0]);
    }

    fetch("http://localhost:8080/api/publicaciones/crear", {
      method: "POST",
      body: fd
    })
      .then(res => {
        if (!res.ok) throw new Error("Error al crear publicación");
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

function cargarPublicaciones({ highlightLatest } = {}) {
  fetch("http://localhost:8080/api/publicaciones/todas")
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
          const esAutor = pub.autor?.nombreUsuario === usuarioActivo.nombreUsuario;
          post.innerHTML = `
            <div class="post-header">
              <h3>${pub.autor?.nombreUsuario ?? "Usuario"}</h3>
              <small>${new Date(pub.fechaPublicacion).toLocaleString()}</small>
              ${esAutor ? `<button class="borrar-btn" data-id="${pub.id}">🗑️</button>` : ""}
            </div>
            ${pub.contenido ? `<p>${pub.contenido}</p>` : ""}
            ${pub.imagenUrl ? `<img src="${pub.imagenUrl}" class="post-img">` : ""}
          `;
          feed.appendChild(post);

          if (highlightLatest && index === 0) {
            post.classList.add("recent");
            post.addEventListener("animationend", (event) => {
              if (event.animationName === "recentPulse") {
                post.classList.remove("recent");
              }
            });
          }

          post.querySelectorAll("button").forEach(btn => btn.addEventListener("click", createRipple));
        });

      document.querySelectorAll(".borrar-btn").forEach(btn => {
        btn.addEventListener("click", e => borrarPublicacion(e.currentTarget.dataset.id));
      });
    })
    .catch(err => console.error("Error al cargar publicaciones:", err));
}

function borrarPublicacion(id) {
  fetch(`http://localhost:8080/api/publicaciones/eliminar/${id}`, { method: "DELETE" })
    .then(() => cargarPublicaciones());
}

function animateError(element) {
  if (!element) return;
  element.classList.remove("shake");
  void element.offsetWidth;
  element.classList.add("shake");
  element.addEventListener("animationend", () => element.classList.remove("shake"), { once: true });
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

cargarPublicaciones();
