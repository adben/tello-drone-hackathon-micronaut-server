package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import static java.lang.System.out;

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
            defaultVideoFrameOutput = resourceLoader.getResourceAsStream("no-video-output.jpg").get().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default video frame output", e);
        }
    }

    @Get("balloons")
    public void balloons() {
        this.droneService.connect();
        this.droneFlightService.takeoff();
        this.droneFlightService.move(MovementDirection.UP, 160);
        for (int range = 1; range < 10; range++) {
            droneFlightService.turn(TurnDirection.LEFT, range * 3);
            droneVideoService.getLastVideoFrame()
                    .map(DroneBalloonsChasingController::bufferedImageToJpgByteArray)
                    .map(byteOutStream -> {
                        String filename = UUID.randomUUID().toString() + ".jpg";
                        try (OutputStream outStream = new FileOutputStream(UUID.randomUUID().toString())) {
                            // writing bytes in to byte output stream
                            byteOutStream.writeTo(outStream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return filename;
                    })
                    .ifPresent(out::println);

        }
        this.droneFlightService.land();
        this.droneService.disconnect();
    }

    private static final ByteArrayOutputStream bufferedImageToJpgByteArray(BufferedImage image) {
        final var out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            throw new DroneException("Failed to convert BufferedImage to JPG byte array", e);
        }
        return out;
    }


}
