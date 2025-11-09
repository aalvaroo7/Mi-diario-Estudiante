const usuarioActivo = JSON.parse(localStorage.getItem("usuarioActivo"));
if (!usuarioActivo) window.location.href = "login.html";

document.body.classList.add("page-loaded");

window.addEventListener("scroll", () => {
  const header = document.querySelector("header");
  if (!header) return;
  if (window.scrollY > 10) {
    header.classList.add("scrolled");
  } else {
    header.classList.remove("scrolled");
  }
});

const nombreEl = document.getElementById("nombre");
const apellidosEl = document.getElementById("apellidos");
const nombreUsuarioEl = document.getElementById("nombreUsuario");
const generoEl = document.getElementById("genero");
const correoEl = document.getElementById("correo");

if (nombreEl) nombreEl.textContent = usuarioActivo.nombre;
if (apellidosEl) apellidosEl.textContent = usuarioActivo.apellidos;
if (nombreUsuarioEl) nombreUsuarioEl.textContent = usuarioActivo.nombreUsuario;
if (generoEl) generoEl.textContent = usuarioActivo.genero;
if (correoEl) correoEl.textContent = usuarioActivo.correo;

const contenedorPublicaciones = document.getElementById("misPublicaciones");

function mostrarMisPublicaciones() {
  if (!contenedorPublicaciones) return;

  const publicaciones = JSON.parse(localStorage.getItem("publicaciones")) || [];
  const misPosts = publicaciones.filter(p => p.autor === usuarioActivo.nombreUsuario);

  contenedorPublicaciones.innerHTML = "";

  if (misPosts.length === 0) {
    contenedorPublicaciones.innerHTML = '<p class="empty-state">Aún no has compartido publicaciones. ¡Comparte tu primera experiencia! ✨</p>';
    return;
  }

  misPosts
    .sort((a, b) => new Date(b.fecha) - new Date(a.fecha))
    .forEach((pub, index) => {
      const post = document.createElement("div");
      post.classList.add("post");
      post.style.setProperty("--delay", `${index * 0.08}s`);
      post.innerHTML = `
        <div class="post-header">
          <h3>${pub.autor}</h3>
          <small>${new Date(pub.fecha).toLocaleString()}</small>
        </div>
        <p>${pub.texto}</p>
        ${pub.imagen ? `<img src="${pub.imagen}" alt="imagen">` : ""}
      `;

      contenedorPublicaciones.appendChild(post);
    });
}

mostrarMisPublicaciones();

["logout", "volverMuro"].forEach(id => {
  const button = document.getElementById(id);
  if (!button) return;

  button.addEventListener("click", createRipple);
  if (id === "logout") {
    button.addEventListener("click", () => {
      localStorage.removeItem("usuarioActivo");
      window.location.href = "/html/login.html";
    });
  } else {
    button.addEventListener("click", () => {
      window.location.href = "/html/muro.html";
    });
  }
});

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
