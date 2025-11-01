package com.MiDiarioEstudiante.http;

import com.MiDiarioEstudiante.dto.UsuarioResponse;
import com.MiDiarioEstudiante.model.Comentario;
import com.MiDiarioEstudiante.model.Notificacion;
import com.MiDiarioEstudiante.model.Publicacion;
import com.MiDiarioEstudiante.model.Reaccion;
import com.MiDiarioEstudiante.model.Usuario;
import com.MiDiarioEstudiante.model.enums.TipoReaccion;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) {
            return result;
        }
        String trimmed = json.trim();
        if (trimmed.isEmpty()) {
            return result;
        }
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        boolean inQuotes = false;
        boolean escaping = false;
        boolean readingKey = true;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (escaping) {
                (readingKey ? key : value).append(c);
                escaping = false;
                continue;
            }
            if (c == '\\') {
                escaping = true;
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && c == ':') {
                readingKey = false;
                continue;
            }
            if (!inQuotes && c == ',') {
                storePair(result, key, value);
                key.setLength(0);
                value.setLength(0);
                readingKey = true;
                continue;
            }
            (readingKey ? key : value).append(c);
        }
        storePair(result, key, value);
        return result;
    }

    private static void storePair(Map<String, String> result, StringBuilder keyBuilder, StringBuilder valueBuilder) {
        if (keyBuilder.length() == 0) {
            return;
        }
        String key = unquote(keyBuilder.toString().trim());
        String value = unquote(valueBuilder.toString().trim());
        result.put(key, value);
    }

    private static String unquote(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.replace("\\\"", "\"").replace("\\n", "\n");
    }

    public static String toJsonUsuario(Usuario usuario) {
        if (usuario == null) {
            return "null";
        }
        return toJsonUsuario(UsuarioResponse.from(usuario));
    }

    public static String toJsonUsuario(UsuarioResponse usuario) {
        if (usuario == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", number(usuario.getId()));
        appendField(sb, "nombre", quote(usuario.getNombre()));
        appendField(sb, "email", quote(usuario.getEmail()));
        appendField(sb, "biografia", quote(usuario.getBiografia()));
        appendField(sb, "fechaRegistro", quote(formatDate(usuario.getFechaRegistro())));
        sb.append('}');
        return sb.toString();
    }

    public static String toJsonUsuarios(List<Usuario> usuarios) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Usuario usuario : usuarios) {
            if (!first) {
                sb.append(',');
            }
            sb.append(toJsonUsuario(usuario));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toJsonPublicacion(Publicacion publicacion) {
        if (publicacion == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", number(publicacion.getId()));
        appendField(sb, "usuarioId", number(publicacion.getUsuarioId()));
        appendField(sb, "contenido", quote(publicacion.getContenido()));
        appendField(sb, "fechaCreacion", quote(formatDate(publicacion.getFechaCreacion())));
        appendField(sb, "visibilidad", quote(publicacion.getVisibilidad() != null ? publicacion.getVisibilidad().name() : null));
        sb.append('}');
        return sb.toString();
    }

    public static String toJsonPublicaciones(List<Publicacion> publicaciones) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Publicacion publicacion : publicaciones) {
            if (!first) {
                sb.append(',');
            }
            sb.append(toJsonPublicacion(publicacion));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toJsonComentario(Comentario comentario) {
        if (comentario == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", number(comentario.getId()));
        appendField(sb, "publicacionId", number(comentario.getPublicacionId()));
        appendField(sb, "usuarioId", number(comentario.getUsuarioId()));
        appendField(sb, "contenido", quote(comentario.getContenido()));
        appendField(sb, "fechaCreacion", quote(formatDate(comentario.getFechaCreacion())));
        sb.append('}');
        return sb.toString();
    }

    public static String toJsonComentarios(List<Comentario> comentarios) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Comentario comentario : comentarios) {
            if (!first) {
                sb.append(',');
            }
            sb.append(toJsonComentario(comentario));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toJsonReaccion(Reaccion reaccion) {
        if (reaccion == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", number(reaccion.getId()));
        appendField(sb, "publicacionId", number(reaccion.getPublicacionId()));
        appendField(sb, "usuarioId", number(reaccion.getUsuarioId()));
        appendField(sb, "tipo", quote(reaccion.getTipo() != null ? reaccion.getTipo().name() : null));
        appendField(sb, "fechaCreacion", quote(formatDate(reaccion.getFechaCreacion())));
        sb.append('}');
        return sb.toString();
    }

    public static String toJsonReacciones(List<Reaccion> reacciones) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Reaccion reaccion : reacciones) {
            if (!first) {
                sb.append(',');
            }
            sb.append(toJsonReaccion(reaccion));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toJsonResumen(Map<TipoReaccion, Long> resumen) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<TipoReaccion, Long> entry : resumen.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            appendField(sb, entry.getKey().name(), number(entry.getValue()));
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }

    public static String toJsonNotificacion(Notificacion notificacion) {
        if (notificacion == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", number(notificacion.getId()));
        appendField(sb, "usuarioId", number(notificacion.getUsuarioId()));
        appendField(sb, "mensaje", quote(notificacion.getMensaje()));
        appendField(sb, "tipo", quote(notificacion.getTipo() != null ? notificacion.getTipo().name() : null));
        appendField(sb, "leido", bool(notificacion.isLeido()));
        appendField(sb, "fechaCreacion", quote(formatDate(notificacion.getFechaCreacion())));
        sb.append('}');
        return sb.toString();
    }

    public static String toJsonNotificaciones(List<Notificacion> notificaciones) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Notificacion notificacion : notificaciones) {
            if (!first) {
                sb.append(',');
            }
            sb.append(toJsonNotificacion(notificacion));
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    public static String toJsonMessage(String message) {
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "message", quote(message));
        sb.append('}');
        return sb.toString();
    }

    private static void appendField(StringBuilder sb, String key, String value) {
        if (sb.length() > 1) {
            sb.append(',');
        }
        sb.append('"').append(escape(key)).append('"').append(':').append(value);
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return '"' + escape(value) + '"';
    }

    private static String number(Number value) {
        return value == null ? "null" : value.toString();
    }

    private static String bool(boolean value) {
        return value ? "true" : "false";
    }

    private static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
