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
import jakarta.inject.Inject;
import me.friwi.tello4j.api.state.TelloDroneState;
import nl.codefoundry.tellodroneserver.services.DroneStateService;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;

@ServerWebSocket("/ws/drone/state")
@Controller("api/drone/state")
public class DroneStateController {
    private final Map<String, PublishSubject<Boolean>> sessionClosedSubjects = new HashMap<>();

    private final DroneStateService droneStateService;

    @Inject
    public DroneStateController(DroneStateService droneStateService) {
        this.droneStateService = droneStateService;
    }

    @Get("battery")
    public int getBatteryLevel() {
        return this.droneStateService.getBatteryLevel();
    }

    @OnOpen
    public Publisher<TelloDroneState> onOpen(WebSocketSession session) {
        var closeSubject = PublishSubject.<Boolean>create();
        sessionClosedSubjects.put(session.getId(), closeSubject);

        return droneStateService.droneState$()
            .takeUntil(closeSubject.toFlowable(BackpressureStrategy.LATEST))
            .flatMap((droneState) -> session.send(droneState));
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
