package nl.codefoundry.tellodroneserver.services;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.friwi.tello4j.api.exception.*;
import me.friwi.tello4j.api.video.TelloVideoFrame;
import me.friwi.tello4j.api.video.VideoListener;
import nl.codefoundry.tellodroneserver.exceptions.DroneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.Optional;

@Singleton
public class DroneVideoService {
    private static final Logger LOG = LoggerFactory.getLogger(DroneService.class);

    private final DroneVideoListener droneVideoListener = new DroneVideoListener();
    private final DroneService droneService;

    @Inject
    public DroneVideoService(DroneService droneService) {
        this.droneService = droneService;
    }

    public void startVideoStream() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Enabling video streaming...");
        }

        final var drone = this.droneService.getDrone().orElseThrow(() -> new DroneException("Drone is not connected"));

        try {
            drone.setStreaming(true);
        } catch (TelloException e) {
            throw new DroneException("Failed to enable video streaming", e);
        }

        drone.addVideoListener(droneVideoListener);
    }

    public void stopVideoStream() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Disabling video streaming...");
        }

        final var drone = this.droneService.getDrone().orElseThrow(() -> new DroneException("Drone is not connected"));

        drone.removeVideoListener(droneVideoListener);
        droneVideoListener.clearLastVideoFrame();

        try {
            drone.setStreaming(false);
        } catch (TelloException e) {
            throw new DroneException("Failed to disable video streaming", e);
        }
    }

    public Flowable<Optional<BufferedImage>> getVideoStream() {
        return droneVideoListener.getVideoStream();
    }

    public Optional<BufferedImage> getLastVideoFrame() {
        return droneVideoListener.getLastVideoFrame();
    }

    private static class DroneVideoListener implements VideoListener {
        private Optional<BufferedImage> lastVideoFrame = Optional.empty();
        private BehaviorSubject<Optional<BufferedImage>> videoFrameSubject = BehaviorSubject.createDefault(Optional.empty());

        @Override
        public void onFrameReceived(TelloVideoFrame telloVideoFrame) {
            BufferedImage image = telloVideoFrame.getImage();
            BufferedImage modifiedImage = ImageRecognitionUtil.removeAllButCollor(image);
            setLastVideoFrame(Optional.of(modifiedImage));
        }

        public void clearLastVideoFrame() {
            setLastVideoFrame(Optional.empty());
        }

        public Flowable<Optional<BufferedImage>> getVideoStream() {
            return videoFrameSubject.toFlowable(BackpressureStrategy.LATEST);
        }

        public Optional<BufferedImage> getLastVideoFrame() {
            return lastVideoFrame;
        }

        private void setLastVideoFrame(Optional<BufferedImage> lastVideoFrame) {
            this.lastVideoFrame = lastVideoFrame;
            videoFrameSubject.onNext(lastVideoFrame);
        }
    }
}
