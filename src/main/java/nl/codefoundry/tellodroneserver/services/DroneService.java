package nl.codefoundry.tellodroneserver.services;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.friwi.tello4j.api.drone.TelloDrone;
import me.friwi.tello4j.api.drone.WifiDroneFactory;
import me.friwi.tello4j.api.exception.*;
import nl.codefoundry.tellodroneserver.exceptions.DroneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class DroneService {
    private static final Logger LOG = LoggerFactory.getLogger(DroneService.class);

    private final BehaviorSubject<Optional<TelloDrone>> droneSubject = BehaviorSubject.createDefault(Optional.empty());
    private final WifiDroneFactory droneFactory = new WifiDroneFactory();
    private final DroneKeepAliveService droneKeepAliveService;

    private DroneKeepAliveService.DroneKeepAliveTask droneKeepAliveTask;

    @Inject
    public DroneService(DroneKeepAliveService droneKeepAliveService) {
        this.droneKeepAliveService = droneKeepAliveService;
    }

    public synchronized void connect() {
        if (isConnected()) {
            throw new DroneException("Already connected to drone");
        }

        LOG.debug("Connecting to drone");

        final var drone = droneFactory.build();

        try {
            drone.connect();
        } catch (TelloException e) {
            drone.disconnect();
            throw new DroneException("Failed to connect to drone: " + e.getMessage(), e);
        }

        droneSubject.onNext(Optional.of(drone));

        droneKeepAliveTask = droneKeepAliveService.startDroneKeepAliveTask(drone, this::disconnect);
    }

    public synchronized void disconnect() {
        if (!isConnected()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Disconnecting from drone");
        }

        getDrone().get().disconnect();
        droneSubject.onNext(Optional.empty());

        droneKeepAliveTask.stop();
        droneKeepAliveTask = null;
    }

    public Optional<TelloDrone> getDrone() {
        return droneSubject.getValue();
    }

    public Flowable<Optional<TelloDrone>> drone$() {
        return this.droneSubject.toFlowable(BackpressureStrategy.LATEST);
    }

    public boolean isConnected() {
        return this.getDrone().isPresent();
    }

    public Flowable<Boolean> isConnected$() {
        return this.drone$().map(Optional::isPresent);
    }
}
