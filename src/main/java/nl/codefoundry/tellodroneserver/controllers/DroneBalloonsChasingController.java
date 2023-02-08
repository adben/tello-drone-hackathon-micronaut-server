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
import nl.codefoundry.tellodroneserver.services.ImageRecognitionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.IntStream;

@Controller("api/drone/chasing")
public class DroneBalloonsChasingController {

    private static final Logger LOG = LoggerFactory.getLogger(DroneBalloonsChasingController.class);

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
            droneFlightService.move(MovementDirection.UP, 120);
            rotateAndFwd();
            this.droneFlightService.land();
        } catch (Exception e) {
            throw new DroneException(e.getMessage());
        } finally {
            this.droneFlightService.emergencyStop();
        }
    }

    private void rotateAndFwd() {
        IntStream.range(1, 20).forEachOrdered(range -> {
            this.droneVideoService.getLastVideoFrame()
                    .ifPresentOrElse(image -> {
                        if (ImageRecognitionUtil.isBalloonInImage(image)) {
                            droneFlightService.move(MovementDirection.FORWARD, 50);
                            rotateAndFwd();
                        }
                    }, () -> droneFlightService.turn(TurnDirection.LEFT, 18));
        });

    }

}
