document.addEventListener("DOMContentLoaded", () => {
    const currentUser = requireAuth();
    if (!currentUser) {
        return;
    }

    const nombreHeading = document.getElementById("perfilNombre");
    const emailParagraph = document.getElementById("perfilEmail");
    const form = document.getElementById("perfilForm");
    const nombreInput = document.getElementById("perfilNombreInput");
    const biografiaInput = document.getElementById("perfilBiografiaInput");
    const passwordInput = document.getElementById("perfilPasswordInput");
    const message = document.getElementById("perfilMessage");
    const notificacionesLista = document.getElementById("notificacionesLista");
    const notificacionesEmpty = document.getElementById("notificacionesEmpty");

    async function cargarUsuario() {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/${currentUser.id}`);
            if (!response.ok) {
                throw new Error("No se pudo obtener la información del usuario");
            }
            const usuario = await response.json();
            nombreHeading.textContent = usuario.nombre;
            emailParagraph.textContent = usuario.email;
            nombreInput.value = usuario.nombre;
            biografiaInput.value = usuario.biografia || "";
            setCurrentUser(usuario);
        } catch (error) {
            console.error(error);
        }
    }

    async function cargarNotificaciones() {
        try {
            const response = await fetch(`${API_BASE_URL}/notificaciones/usuario/${currentUser.id}`);
            if (!response.ok) {
                throw new Error("No se pudo obtener las notificaciones");
            }
            const notificaciones = await response.json();
            notificacionesLista.innerHTML = "";
            const hayNotificaciones = notificaciones.length > 0;
            notificacionesEmpty.hidden = hayNotificaciones;
            if (!hayNotificaciones) {
                return;
            }
            notificaciones.forEach(notificacion => {
                const item = document.createElement("li");
                if (notificacion.leido) {
                    item.classList.add("leida");
                }
                const contenido = document.createElement("span");
                contenido.textContent = `${notificacion.mensaje} · ${formatDate(notificacion.fechaCreacion)}`;
                item.appendChild(contenido);
                if (!notificacion.leido) {
                    const boton = document.createElement("button");
                    boton.type = "button";
                    boton.textContent = "Marcar como leída";
                    boton.addEventListener("click", async () => {
                        try {
                            await fetch(`${API_BASE_URL}/notificaciones/${notificacion.id}/leida`, {
                                method: "PATCH"
                            });
                            await cargarNotificaciones();
                        } catch (error) {
                            console.error(error);
                            alert("No se pudo actualizar la notificación");
                        }
                    });
                    item.appendChild(boton);
                }
                notificacionesLista.appendChild(item);
            });
        } catch (error) {
            console.error(error);
            notificacionesEmpty.hidden = false;
            notificacionesEmpty.textContent = "No pudimos cargar las notificaciones.";
        }
    }

    form.addEventListener("submit", async event => {
        event.preventDefault();
        message.hidden = true;
        message.classList.remove("error", "success");

        const payload = {
            nombre: nombreInput.value.trim(),
            biografia: biografiaInput.value.trim()
        };
        if (passwordInput.value.trim()) {
            payload.password = passwordInput.value;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/${currentUser.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                const errorBody = await response.json().catch(() => null);
                throw new Error(errorBody?.message || "No se pudo actualizar el perfil");
            }
            const usuarioActualizado = await response.json();
            passwordInput.value = "";
            message.textContent = "Perfil actualizado";
            message.classList.add("success");
            message.hidden = false;
            nombreHeading.textContent = usuarioActualizado.nombre;
            emailParagraph.textContent = usuarioActualizado.email;
            setCurrentUser(usuarioActualizado);
        } catch (error) {
            console.error(error);
            message.textContent = error.message || "No se pudo actualizar";
            message.classList.add("error");
            message.hidden = false;
        }
    });

    cargarUsuario();
    cargarNotificaciones();
});
