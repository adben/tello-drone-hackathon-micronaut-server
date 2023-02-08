package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.subjects.PublishSubject;
import nl.codefoundry.tellodroneserver.services.DroneService;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

@ServerWebSocket("/ws/drone/is-connected")
@Controller("api/drone")
public class DroneController {
    private final Map<String, PublishSubject<Boolean>> sessionClosedSubjects = new HashMap<>();

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @Get("connect")
    public String connect() {
        this.droneService.connect();

        return "connected";
    }

    @Get("disconnect")
    public String disconnect() {
        this.droneService.disconnect();

        return "disconnected";
    }

    @Get("is-connected")
    public boolean isConnected() {
        return this.droneService.isConnected();
    }

    @OnOpen
    public Publisher<Boolean> onOpen(WebSocketSession session) {
        var closeSubject = PublishSubject.<Boolean>create();
        sessionClosedSubjects.put(session.getId(), closeSubject);

        return droneService.isConnected$()
            .takeUntil(closeSubject.toFlowable(BackpressureStrategy.LATEST))
            .flatMap((isConnected) -> session.send(isConnected));
    }

    @OnClose
    public void onClose(WebSocketSession session) {
        var closeSubject = sessionClosedSubjects.get(session.getId());
        if (closeSubject != null) {
            sessionClosedSubjects.remove(session.getId());
            closeSubject.onNext(true);
            closeSubject.onComplete();
        }
    }

    @OnMessage
    public void onMessage(String rawMessage) {
        // We don't care about the incoming messages, so we just ignore them.
    }
}
