document.addEventListener("DOMContentLoaded", () => {
    cargarDatos();

    // Botón volver
    document.getElementById("volverBtn").addEventListener("click", () => {
        window.location.href = "/html/muro.html";
    });

    // Guardar cambios
    document.getElementById("perfilForm").addEventListener("submit", guardarCambios);

    // Previsualización de imagen al seleccionar
    document.getElementById("fileInput").addEventListener("change", function(e) {
        if (e.target.files && e.target.files[0]) {
            const reader = new FileReader();
            reader.onload = function(event) {
                // Muestra la foto inmediatamente
                document.getElementById("avatarImg").src = event.target.result;
            };
            reader.readAsDataURL(e.target.files[0]);
        }
    });

    // Actualizar nombre grande mientras escribes
    document.getElementById("nombre").addEventListener("input", (e) => {
        document.getElementById("headerNombre").textContent = e.target.value;
    });
});

// Función para convertir archivo a Base64
const convertBase64 = (file) => {
    return new Promise((resolve, reject) => {
        const fileReader = new FileReader();
        fileReader.readAsDataURL(file);
        fileReader.onload = () => resolve(fileReader.result);
        fileReader.onerror = (error) => reject(error);
    });
};

async function cargarDatos() {
    const usuarioLocal = JSON.parse(localStorage.getItem("usuario"));

    if (!usuarioLocal) {
        window.location.href = "/index.html";
        return;
    }

    // Intentamos obtener datos frescos del servidor
    // Si no tienes endpoint GET específico, usará los del localStorage
    try {
        // Rellenar campos
        document.getElementById("headerNombre").textContent = usuarioLocal.nombre;
        document.getElementById("nombre").value = usuarioLocal.nombre;
        document.getElementById("genero").value = usuarioLocal.genero || "No especificado";
        document.getElementById("nombreUsuario").value = usuarioLocal.nombreUsuario;
        document.getElementById("email").value = usuarioLocal.email || usuarioLocal.correo;

        // Rellenar Foto: Si tiene foto guardada (Base64) la usa, si no, usa la de letras
        if (usuarioLocal.fotoPerfil) {
            document.getElementById("avatarImg").src = usuarioLocal.fotoPerfil;
        } else {
            document.getElementById("avatarImg").src = `https://ui-avatars.com/api/?name=${usuarioLocal.nombre}&background=random&size=128`;
        }

    } catch (error) {
        console.error("Error cargando datos:", error);
    }
}

async function guardarCambios(e) {
    e.preventDefault();

    const usuarioLocal = JSON.parse(localStorage.getItem("usuario"));

    // 1. Recoger valores del formulario
    const nuevoNombre = document.getElementById("nombre").value;
    const nuevoNick = document.getElementById("nombreUsuario").value;
    const nuevoEmail = document.getElementById("email").value;

    // 2. Procesar la imagen
    const fileInput = document.getElementById("fileInput");
    let fotoFinal = usuarioLocal.fotoPerfil; // Por defecto mantenemos la que tenía

    if (fileInput.files.length > 0) {
        // Si subió una nueva, la convertimos
        fotoFinal = await convertBase64(fileInput.files[0]);
    }

    // 3. Objeto para enviar al Backend
    const datosActualizar = {
        nombre: nuevoNombre,
        nombreUsuario: nuevoNick,
        email: nuevoEmail,
        fotoPerfil: fotoFinal // Enviamos el texto largo de la imagen
    };

    try {
        const response = await fetch(`/api/usuarios/actualizar/${usuarioLocal.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosActualizar)
        });

        if (response.ok) {
            const usuarioActualizado = await response.json();

            // IMPORTANTE: Actualizar localStorage con la respuesta del servidor
            // para que el Muro vea la foto nueva inmediatamente
            localStorage.setItem("usuario", JSON.stringify(usuarioActualizado));

            alert("¡Perfil actualizado correctamente!");
        } else {
            const msg = await response.text();
            alert("Error al guardar: " + msg);
        }
    } catch (error) {
        console.error(error);
        alert("Error de conexión con el servidor.");
    }
}