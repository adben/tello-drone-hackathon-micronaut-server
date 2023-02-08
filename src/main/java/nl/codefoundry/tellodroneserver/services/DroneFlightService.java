package nl.codefoundry.tellodroneserver.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.friwi.tello4j.api.drone.TelloDrone;
import me.friwi.tello4j.api.exception.TelloException;
import me.friwi.tello4j.api.world.MovementDirection;
import me.friwi.tello4j.api.world.TurnDirection;
import nl.codefoundry.tellodroneserver.exceptions.DroneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DroneFlightService {
    private static final Logger LOG = LoggerFactory.getLogger(DroneFlightService.class);

    private final DroneService droneService;

    @Inject
    public DroneFlightService(final DroneService droneService) {
        this.droneService = droneService;
    }

    public void takeoff() {
        performAction(
            TelloDrone::takeoff,
            "Takeoff"
        );
    }

    public void land() {
        performAction(
            TelloDrone::land,
            "Land"
        );
    }

    public void emergencyStop() {
        performAction(
            TelloDrone::emergency,
            "Emergency stop"
        );
    }

    public void turn(final TurnDirection direction, final int degrees) {
        performAction(
            drone -> drone.turn(direction, degrees),
            "Turn " + (direction == TurnDirection.LEFT ? "left" : "right") + " " + degrees + " degrees"
        );
    }

    public void move(final MovementDirection direction, final int millimeters) {
        String directionName;

        switch (direction) {
            case FORWARD -> directionName = "forward";
            case BACKWARD -> directionName = "backward";
            case LEFT -> directionName = "left";
            case RIGHT -> directionName = "right";
            case UP -> directionName = "up";
            case DOWN -> directionName = "down";
            default -> directionName = "(unknown direction)";
        }

        performAction(
            drone -> drone.moveDirection(direction, millimeters),
            "Move " + directionName + " " + millimeters + " mm"
        );
    }

    private void performAction(DroneAction action, String description) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(description);
        }

        final var drone = droneService.getDrone().orElseThrow(() -> new DroneException("Drone is not connected"));

        try {
            action.executeAction(drone);
        } catch (TelloException e) {
            throw new DroneException(description + " failed: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    interface DroneAction {
        void executeAction(TelloDrone drone) throws TelloException;
    }
}
