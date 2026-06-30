package es.ucab.entrenos.modulos.notificacion.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    private final Map<String, SseEmitter> emisores = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SseService.class);

    public SseEmitter suscribir(String idUsuario) {
        SseEmitter emisor = new SseEmitter(Long.MAX_VALUE);
        emisores.put(idUsuario, emisor);
        emisor.onCompletion(() -> emisores.remove(idUsuario));
        emisor.onTimeout(() -> emisores.remove(idUsuario));
        emisor.onError(e -> emisores.remove(idUsuario));
        try {
            emisor.send(SseEmitter.event().name("conectado").data("{\"mensaje\":\"Conectado\"}"));
        } catch (IOException e) {
            emisores.remove(idUsuario);
        }
        return emisor;
    }

    public void enviar(String idUsuario, String evento, String datos) {
        SseEmitter emisor = emisores.get(idUsuario);
        if (emisor != null) {
            try {
                emisor.send(SseEmitter.event().name(evento).data(datos));
            } catch (IOException e) {
                emisores.remove(idUsuario);
                log.warn("SSE: error al enviar a {}: {}", idUsuario, e.getMessage());
            }
        }
    }

    public void enviarATodos(String evento, String datos) {
        emisores.forEach((id, emisor) -> enviar(id, evento, datos));
    }
}
