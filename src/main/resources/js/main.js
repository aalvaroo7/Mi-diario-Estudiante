const API_BASE_URL = "http://localhost:8080/api";
const STORAGE_KEY = "miDiario.usuario";

function getCurrentUser() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY));
    } catch (error) {
        console.error("No se pudo leer el usuario almacenado", error);
        return null;
    }
}

function setCurrentUser(user) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
}

function clearCurrentUser() {
    localStorage.removeItem(STORAGE_KEY);
}

function requireAuth() {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }
    return user;
}

function formatDate(isoString) {
    if (!isoString) {
        return "";
    }
    const date = new Date(isoString);
    return date.toLocaleString("es-ES", {
        day: "2-digit",
        month: "short",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function updateNavigation() {
    const user = getCurrentUser();
    const logoutButton = document.getElementById("logoutButton");
    const authLinks = document.querySelectorAll("[data-auth='required']");

    if (logoutButton) {
        logoutButton.hidden = !user;
        if (user) {
            logoutButton.addEventListener("click", () => {
                clearCurrentUser();
                window.location.href = "index.html";
            }, { once: true });
        }
    }

    authLinks.forEach(link => {
        if (!user) {
            link.addEventListener("click", event => {
                event.preventDefault();
                window.location.href = "login.html";
            });
        }
    });
}

window.API_BASE_URL = API_BASE_URL;
window.getCurrentUser = getCurrentUser;
window.setCurrentUser = setCurrentUser;
window.clearCurrentUser = clearCurrentUser;
window.requireAuth = requireAuth;
window.formatDate = formatDate;

document.addEventListener("DOMContentLoaded", updateNavigation);
