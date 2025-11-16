// /js/login.js
document.addEventListener("DOMContentLoaded", () => {
    console.log("login.js cargado");

    const form = document.getElementById("loginForm");
    const inputIdentificador = document.getElementById("identificador"); // usuario o email en el mismo campo
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
                credentials: "same-origin",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    identificador,   // puede ser nombreUsuario o correo
                    password
                })
            });

            if (!response.ok) {
                const text = await response.text();
                alert(text || "Error al iniciar sesión");
                return;
            }

            // Intentamos leer JSON de forma segura
            let usuario;
            try {
                usuario = await response.json();
            } catch (e) {
                console.error("No se pudo parsear la respuesta como JSON:", e);
                alert("Formato de respuesta no válido (no es JSON)");
                return;
            }

            if (!usuario || typeof usuario !== "object") {
                console.error("Respuesta sin usuario válido:", usuario);
                alert("No se pudo recuperar los datos del usuario");
                return;
            }

            // Normalizamos la estructura para evitar fallos por propiedades faltantes
            const usuarioNormalizado = {
                id: usuario.id ?? usuario.usuarioId ?? null,
                usuarioId: usuario.usuarioId ?? usuario.id ?? null,
                nombre: usuario.nombre ?? "",
                apellidos: usuario.apellidos ?? "",
                nombreUsuario: usuario.nombreUsuario ?? usuario.nombre ?? "",
                email: usuario.email ?? usuario.correo ?? "",
                correo: usuario.correo ?? usuario.email ?? "",
                genero: usuario.genero ?? "",
                rol: usuario.rol ?? "",
                activo: usuario.activo ?? false
            };

            console.log("Usuario logueado:", usuarioNormalizado);

            // guardamos SIEMPRE con la MISMA clave
            localStorage.setItem("usuario", JSON.stringify(usuarioNormalizado));

            // redirigimos al muro
            window.location.href = "/html/muro.html";

        } catch (err) {
            console.error("❌ Error en login:", err);
            alert("Error de comunicación con el servidor");
        }
    });
});
