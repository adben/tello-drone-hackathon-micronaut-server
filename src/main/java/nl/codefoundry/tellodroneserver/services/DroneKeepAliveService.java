package nl.codefoundry.tellodroneserver.services;

import jakarta.inject.Singleton;
import me.friwi.tello4j.api.drone.TelloDrone;
import me.friwi.tello4j.api.exception.TelloException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DroneKeepAliveService {
    private static final int DRONE_CONNECTION_STATUS_POLL_INTERVAL_IN_MILLISECONDS = 100;
    private static final int DRONE_KEEP_ALIVE_COMMAND_INTERVAL_IN_MILLISECONDS = 10000;

    public DroneKeepAliveTask startDroneKeepAliveTask(TelloDrone drone, DroneDisconnectHandler droneDisconnectHandler) {
        return new DroneKeepAliveTask(drone, droneDisconnectHandler);
    }

    public static class DroneKeepAliveTask implements Runnable {
        private static final Logger LOG = LoggerFactory.getLogger(DroneKeepAliveTask.class);
        private static int nextThreadId = 1;

        private final TelloDrone drone;
        private final DroneDisconnectHandler droneDisconnectHandler;
        private Thread droneKeepAliveThread;

        public DroneKeepAliveTask(TelloDrone drone, DroneDisconnectHandler droneDisconnectHandler) {
            this.drone = drone;
            this.droneDisconnectHandler = droneDisconnectHandler;

            droneKeepAliveThread = new Thread(this,"drone-keep-alive-" + nextThreadId++);
            droneKeepAliveThread.start();
        }

        @Override
        public void run() {
            final var maxIteration = DRONE_KEEP_ALIVE_COMMAND_INTERVAL_IN_MILLISECONDS / DRONE_CONNECTION_STATUS_POLL_INTERVAL_IN_MILLISECONDS;
            int iteration = 0;
            try {
                while(!Thread.interrupted() && drone.isConnected()) {
                    if (iteration == 0) {
                        final var batteryLevel = drone.fetchBattery();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Current drone battery level {}%", batteryLevel);
                        }
                    }

                    Thread.sleep(DRONE_CONNECTION_STATUS_POLL_INTERVAL_IN_MILLISECONDS);

                    iteration = (iteration + 1) % maxIteration;
                }
                LOG.debug("Drone disconnected unexpectedly");
            } catch (InterruptedException | TelloException e) {
                LOG.debug("Failed to keep drone alive due to " + e.getMessage());
            } finally {
                LOG.debug("Gracefully disconnecting drone...");
                stop();
                droneDisconnectHandler.handleDroneDisconnect();
            }
        }

        public void stop() {
            if (droneKeepAliveThread == null) {
                return;
            }

            droneKeepAliveThread.interrupt();
            droneKeepAliveThread = null;
        }
    }
}
