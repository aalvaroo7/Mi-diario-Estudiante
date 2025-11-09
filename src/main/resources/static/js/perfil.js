const usuarioActivo = JSON.parse(localStorage.getItem("usuarioActivo"));
if (!usuarioActivo) window.location.href = "login.html";

// Mostrar datos del usuario
document.getElementById("nombre").textContent = usuarioActivo.nombre;
document.getElementById("apellidos").textContent = usuarioActivo.apellidos;
document.getElementById("nombreUsuario").textContent = usuarioActivo.nombreUsuario;
document.getElementById("genero").textContent = usuarioActivo.genero;
document.getElementById("correo").textContent = usuarioActivo.correo;

// Mostrar publicaciones del usuario
function mostrarMisPublicaciones() {
  const publicaciones = JSON.parse(localStorage.getItem("publicaciones")) || [];
  const misPosts = publicaciones.filter(p => p.autor === usuarioActivo.nombreUsuario);
  const contenedor = document.getElementById("misPublicaciones");

  contenedor.innerHTML = "";

  if (misPosts.length === 0) {
    contenedor.innerHTML = "<p>No tienes publicaciones aún.</p>";
    return;
  }

  misPosts.forEach(pub => {
    const post = document.createElement("div");
    post.classList.add("post");

    post.innerHTML = `
      <div class="post-header">
        <h3>${pub.autor}</h3>
        <small>${new Date(pub.fecha).toLocaleString()}</small>
      </div>
      <p>${pub.texto}</p>
      ${pub.imagen ? `<img src="${pub.imagen}" alt="imagen">` : ""}
    `;

    contenedor.appendChild(post);
  });
}

mostrarMisPublicaciones();

// Botones
document.getElementById("logout").addEventListener("click", () => {
  localStorage.removeItem("usuarioActivo");
  window.location.href = "/html/login.html";
});

document.getElementById("volverMuro").addEventListener("click", () => {
  window.location.href = "/html/muro.html";
});
