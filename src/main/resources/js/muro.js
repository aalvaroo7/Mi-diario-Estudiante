document.getElementById("logout").addEventListener("click", () => {
    localStorage.removeItem("usuario");
    window.location.href = "login.html";
});
const REACTION_OPTIONS = [
    { type: "ME_GUSTA", icon: "👍", label: "Me gusta" },
    { type: "ME_ENCANTA", icon: "❤️", label: "Me encanta" },
    { type: "ME_APOYA", icon: "🤝", label: "Me apoya" }
];

document.addEventListener("DOMContentLoaded", () => {
    const currentUser = requireAuth();
    if (!currentUser) {
        return;
    }

    const form = document.getElementById("postForm");
    const feedContainer = document.getElementById("feedContainer");
    const message = document.getElementById("postMessage");

    async function cargarFeed() {
        feedContainer.innerHTML = "";
        try {
            const [publicaciones, usuarios] = await Promise.all([
                fetch(`${API_BASE_URL}/publicaciones`).then(res => res.json()),
                fetch(`${API_BASE_URL}/usuarios`).then(res => res.json())
            ]);

            const usuariosMap = new Map(usuarios.map(usuario => [usuario.id, usuario]));

            if (!publicaciones.length) {
                feedContainer.innerHTML = `<p class="empty-state">Aún no hay publicaciones. ¡Sé el primero en compartir!</p>`;
                return;
            }

            for (const publicacion of publicaciones) {
                const elemento = await crearPublicacion(publicacion, usuariosMap, currentUser);
                feedContainer.appendChild(elemento);
            }
        } catch (error) {
            console.error(error);
            feedContainer.innerHTML = `<p class="empty-state">No pudimos cargar el muro. Intenta de nuevo más tarde.</p>`;
        }
    }

    form?.addEventListener("submit", async event => {
        event.preventDefault();
        message.hidden = true;
        message.classList.remove("error", "success");

        const contenido = form.postContent.value.trim();
        if (!contenido) {
            message.textContent = "Escribe algo antes de publicar";
            message.classList.add("error");
            message.hidden = false;
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/publicaciones`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ usuarioId: currentUser.id, contenido })
            });

            if (!response.ok) {
                throw new Error("No se pudo crear la publicación");
            }

            form.reset();
            message.textContent = "¡Publicación creada!";
            message.classList.add("success");
            message.hidden = false;
            await cargarFeed();
        } catch (error) {
            console.error(error);
            message.textContent = error.message || "Ocurrió un error al publicar";
            message.classList.add("error");
            message.hidden = false;
        }
    });

    cargarFeed();
});

async function crearPublicacion(publicacion, usuariosMap, currentUser) {
    const article = document.createElement("article");
    article.className = "post";

    const header = document.createElement("header");
    header.className = "post__header";
    const author = document.createElement("span");
    author.className = "post__author";
    const autor = usuariosMap.get(publicacion.usuarioId);
    author.textContent = autor ? autor.nombre : "Usuario";
    const date = document.createElement("time");
    date.className = "post__date";
    date.dateTime = publicacion.fechaCreacion;
    date.textContent = formatDate(publicacion.fechaCreacion);
    header.append(author, date);

    const content = document.createElement("p");
    content.textContent = publicacion.contenido;

    const actions = document.createElement("div");
    actions.className = "post__actions";

    const counters = document.createElement("span");
    counters.className = "post__counters";
    actions.appendChild(counters);

    const commentSection = document.createElement("div");
    commentSection.className = "post__comments";

    const commentList = document.createElement("div");
    commentList.className = "post__comments-list";
    commentSection.appendChild(commentList);

    const commentForm = document.createElement("form");
    commentForm.className = "comment-form";
    commentForm.innerHTML = `
        <textarea name="comentario" placeholder="Escribe un comentario" required></textarea>
        <button type="submit" class="btn">Comentar</button>
    `;
    commentSection.appendChild(commentForm);

    const reactionButtons = REACTION_OPTIONS.map(option => {
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = `${option.icon} ${option.label}`;
        button.addEventListener("click", async () => {
            try {
                await fetch(`${API_BASE_URL}/reacciones`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        publicacionId: publicacion.id,
                        usuarioId: currentUser.id,
                        tipo: option.type
                    })
                });
                await actualizarReacciones();
            } catch (error) {
                console.error(error);
                alert("No se pudo registrar la reacción");
            }
        });
        actions.appendChild(button);
        return { button, option };
    });

    commentForm.addEventListener("submit", async event => {
        event.preventDefault();
        const texto = commentForm.comentario.value.trim();
        if (!texto) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/comentarios`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    publicacionId: publicacion.id,
                    usuarioId: currentUser.id,
                    contenido: texto
                })
            });
            if (!response.ok) {
                throw new Error("No se pudo crear el comentario");
            }
            commentForm.reset();
            await actualizarComentarios();
        } catch (error) {
            console.error(error);
            alert(error.message || "No se pudo comentar");
        }
    });

    article.append(header, content, actions, commentSection);

    async function actualizarReacciones() {
        try {
            const [resumen, reacciones] = await Promise.all([
                fetch(`${API_BASE_URL}/reacciones/publicacion/${publicacion.id}/resumen`).then(res => res.json()),
                fetch(`${API_BASE_URL}/reacciones/publicacion/${publicacion.id}`).then(res => res.json())
            ]);
            const actual = reacciones.find(reaccion => reaccion.usuarioId === currentUser.id);
            reactionButtons.forEach(({ button, option }) => {
                const total = resumen[option.type] ?? 0;
                button.textContent = `${option.icon} ${total}`;
                button.classList.toggle("is-active", actual && actual.tipo === option.type);
            });
            const totalGeneral = Object.values(resumen).reduce((acc, value) => acc + Number(value), 0);
            counters.textContent = totalGeneral ? `${totalGeneral} reacciones` : "";
        } catch (error) {
            console.error(error);
        }
    }

    async function actualizarComentarios() {
        try {
            const comentarios = await fetch(`${API_BASE_URL}/comentarios/publicacion/${publicacion.id}`).then(res => res.json());
            commentList.innerHTML = "";
            if (!comentarios.length) {
                const empty = document.createElement("p");
                empty.className = "empty-state";
                empty.textContent = "Sé el primero en comentar";
                commentList.appendChild(empty);
                return;
            }
            for (const comentario of comentarios) {
                const nodo = crearComentario(comentario, usuariosMap);
                commentList.appendChild(nodo);
            }
        } catch (error) {
            console.error(error);
        }
    }

    await Promise.all([actualizarReacciones(), actualizarComentarios()]);

    return article;
}

function crearComentario(comentario, usuariosMap) {
    const template = document.getElementById("commentTemplate");
    const clone = template.content.firstElementChild.cloneNode(true);
    const autor = usuariosMap.get(comentario.usuarioId);
    clone.querySelector(".comment__author").textContent = autor ? autor.nombre : "Usuario";
    const timeEl = clone.querySelector(".comment__time");
    timeEl.dateTime = comentario.fechaCreacion;
    timeEl.textContent = formatDate(comentario.fechaCreacion);
    clone.querySelector(".comment__content").textContent = comentario.contenido;
    return clone;
}
