package com.MiDiarioEstudiante.http;

import com.MiDiarioEstudiante.dto.UsuarioResponse;
import com.MiDiarioEstudiante.model.Comentario;
import com.MiDiarioEstudiante.model.Notificacion;
import com.MiDiarioEstudiante.model.Publicacion;
import com.MiDiarioEstudiante.model.Reaccion;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.model.enums.TipoReaccion;
import com.MiDiarioEstudiante.service.ComentarioService;
import com.MiDiarioEstudiante.service.NotificacionService;
import com.MiDiarioEstudiante.service.PublicacionService;
import com.MiDiarioEstudiante.service.ReaccionService;
import com.MiDiarioEstudiante.service.UsuarioService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ApiHandler implements HttpHandler {
    private final UsuarioService usuarioService;
    private final PublicacionService publicacionService;
    private final ComentarioService comentarioService;
    private final ReaccionService reaccionService;
    private final NotificacionService notificacionService;

    public ApiHandler(UsuarioService usuarioService,
                      PublicacionService publicacionService,
                      ComentarioService comentarioService,
                      ReaccionService reaccionService,
                      NotificacionService notificacionService) {
        this.usuarioService = usuarioService;
        this.publicacionService = publicacionService;
        this.comentarioService = comentarioService;
        this.reaccionService = reaccionService;
        this.notificacionService = notificacionService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCommonHeaders(exchange);
        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            if (path.equals("/api/usuarios") && "GET".equalsIgnoreCase(method)) {
                handleListUsuarios(exchange);
            } else if (path.equals("/api/usuarios") && "POST".equalsIgnoreCase(method)) {
                handleCreateUsuario(exchange);
            } else if (path.equals("/api/usuarios/login") && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
            } else if (path.startsWith("/api/usuarios/") && path.split("/").length == 4) {
                handleUsuarioById(exchange, method);
            } else if (path.equals("/api/publicaciones") && "GET".equalsIgnoreCase(method)) {
                handleListPublicaciones(exchange);
            } else if (path.equals("/api/publicaciones") && "POST".equalsIgnoreCase(method)) {
                handleCreatePublicacion(exchange);
            } else if (path.startsWith("/api/comentarios/publicacion/") && "GET".equalsIgnoreCase(method)) {
                handleComentariosPorPublicacion(exchange);
            } else if (path.equals("/api/comentarios") && "POST".equalsIgnoreCase(method)) {
                handleCreateComentario(exchange);
            } else if (path.startsWith("/api/reacciones/publicacion/") && path.endsWith("/resumen") && "GET".equalsIgnoreCase(method)) {
                handleResumenReacciones(exchange);
            } else if (path.startsWith("/api/reacciones/publicacion/") && "GET".equalsIgnoreCase(method)) {
                handleReaccionesPorPublicacion(exchange);
            } else if (path.equals("/api/reacciones") && "POST".equalsIgnoreCase(method)) {
                handleCreateReaccion(exchange);
            } else if (path.startsWith("/api/notificaciones/usuario/") && "GET".equalsIgnoreCase(method)) {
                handleNotificacionesPorUsuario(exchange);
            } else if (path.startsWith("/api/notificaciones/") && path.endsWith("/leida") && "PATCH".equalsIgnoreCase(method)) {
                handleMarcarNotificacion(exchange);
            } else {
                sendJson(exchange, 404, JsonUtils.toJsonMessage("Ruta no encontrada"));
            }
        } catch (HttpException httpException) {
            sendJson(exchange, httpException.getStatusCode(), JsonUtils.toJsonMessage(httpException.getMessage()));
        } catch (Exception exception) {
            exception.printStackTrace();
            sendJson(exchange, 500, JsonUtils.toJsonMessage("Error interno del servidor"));
        }
    }

    private void handleListUsuarios(HttpExchange exchange) throws IOException {
        List<Usuario> usuarios = usuarioService.obtenerTodos();
        sendJson(exchange, 200, JsonUtils.toJsonUsuarios(usuarios));
    }

    private void handleCreateUsuario(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtils.parseObject(readBody(exchange));
        Usuario usuario = new Usuario();
        usuario.setNombre(body.get("nombre"));
        usuario.setEmail(body.get("email"));
        usuario.setPassword(body.get("password"));
        usuario.setBiografia(body.getOrDefault("biografia", ""));
        Usuario creado = usuarioService.registrar(usuario);
        sendJson(exchange, 201, JsonUtils.toJsonUsuario(creado));
    }

    private void handleUsuarioById(HttpExchange exchange, String method) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        Long id = parseId(parts[3]);
        if (id == null) {
            throw HttpException.badRequest("Identificador inválido");
        }
        if ("GET".equalsIgnoreCase(method)) {
            Usuario usuario = usuarioService.buscarPorId(id);
            sendJson(exchange, 200, JsonUtils.toJsonUsuario(usuario));
        } else if ("PUT".equalsIgnoreCase(method)) {
            Map<String, String> body = JsonUtils.parseObject(readBody(exchange));
            Usuario usuarioActualizado = new Usuario();
            usuarioActualizado.setNombre(body.get("nombre"));
            usuarioActualizado.setEmail(body.get("email"));
            usuarioActualizado.setPassword(body.get("password"));
            if (body.containsKey("biografia")) {
                usuarioActualizado.setBiografia(body.get("biografia"));
            }
            Usuario actualizado = usuarioService.actualizar(id, usuarioActualizado);
            sendJson(exchange, 200, JsonUtils.toJsonUsuario(actualizado));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            usuarioService.eliminar(id);
            sendJson(exchange, 204, "");
        } else {
            sendJson(exchange, 405, JsonUtils.toJsonMessage("Método no permitido"));
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtils.parseObject(readBody(exchange));
        Usuario usuario = usuarioService.autenticar(body.get("email"), body.get("password"));
        UsuarioResponse response = UsuarioResponse.from(usuario);
        sendJson(exchange, 200, JsonUtils.toJsonUsuario(response));
    }

    private void handleListPublicaciones(HttpExchange exchange) throws IOException {
        List<Publicacion> publicaciones = publicacionService.obtenerTodas();
        sendJson(exchange, 200, JsonUtils.toJsonPublicaciones(publicaciones));
    }

    private void handleCreatePublicacion(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtils.parseObject(readBody(exchange));
        Publicacion publicacion = new Publicacion();
        publicacion.setUsuarioId(parseLong(body.get("usuarioId")));
        publicacion.setContenido(body.get("contenido"));
        Publicacion creada = publicacionService.crear(publicacion);
        sendJson(exchange, 201, JsonUtils.toJsonPublicacion(creada));
    }

    private void handleComentariosPorPublicacion(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        Long publicacionId = parseId(parts[4]);
        if (publicacionId == null) {
            throw HttpException.badRequest("Identificador inválido");
        }
        List<Comentario> comentarios = comentarioService.obtenerPorPublicacion(publicacionId);
        sendJson(exchange, 200, JsonUtils.toJsonComentarios(comentarios));
    }

    private void handleCreateComentario(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtils.parseObject(readBody(exchange));
        Comentario comentario = new Comentario();
        comentario.setPublicacionId(parseLong(body.get("publicacionId")));
        comentario.setUsuarioId(parseLong(body.get("usuarioId")));
        comentario.setContenido(body.get("contenido"));
        Comentario creado = comentarioService.crear(comentario);
        sendJson(exchange, 201, JsonUtils.toJsonComentario(creado));
    }

    private void handleResumenReacciones(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        Long publicacionId = parseId(parts[4]);
        if (publicacionId == null) {
            throw HttpException.badRequest("Identificador inválido");
        }
        Map<TipoReaccion, Long> resumen = new EnumMap<>(TipoReaccion.class);
        for (TipoReaccion tipo : TipoReaccion.values()) {
            resumen.put(tipo, reaccionService.contarPorTipo(publicacionId, tipo));
        }
        sendJson(exchange, 200, JsonUtils.toJsonResumen(resumen));
    }

    private void handleReaccionesPorPublicacion(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        Long publicacionId = parseId(parts[4]);
        if (publicacionId == null) {
            throw HttpException.badRequest("Identificador inválido");
        }
        List<Reaccion> reacciones = reaccionService.obtenerPorPublicacion(publicacionId);
        sendJson(exchange, 200, JsonUtils.toJsonReacciones(reacciones));
    }

    private void handleCreateReaccion(HttpExchange exchange) throws IOException {
        Map<String, String> body = JsonUtils.parseObject(readBody(exchange));
        Reaccion reaccion = new Reaccion();
        reaccion.setPublicacionId(parseLong(body.get("publicacionId")));
        reaccion.setUsuarioId(parseLong(body.get("usuarioId")));
        reaccion.setTipo(parseTipoReaccion(body.get("tipo")));
        Reaccion guardada = reaccionService.guardar(reaccion);
        sendJson(exchange, 200, JsonUtils.toJsonReaccion(guardada));
    }

    private void handleNotificacionesPorUsuario(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        Long usuarioId = parseId(parts[4]);
        if (usuarioId == null) {
            throw HttpException.badRequest("Identificador inválido");
        }
        List<Notificacion> notificaciones = notificacionService.obtenerPorUsuario(usuarioId);
        sendJson(exchange, 200, JsonUtils.toJsonNotificaciones(notificaciones));
    }

    private void handleMarcarNotificacion(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        Long notificacionId = parseId(parts[3]);
        if (notificacionId == null) {
            throw HttpException.badRequest("Identificador inválido");
        }
        Notificacion notificacion = notificacionService.marcarComoLeida(notificacionId);
        sendJson(exchange, 200, JsonUtils.toJsonNotificacion(notificacion));
    }

    private TipoReaccion parseTipoReaccion(String value) {
        if (value == null || value.isBlank()) {
            return TipoReaccion.ME_GUSTA;
        }
        try {
            return TipoReaccion.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw HttpException.badRequest("Tipo de reacción inválido");
        }
    }

    private Long parseId(String value) {
        return parseLong(value);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            throw HttpException.badRequest("Formato numérico inválido");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private void addCommonHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
    }
}
