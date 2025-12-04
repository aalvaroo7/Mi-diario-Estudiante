// /js/registro.js
document.addEventListener("DOMContentLoaded", () => {
    console.log("registro.js cargado");

    const form = document.getElementById("registroForm");

    if (!form) {
        console.error("❌ No se encontró el formulario de registro");
        return;
    }

    const nombre = document.getElementById("nombre");
    const apellidos = document.getElementById("apellidos");
    const nombreUsuario = document.getElementById("nombreUsuario");
    const genero = document.getElementById("genero");
    const correo = document.getElementById("correo");
    const password = document.getElementById("password");

    form.addEventListener("submit", async e => {
        e.preventDefault();

        const payload = {
            nombre: nombre.value.trim(),
            apellidos: apellidos.value.trim(),
            nombreUsuario: nombreUsuario.value.trim(),
            genero: genero.value,
            email: correo.value.trim(),
            password: password.value.trim()
        };
        console.log("Payload enviado:", payload);

        if (!payload.nombre || !payload.nombreUsuario || !payload.email || !payload.password) {
            alert("Rellena al menos nombre, usuario, correo y contraseña");
            return;
        }


        try {
            const res = await fetch("/auth/registro", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            const text = await res.text();
            console.log("Respuesta registro:", text);

            if (!res.ok) {
                alert(text || "Error al registrar usuario");
                return;
            }

            alert("Usuario registrado correctamente. Ahora puedes iniciar sesión.");
            window.location.href = "/html/login.html";
        } catch (err) {
            console.error("Error en registro:", err);
            alert("Error de comunicación con el servidor");
        }
    });
});
