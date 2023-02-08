package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;
import nl.codefoundry.tellodroneserver.services.DroneFlightService;
import nl.codefoundry.tellodroneserver.services.DroneService;
import nl.codefoundry.tellodroneserver.services.DroneVideoService;
import nl.codefoundry.tellodroneserver.services.ImageRecognitionUtil;

import java.util.stream.IntStream;

@Controller("api/drone/chasing")
public class DroneBalloonsChasingController {

    private final DroneService droneService;
    private final DroneFlightService droneFlightService;
    private final DroneVideoService droneVideoService;

    @Inject
    public DroneBalloonsChasingController(ResourceLoader resourceLoader, DroneService droneService,
            DroneFlightService droneFlightService, DroneVideoService droneVideoService) {
        this.droneService = droneService;
        this.droneFlightService = droneFlightService;
        this.droneVideoService = droneVideoService;
    }

    @Get("balloons")
    public void balloons() {
        this.droneFlightService.takeoff();
        try {
            rotateAndFwd();
            rotateAndFwd();
            //todo repeat tot klaar
        } catch (Exception e) {
            System.out.println(e);
            this.droneFlightService.emergencyStop();
        } finally {
            this.droneFlightService.land();
        }
    }

    private void rotateAndFwd() {
        IntStream.range(1, 20).forEachOrdered(range -> {
            droneFlightService.turn(TurnDirection.LEFT, 18);
            // detect color
        });
        this.droneVideoService.getLastVideoFrame()
            .ifPresent(image -> {
                if (ImageRecognitionUtil.isBalloonInImage(image)) {
                    droneFlightService.move(MovementDirection.FORWARD, 50);
                }
            });
    }

}
