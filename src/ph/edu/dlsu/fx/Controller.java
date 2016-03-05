package ph.edu.dlsu.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {

    // the FXML startBtn
    @FXML
    private Button startBtn;

    // the FXML image view
    @FXML
    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();

    // a flag to change the startBtn behavior
    private boolean cameraActive = false;

    @FXML
    public void startCamera(ActionEvent actionEvent) {
        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(0);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = () -> {
                    Image imageToShow = grabFrame();
                    currentFrame.setImage(imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the startBtn content
                this.startBtn.setText("Stop Camera");
            } else {
                System.err.println("Failed to open the camera...");
            }
        } else {

            // the camera is not active at this point
            this.cameraActive = false;
            // update again the startBtn content
            this.startBtn.setText("Start Camera");

            // stop the timer
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }

            // release the camera
            this.capture.release();
            // clean the frame
            this.currentFrame.setImage(null);
        }
    }


    private Image grabFrame() {

        Image imageToShow = null;
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // convert the image to gray scale
                    // Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    // convert the Mat object (OpenCV) to Image (JavaFX)
                    imageToShow = mat2Image(frame);
                }

            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return imageToShow;
    }


    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
