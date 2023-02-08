package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import me.friwi.tello4j.api.world.MovementDirection;
import nl.codefoundry.tellodroneserver.services.DroneFlightService;
import nl.codefoundry.tellodroneserver.services.DroneService;
import nl.codefoundry.tellodroneserver.services.DroneVideoService;

import java.io.IOException;

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
        this.droneFlightService.land();
    }
}
