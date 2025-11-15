document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const identificador = usernameInput.value.trim(); // puede ser email O usuario
        const password = passwordInput.value.trim();

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    identificador: identificador,  // campo único
                    password: password
                })
            });

            if (!response.ok) throw new Error("Credenciales incorrectas");

            const data = await response.json();
            localStorage.setItem("usuario", JSON.stringify(data));

            window.location.href = "/html/muro.html";

        } catch (err) {
            alert("Error al iniciar sesión");
            console.error(err);
        }
    });
});
