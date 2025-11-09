document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const nombreUsuario = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch(`http://localhost:8080/api/usuarios/login?nombreUsuario=${nombreUsuario}&password=${password}`, {
        method: "POST"
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Error al iniciar sesión");
        }
        return response.json();
    })
    .then(usuario => {
        if (usuario && usuario.nombreUsuario) {
            localStorage.setItem("usuarioActivo", JSON.stringify(usuario));
            window.location.href = "/html/muro.html";

        } else {
            alert("Usuario o contraseña incorrectos.");
        }
    })
    .catch(error => {
        console.error("Error:", error);
        alert("No se pudo iniciar sesión.");
    });
});
