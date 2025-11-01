document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    // Por ahora, simulamos un login simple:
    if (username === "admin" && password === "1234") {
        // Guardamos el usuario en localStorage
        localStorage.setItem("usuario", username);
        // Redirigimos al muro (el blog)
        window.location.href = "muro.html";
    } else {
        alert("Usuario o contraseña incorrectos");
    }
});
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    const message = document.getElementById("loginMessage");

    if (!form) {
        return;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        message.hidden = true;
        message.classList.remove("error", "success");

        const payload = {
            email: form.email.value.trim(),
            password: form.password.value
        };

        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error("Credenciales inválidas");
            }

            const user = await response.json();
            setCurrentUser(user);
            message.textContent = "Inicio de sesión correcto. Redirigiendo...";
            message.classList.add("success");
            message.hidden = false;
            setTimeout(() => window.location.href = "muro.html", 700);
        } catch (error) {
            console.error(error);
            message.textContent = error.message || "No se pudo iniciar sesión";
            message.classList.add("error");
            message.hidden = false;
        }
    });
});
