package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import jakarta.inject.Inject;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;
import nl.codefoundry.tellodroneserver.exceptions.DroneException;
import nl.codefoundry.tellodroneserver.services.DroneFlightService;
import nl.codefoundry.tellodroneserver.services.DroneService;
import nl.codefoundry.tellodroneserver.services.DroneVideoService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;

@Controller("api/drone/chasing")
public class DroneBalloonsChasingController {

    private final DroneVideoService droneVideoService;
    private final DroneService droneService;
    private final DroneFlightService droneFlightService;
    private final byte[] defaultVideoFrameOutput;

    @Inject
    public DroneBalloonsChasingController(DroneVideoService droneVideoService,
                                          ResourceLoader resourceLoader, DroneService droneService, DroneFlightService droneFlightService) {
        this.droneVideoService = droneVideoService;
        this.droneService = droneService;
        this.droneFlightService = droneFlightService;

        try {
            defaultVideoFrameOutput = resourceLoader.getResourceAsStream("no-video-output.jpg")
                    .get().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default video frame output", e);
        }
    }

    @Get("balloons")
    public void balloons() {
        this.droneService.connect();
        this.droneFlightService.takeoff();
        this.droneFlightService.move(MovementDirection.UP, 120);
        IntStream.range(1, 20)
                .forEachOrdered(range -> {
                    droneFlightService.turn(TurnDirection.LEFT, range * 18);
                    this.getLastVideoFrame();
                });
        this.droneFlightService.land();
        this.droneService.disconnect();
    }

    @Get("foto")
    @Produces(MediaType.IMAGE_JPEG)
    public byte[] getLastVideoFrame() {
        final var lastFrame = droneVideoService.getLastVideoFrame();
        return lastFrame.map(DroneBalloonsChasingController::bufferedImageToJpgByteArray)
                .orElse(defaultVideoFrameOutput);

    }

    private static byte[] bufferedImageToJpgByteArray(BufferedImage image) {
        final var out = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            throw new DroneException("Failed to convert BufferedImage to JPG byte array", e);
        }

        return out.toByteArray();
    }


}
