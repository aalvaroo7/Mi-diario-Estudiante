package com.miDiario.blog;

import com.miDiario.blog.dto.LoginDTO;
import com.miDiario.blog.dto.RegistroDTO;
import com.miDiario.blog.dto.UsuarioDTO;
import com.miDiario.blog.model.Publicacion;
import com.miDiario.blog.model.Usuario;
import com.miDiario.blog.service.DataStore;
import com.miDiario.blog.service.PublicacionService;
import com.miDiario.blog.service.UsuarioService;
import com.miDiario.blog.util.JsonUtils;
import com.miDiario.blog.util.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainApplication {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) throws IOException {
        DataStore dataStore = new DataStore();
        SessionManager sessionManager = new SessionManager();
        UsuarioService usuarioService = new UsuarioService(dataStore, sessionManager);
        PublicacionService publicacionService = new PublicacionService(dataStore);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Ruta raíz → login
        server.createContext("/", exchange -> redirect(exchange, "/html/login.html"));

        // Archivos estáticos
        server.createContext("/html", new StaticHandler("/static/html"));
        server.createContext("/js", new StaticHandler("/static/js"));
        server.createContext("/css", new StaticHandler("/static/css"));

        // API Auth
        server.createContext("/auth/registro", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Método no soportado");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> json = JsonUtils.parseSimpleJson(body);
            RegistroDTO dto = new RegistroDTO();
            dto.setNombre(json.get("nombre"));
            dto.setApellidos(json.get("apellidos"));
            dto.setNombreUsuario(json.getOrDefault("nombreUsuario", json.get("usuario")));
            dto.setGenero(json.get("genero"));
            dto.setCorreo(json.getOrDefault("correo", json.get("email")));
            dto.setPassword(json.get("password"));

            String resultado = usuarioService.registrar(dto);
            if (!"Usuario registrado correctamente".equals(resultado)) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_REQUEST, resultado);
                return;
            }
            sendPlain(exchange, HttpURLConnection.HTTP_OK, resultado);
        });

        server.createContext("/auth/login", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Método no soportado");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> json = JsonUtils.parseSimpleJson(body);
            LoginDTO dto = new LoginDTO();
            dto.setIdentificador(json.getOrDefault("identificador", json.get("email")));
            dto.setPassword(json.get("password"));

            Map<String, String> headers = new HashMap<>();
            Optional<UsuarioService.LoginResult> login = usuarioService.login(dto, headers);
            if (login.isEmpty()) {
                sendPlain(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "Credenciales incorrectas o usuario inactivo");
                return;
            }
            headers.forEach((k, v) -> exchange.getResponseHeaders().add(k, v));

            UsuarioDTO usuarioDTO = login.get().usuario();
            String jsonResponse = "{" +
                    "\"id\":" + usuarioDTO.getId() + "," +
                    "\"nombre\":" + JsonUtils.quote(usuarioDTO.getNombre()) + "," +
                    "\"apellidos\":" + JsonUtils.quote(usuarioDTO.getApellidos()) + "," +
                    "\"nombreUsuario\":" + JsonUtils.quote(usuarioDTO.getNombreUsuario()) + "," +
                    "\"correo\":" + JsonUtils.quote(usuarioDTO.getCorreo()) + "," +
                    "\"email\":" + JsonUtils.quote(usuarioDTO.getCorreo()) + "," +
                    "\"genero\":" + JsonUtils.quote(usuarioDTO.getGenero()) + "," +
                    "\"rol\":" + JsonUtils.quote(usuarioDTO.getRol()) + "," +
                    "\"activo\":" + usuarioDTO.isActivo() + "}";

            sendJson(exchange, HttpURLConnection.HTTP_OK, jsonResponse);
        });

        server.createContext("/auth/logout", exchange -> {
            String sessionId = readSessionCookie(exchange.getRequestHeaders().getFirst("Cookie"));
            usuarioService.logout(sessionId);
            sendPlain(exchange, HttpURLConnection.HTTP_OK, "Logout correcto");
        });

        // Publicaciones
        server.createContext("/api/publicaciones/crear", exchange -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Método no soportado");
                return;
            }
            Optional<Usuario> usuarioOpt = usuarioDesdeCookie(
                    exchange.getRequestHeaders().getFirst("Cookie"),
                    usuarioService);
            if (usuarioOpt.isEmpty()) {
                sendPlain(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "No autenticado");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> json = JsonUtils.parseSimpleJson(body);
            String contenido = json.get("contenido");
            Optional<Publicacion> creada = publicacionService.crear(contenido, usuarioOpt.get());
            if (creada.isEmpty()) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "No se pudo crear la publicación");
                return;
            }
            sendPlain(exchange, HttpURLConnection.HTTP_OK, "Publicación creada");
        });

        server.createContext("/api/publicaciones/todas", exchange -> {
            Optional<Usuario> usuarioOpt = usuarioDesdeCookie(
                    exchange.getRequestHeaders().getFirst("Cookie"),
                    usuarioService);
            if (usuarioOpt.isEmpty()) {
                sendPlain(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "No autenticado");
                return;
            }
            List<Publicacion> publicaciones = publicacionService.todasOrdenadas();
            String json = publicaciones.stream()
                    .map(MainApplication::toJson)
                    .reduce((a, b) -> a + "," + b)
                    .map(s -> "[" + s + "]")
                    .orElse("[]");
            sendJson(exchange, HttpURLConnection.HTTP_OK, json);
        });

        server.createContext("/api/publicaciones/eliminar", exchange -> {
            if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_METHOD, "Método no soportado");
                return;
            }
            Optional<Usuario> usuarioOpt = usuarioDesdeCookie(exchange.getRequestHeaders().getFirst("Cookie"), usuarioService);
            if (usuarioOpt.isEmpty()) {
                sendPlain(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "No autenticado");
                return;
            }
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 5) {
                sendPlain(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "ID no proporcionado");
                return;
            }
            long id = Long.parseLong(parts[4]);
            boolean eliminado = publicacionService.eliminar(id, usuarioOpt.get());
            if (!eliminado) {
                sendPlain(exchange, HttpURLConnection.HTTP_FORBIDDEN, "No se pudo eliminar la publicación");
                return;
            }
            sendPlain(exchange, HttpURLConnection.HTTP_OK, "Publicación eliminada");
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado en http://localhost:8080 (usuario demo: demo@correo.com / demo123)");
    }

    private static Optional<Usuario> usuarioDesdeCookie(String cookieHeader, UsuarioService usuarioService) {
        String sessionId = readSessionCookie(cookieHeader);
        if (sessionId == null) return Optional.empty();
        return usuarioService.fromSession(sessionId);
    }

    private static String readSessionCookie(String cookieHeader) {
        if (cookieHeader == null) return null;
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String trimmed = cookie.trim();
            if (trimmed.startsWith("SESSIONID=")) {
                return trimmed.substring("SESSIONID=".length());
            }
        }
        return null;
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
        exchange.close();
    }

    private static void sendPlain(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String toJson(Publicacion p) {
        String autorJson = "{\"nombreUsuario\":" + JsonUtils.quote(p.getAutor().getNombreUsuario()) + "}";
        return "{" +
                "\"id\":" + p.getId() + "," +
                "\"contenido\":" + JsonUtils.quote(p.getContenido()) + "," +
                "\"fechaPublicacion\":" + JsonUtils.quote(p.getFechaPublicacion().format(DATE_FORMAT)) + "," +
                "\"autor\":" + autorJson + "}";
    }

    private static class StaticHandler implements HttpHandler {
        private final String resourceRoot;

        StaticHandler(String resourceRoot) {
            this.resourceRoot = resourceRoot;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String resourcePath = resourceRoot + path.replaceFirst("/[^/]+", "");
            byte[] bytes = readResource(resourcePath);
            if (bytes == null) {
                sendPlain(exchange, HttpURLConnection.HTTP_NOT_FOUND, "Recurso no encontrado");
                return;
            }
            String contentType = URLConnection.guessContentTypeFromName(path);
            if (contentType == null) {
                contentType = "text/plain";
            }
            if (contentType.startsWith("text/")) {
                contentType += "; charset=utf-8";
            }
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private byte[] readResource(String resourcePath) throws IOException {
            InputStream is = MainApplication.class.getResourceAsStream(resourcePath);
            if (is != null) {
                return is.readAllBytes();
            }
            java.nio.file.Path fromFs = java.nio.file.Paths.get("src/main/resources" + resourcePath);
            if (java.nio.file.Files.exists(fromFs)) {
                return java.nio.file.Files.readAllBytes(fromFs);
            }
            return null;
        }
    }
}
