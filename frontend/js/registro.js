document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");
    const message = document.getElementById("registerMessage");

    if (!form) {
        return;
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        message.hidden = true;
        message.classList.remove("error", "success");

        const payload = {
            nombre: form.nombre.value.trim(),
            email: form.email.value.trim(),
            password: form.password.value,
            biografia: form.biografia.value.trim()
        };

        try {
            const response = await fetch(`${API_BASE_URL}/usuarios`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorBody = await response.json().catch(() => null);
                throw new Error(errorBody?.message || "No se pudo registrar el usuario");
            }

            const user = await response.json();
            setCurrentUser(user);
            message.textContent = "Cuenta creada correctamente. Redirigiendo al muro...";
            message.classList.add("success");
            message.hidden = false;
            setTimeout(() => window.location.href = "muro.html", 900);
        } catch (error) {
            console.error(error);
            message.textContent = error.message || "Ocurrió un error al registrarse";
            message.classList.add("error");
            message.hidden = false;
        }
    });
});
