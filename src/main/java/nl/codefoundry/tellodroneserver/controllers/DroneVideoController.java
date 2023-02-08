package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.subjects.PublishSubject;
import jakarta.inject.Inject;
import nl.codefoundry.tellodroneserver.exceptions.DroneException;
import nl.codefoundry.tellodroneserver.services.DroneVideoService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerWebSocket("/ws/drone/video")
@Controller("api/drone/video")
public class DroneVideoController {
    private static final Logger LOG = LoggerFactory.getLogger(DroneVideoController.class);

    private final Map<String, PublishSubject<Boolean>> sessionClosedSubjects = new HashMap<>();

    private final DroneVideoService droneVideoService;
    private final byte[] defaultVideoFrameOutput;

    @Inject
    public DroneVideoController(DroneVideoService droneVideoService, ResourceLoader resourceLoader) {
        this.droneVideoService = droneVideoService;

        try {
            defaultVideoFrameOutput = resourceLoader.getResourceAsStream("no-video-output.jpg").get().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default video frame output", e);
        }
    }

    @Get("start")
    public String startVideoStream() {
        this.droneVideoService.startVideoStream();

        return "video stream started";
    }

    @Get("stop")
    public String stopVideoStream() {
        this.droneVideoService.stopVideoStream();

        return "video stream stopped";
    }

    @Get("frame")
    @Produces(MediaType.IMAGE_JPEG)
    public byte[] getLastVideoFrame() {
        final var lastFrame = droneVideoService.getLastVideoFrame();

        if (lastFrame.isEmpty()) {
            return defaultVideoFrameOutput;
        }

        return bufferedImageToJpgByteArray(lastFrame.get());
    }

    @OnOpen
    public Publisher<Object> onOpen(WebSocketSession session) {
        var closeSubject = PublishSubject.<Boolean>create();
        sessionClosedSubjects.put(session.getId(), closeSubject);

        return droneVideoService.getVideoStream()
            .map((videoFrame) -> videoFrame.isPresent()
                ? bufferedImageToJpgByteArray(videoFrame.get())
                : defaultVideoFrameOutput
            )
            .takeUntil(closeSubject.toFlowable(BackpressureStrategy.LATEST))
            .flatMap((videoFrameBytes) -> session.send(videoFrameBytes));
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

    private static final byte[] bufferedImageToJpgByteArray(BufferedImage image) {
        final var out = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            throw new DroneException("Failed to convert BufferedImage to JPG byte array", e);
        }

        return out.toByteArray();
    }
}
