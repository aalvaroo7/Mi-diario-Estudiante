const usuarioActivo = JSON.parse(localStorage.getItem("usuarioActivo"));
if (!usuarioActivo) window.location.href = "login.html";

document.getElementById("nombreUsuario").textContent = usuarioActivo.nombreUsuario;

// Cerrar sesión
document.getElementById("logout").addEventListener("click", () => {
  localStorage.removeItem("usuarioActivo");
  window.location.href = "/html/login.html";
});

// Publicar texto + imagen
document.getElementById("publicarBtn").addEventListener("click", () => {
  const texto = document.getElementById("postTexto").value.trim();
  const imagenInput = document.getElementById("postImagen");

  if (!texto && (!imagenInput.files || !imagenInput.files.length)) {
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
      document.getElementById("postTexto").value = "";
      imagenInput.value = "";
      cargarPublicaciones();
    })
    .catch(err => {
      console.error(err);
      alert("No se pudo crear la publicación.");
    });
});

// Cargar publicaciones
function cargarPublicaciones() {
  fetch("http://localhost:8080/api/publicaciones/todas")
    .then(r => r.json())
    .then(publicaciones => {
      const feed = document.getElementById("feed");
      feed.innerHTML = "";

      publicaciones
        .sort((a, b) => new Date(b.fechaPublicacion) - new Date(a.fechaPublicacion))
        .forEach(pub => {
          const post = document.createElement("div");
          post.className = "post";
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

cargarPublicaciones();
