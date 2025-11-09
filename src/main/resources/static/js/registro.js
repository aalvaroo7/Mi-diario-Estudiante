const registroContainer = document.querySelector(".login-container");
const registroForm = document.getElementById("registroForm");
const registroButton = registroForm.querySelector("button");
const registroFields = registroForm.querySelectorAll("input, select");

document.body.classList.add("page-loaded");

registroFields.forEach(field => {
  const toggleFilled = () => {
    if (field.value.trim()) {
      field.classList.add("has-value");
    } else {
      field.classList.remove("has-value");
    }
  };

  toggleFilled();
  field.addEventListener("focus", () => field.classList.add("is-focused"));
  field.addEventListener("blur", () => field.classList.remove("is-focused"));
  field.addEventListener("input", toggleFilled);
});

registroButton.addEventListener("click", createRipple);

registroForm.addEventListener("submit", function (e) {
  e.preventDefault();

  const usuario = {
    nombre: document.getElementById("nombre").value.trim(),
    apellidos: document.getElementById("apellidos").value.trim(),
    nombreUsuario: document.getElementById("nombreUsuario").value.trim(),
    genero: document.getElementById("genero").value,
    correo: document.getElementById("correo").value.trim(),
    password: document.getElementById("password").value,
    rol: "usuario"
  };

  fetch("http://localhost:8080/api/usuarios/registro", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(usuario)
  })
    .then(response => {
      if (!response.ok) {
        throw new Error("No se pudo registrar el usuario");
      }
      return response.text();
    })
    .then(data => {
      alert(data);
      if (data.toLowerCase().includes("correctamente")) {
        registroContainer.classList.add("success");
        setTimeout(() => {
          window.location.href = "/html/login.html";
        }, 650);
      } else {
        triggerShake();
      }
    })
    .catch(error => {
      console.error("Error al registrar:", error);
      triggerShake();
      alert("Ocurrió un error al registrar el usuario.");
    });
});

function triggerShake() {
  registroContainer.classList.remove("success");
  registroContainer.classList.remove("shake");
  void registroContainer.offsetWidth;
  registroContainer.classList.add("shake");
  registroContainer.addEventListener("animationend", () => registroContainer.classList.remove("shake"), { once: true });
}

function createRipple(event) {
  const button = event.currentTarget;
  const rect = button.getBoundingClientRect();
  const size = Math.max(rect.width, rect.height);
  const ripple = document.createElement("span");
  ripple.classList.add("ripple");
  ripple.style.width = ripple.style.height = `${size}px`;
  ripple.style.left = `${event.clientX - rect.left}px`;
  ripple.style.top = `${event.clientY - rect.top}px`;

  button.appendChild(ripple);
  ripple.addEventListener("animationend", () => ripple.remove());
}
