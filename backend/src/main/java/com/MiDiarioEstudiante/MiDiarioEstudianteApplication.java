package com.MiDiarioEstudiante;

import com.MiDiarioEstudiante.http.ApiHandler;
import com.MiDiarioEstudiante.repository.ComentarioRepository;
import com.MiDiarioEstudiante.repository.NotificacionRepository;
import com.MiDiarioEstudiante.repository.PublicacionRepository;
import com.MiDiarioEstudiante.repository.ReaccionRepository;
import com.MiDiarioEstudiante.repository.UsuarioRepository;
import com.MiDiarioEstudiante.service.ComentarioService;
import com.MiDiarioEstudiante.service.NotificacionService;
import com.MiDiarioEstudiante.service.PublicacionService;
import com.MiDiarioEstudiante.service.ReaccionService;
import com.MiDiarioEstudiante.service.UsuarioService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class MiDiarioEstudianteApplication {
    private static final Map<String, String> CONTENT_TYPES = createContentTypes();

    public static void main(String[] args) throws IOException {
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        NotificacionRepository notificacionRepository = new NotificacionRepository();
        PublicacionRepository publicacionRepository = new PublicacionRepository();
        ComentarioRepository comentarioRepository = new ComentarioRepository();
        ReaccionRepository reaccionRepository = new ReaccionRepository();

        UsuarioService usuarioService = new UsuarioService(usuarioRepository);
        NotificacionService notificacionService = new NotificacionService(notificacionRepository, usuarioService);
        PublicacionService publicacionService = new PublicacionService(publicacionRepository, usuarioService);
        ComentarioService comentarioService = new ComentarioService(comentarioRepository, publicacionService, usuarioService, notificacionService);
        ReaccionService reaccionService = new ReaccionService(reaccionRepository, publicacionService, usuarioService, notificacionService);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        ApiHandler apiHandler = new ApiHandler(usuarioService, publicacionService, comentarioService, reaccionService, notificacionService);
        server.createContext("/api", apiHandler);

        Path frontendRoot = Path.of("frontend").toAbsolutePath().normalize();
        server.createContext("/", exchange -> handleStatic(exchange, frontendRoot));

        server.start();
        System.out.println("Servidor iniciado en http://localhost:8080");
    }

    private static void handleStatic(HttpExchange exchange, Path root) throws IOException {
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"GET".equalsIgnoreCase(method)) {
            sendResponse(exchange, 405, "Método no permitido", "text/plain; charset=utf-8");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            path = "/index.html";
        }
        Path requested = root.resolve(path.substring(1)).normalize();
        if (!requested.startsWith(root) || Files.isDirectory(requested) || !Files.exists(requested)) {
            sendResponse(exchange, 404, "Recurso no encontrado", "text/plain; charset=utf-8");
            return;
        }

        String contentType = detectContentType(requested);
        byte[] bytes = Files.readAllBytes(requested);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static String detectContentType(Path file) {
        String filename = file.getFileName().toString().toLowerCase();
        for (Map.Entry<String, String> entry : CONTENT_TYPES.entrySet()) {
            if (filename.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        try {
            String probe = Files.probeContentType(file);
            if (probe != null) {
                return probe;
            }
        } catch (IOException ignored) {
        }
        return "application/octet-stream";
    }

    private static Map<String, String> createContentTypes() {
        Map<String, String> map = new HashMap<>();
        map.put(".html", "text/html; charset=utf-8");
        map.put(".css", "text/css; charset=utf-8");
        map.put(".js", "application/javascript; charset=utf-8");
        map.put(".json", "application/json; charset=utf-8");
        map.put(".png", "image/png");
        map.put(".jpg", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".svg", "image/svg+xml");
        return map;
    }
}
