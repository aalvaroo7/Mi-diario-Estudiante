document.getElementById("registroForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const usuario = {
        nombre: document.getElementById("nombre").value,
        apellidos: document.getElementById("apellidos").value,
        nombreUsuario: document.getElementById("nombreUsuario").value,
        genero: document.getElementById("genero").value,
        correo: document.getElementById("correo").value,
        password: document.getElementById("password").value,
        rol: "usuario"
    };

    fetch("http://localhost:8080/api/usuarios/registro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(usuario)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        if (data.includes("correctamente")) {
            window.location.href = "/html/login.html";
        }
    })
    .catch(error => {
        console.error("Error al registrar:", error);
        alert("Ocurrió un error al registrar el usuario.");
    });
});
