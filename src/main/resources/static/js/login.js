// /js/login.js
document.addEventListener("DOMContentLoaded", () => {
    console.log("login.js cargado");

    const form = document.getElementById("loginForm");
    const inputIdentificador = document.getElementById("username"); // usuario o email en el mismo campo
    const inputPassword = document.getElementById("password");

    if (!form || !inputIdentificador || !inputPassword) {
        console.error("❌ ERROR: No se encontró el formulario o los inputs de login");
        return;
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const identificador = inputIdentificador.value.trim();
        const password = inputPassword.value.trim();

        console.log("Enviando:", identificador, password);

        if (!identificador || !password) {
            alert("Introduce usuario/email y contraseña");
            return;
        }

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    identificador,   // puede ser nombreUsuario o correo
                    password
                })
            });

            const text = await response.text();
            console.log("Respuesta backend cruda:", text);

            if (!response.ok) {
                alert(text || "Error al iniciar sesión");
                return;
            }

            // El backend debe devolver aquí el usuario en JSON.
            // Si text es JSON, lo parseamos:
            let usuario;
            try {
                usuario = JSON.parse(text);
            } catch (e) {
                console.error("No se pudo parsear la respuesta como JSON:", e);
                alert("Formato de respuesta no válido (no es JSON)");
                return;
            }

            // Esperamos que el objeto tenga al menos nombreUsuario y/o correo
            console.log("Usuario logueado:", usuario);

            // guardamos SIEMPRE con la MISMA clave
            localStorage.setItem("usuario", JSON.stringify(usuario));

            // redirigimos al muro
            window.location.href = "/html/muro.html";

        } catch (err) {
            console.error("❌ Error en login:", err);
            alert("Error de comunicación con el servidor");
        }
    });
});
