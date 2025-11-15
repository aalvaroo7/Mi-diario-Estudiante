document.getElementById("registroForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre = document.getElementById("nombre").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const data = {
        nombre: nombre,
        email: email,
        password: password
    };

    try {
        const response = await fetch("/api/auth/registro", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error("No se pudo registrar el usuario");
        }

        alert("Usuario registrado correctamente. Ahora inicia sesión.");
        window.location.href = "/html/login.html";

    } catch (error) {
        alert("Error: " + error);
        console.error(error);
    }
});
