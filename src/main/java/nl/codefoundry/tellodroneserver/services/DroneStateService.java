package nl.codefoundry.tellodroneserver.services;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.friwi.tello4j.api.exception.TelloException;
import me.friwi.tello4j.api.state.TelloDroneState;
import nl.codefoundry.tellodroneserver.exceptions.DroneException;

@Singleton
public class DroneStateService {
    private final DroneService droneService;

    @Inject
    public DroneStateService(DroneService droneService) {
        this.droneService = droneService;
    }

    public Flowable<TelloDroneState> droneState$() {
        return droneService.drone$().switchMap((drone) -> drone.isEmpty()
            ? Flowable.empty()
            : Flowable.create(emitter -> {
                drone.get().addStateListener((state1, state2) -> emitter.onNext(state2));
            }, BackpressureStrategy.LATEST)
        );
    }

    public int getBatteryLevel() {
        final var drone = droneService.getDrone().orElseThrow(() -> new DroneException("Drone is not connected"));

        try {
            return drone.fetchBattery();
        } catch (TelloException e) {
            throw new DroneException("Failed to retrieve drone battery level", e);
        }
    }
}
