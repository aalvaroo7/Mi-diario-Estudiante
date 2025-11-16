// /js/perfil.js
document.addEventListener("DOMContentLoaded", () => {
    console.log("perfil.js cargado");

    const usuario = JSON.parse(localStorage.getItem("usuario"));

    if (!usuario) {
        window.location.href = "/html/login.html";
        return;
    }

    // Ajusta estos IDs a lo que tengas en perfil.html
    const nombreEl = document.getElementById("perfilNombre");
    const usuarioEl = document.getElementById("perfilUsuario");
    const correoEl = document.getElementById("perfilCorreo");
    const generoEl = document.getElementById("perfilGenero");

    if (nombreEl) nombreEl.textContent = usuario.nombre || "";
    if (usuarioEl) usuarioEl.textContent = usuario.nombreUsuario || "";
    if (correoEl) correoEl.textContent = usuario.correo || usuario.email || "";
    if (generoEl) generoEl.textContent = usuario.genero || "";

    const volverBtn = document.getElementById("volverMuro");
    if (volverBtn) {
        volverBtn.addEventListener("click", () => {
            window.location.href = "/html/muro.html";
        });
    }
});
