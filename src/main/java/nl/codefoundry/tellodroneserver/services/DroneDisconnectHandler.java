package nl.codefoundry.tellodroneserver.services;

@FunctionalInterface
public interface DroneDisconnectHandler {
    void handleDroneDisconnect();
}
