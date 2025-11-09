const loginContainer = document.querySelector(".login-container");
const loginForm = document.getElementById("loginForm");
const submitButton = loginForm.querySelector("button");
const interactiveFields = loginForm.querySelectorAll("input, select");

document.body.classList.add("page-loaded");

interactiveFields.forEach(field => {
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

submitButton.addEventListener("click", createRipple);

loginForm.addEventListener("submit", function (e) {
  e.preventDefault();

  const nombreUsuario = document.getElementById("username").value.trim();
  const password = document.getElementById("password").value;

  fetch(`http://localhost:8080/api/usuarios/login?nombreUsuario=${encodeURIComponent(nombreUsuario)}&password=${encodeURIComponent(password)}`, {
    method: "POST"
  })
    .then(response => {
      if (!response.ok) {
        throw new Error("Error al iniciar sesión");
      }
      return response.json();
    })
    .then(usuario => {
      if (usuario && usuario.nombreUsuario) {
        localStorage.setItem("usuarioActivo", JSON.stringify(usuario));
        loginContainer.classList.add("success");
        setTimeout(() => {
          window.location.href = "/html/muro.html";
        }, 650);
      } else {
        triggerShake();
        alert("Usuario o contraseña incorrectos.");
      }
    })
    .catch(error => {
      console.error("Error:", error);
      triggerShake();
      alert("No se pudo iniciar sesión.");
    });
});

function triggerShake() {
  loginContainer.classList.remove("success");
  loginContainer.classList.remove("shake");
  void loginContainer.offsetWidth;
  loginContainer.classList.add("shake");
  loginContainer.addEventListener("animationend", () => loginContainer.classList.remove("shake"), { once: true });
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
