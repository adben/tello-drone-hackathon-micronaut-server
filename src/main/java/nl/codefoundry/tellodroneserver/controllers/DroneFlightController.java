package nl.codefoundry.tellodroneserver.controllers;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;
import nl.codefoundry.tellodroneserver.services.DroneFlightService;

@Controller("api/drone/flight")
public class DroneFlightController {
    private final DroneFlightService droneFlightService;

    @Inject
    public DroneFlightController(DroneFlightService droneFlightService) {
        this.droneFlightService = droneFlightService;
    }

    @Get("takeoff")
    public void takeoff() {
        this.droneFlightService.takeoff();
    }

    @Get("land")
    public void land() {
        this.droneFlightService.land();
    }

    @Get("emergency-stop")
    public void emergencyStop() {
        this.droneFlightService.emergencyStop();
    }

    @Get("move-forward/{millimeters}")
    public void moveForward(int millimeters) {
        this.droneFlightService.move(MovementDirection.FORWARD, millimeters);
    }

    @Get("move-backward/{millimeters}")
    public void moveBackward(int millimeters) {
        this.droneFlightService.move(MovementDirection.BACKWARD, millimeters);
    }

    @Get("move-left/{millimeters}")
    public void moveLeft(int millimeters) {
        this.droneFlightService.move(MovementDirection.LEFT, millimeters);
    }

    @Get("move-right/{millimeters}")
    public void moveRight(int millimeters) {
        this.droneFlightService.move(MovementDirection.RIGHT, millimeters);
    }

    @Get("move-up/{millimeters}")
    public void moveUp(int millimeters) {
        this.droneFlightService.move(MovementDirection.UP, millimeters);
    }

    @Get("move-down/{millimeters}")
    public void moveDown(int millimeters) {
        this.droneFlightService.move(MovementDirection.DOWN, millimeters);
    }

    @Get("turn-left/{degrees}")
    public void turnLeft(int degrees) {
        this.droneFlightService.turn(TurnDirection.LEFT, degrees);
    }

    @Get("turn-right/{degrees}")
    public void turnRight(int degrees) {
        this.droneFlightService.turn(TurnDirection.RIGHT, degrees);
    }

    @Get("balloons")
    public void balloons() {
        this.droneFlightService.takeoff();
        this.droneFlightService.move(MovementDirection.UP, 160);
        this.droneFlightService.land();
    }
}
