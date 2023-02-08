package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;
import nl.codefoundry.tellodroneserver.services.DroneFlightService;
import nl.codefoundry.tellodroneserver.services.DroneService;

import java.util.stream.IntStream;

@Controller("api/drone/chasing")
public class DroneBalloonsChasingController {

    private final DroneService droneService;
    private final DroneFlightService droneFlightService;

    @Inject
    public DroneBalloonsChasingController(ResourceLoader resourceLoader, DroneService droneService, DroneFlightService droneFlightService) {
        this.droneService = droneService;
        this.droneFlightService = droneFlightService;
    }

    @Get("balloons")
    public void balloons() {
        this.droneService.connect();
        this.droneFlightService.takeoff();
        try {
            this.droneFlightService.move(MovementDirection.UP, 100);
            rotate(3);
            droneFlightService.move(MovementDirection.FORWARD, 100);
            rotate(4);
            droneFlightService.move(MovementDirection.FORWARD, 100);
            rotate(5);
            droneFlightService.move(MovementDirection.FORWARD, 100);

        } catch (Exception e) {
            System.out.println(e);
            this.droneFlightService.emergencyStop();
        } finally {
            this.droneFlightService.land();
            this.droneService.disconnect();
        }
    }

    private void rotate(int times) {
        IntStream.range(1, times).forEachOrdered(range -> {
            droneFlightService.turn(TurnDirection.LEFT, 18);
        });
        //detect color
    }

}
