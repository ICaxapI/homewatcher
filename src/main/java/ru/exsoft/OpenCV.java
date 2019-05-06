package ru.exsoft;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class OpenCV {
    private static Watcher watcher;

    public static void init(int width, int height, int sensitivity, double maxArea) {
        try {
            if (System.getProperty("os.name").equals("Linux")) {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            } else if (System.getProperty("os.arch").equals("amd64") && System.getProperty("os.name").toLowerCase().contains("windows")) {
                System.loadLibrary("./lib/windows/x64/opencv_java341");
            } else {
                System.loadLibrary("./lib/windows/x86/opencv_java341");
            }
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }

        VideoCapture camera = new VideoCapture(0);
        camera.set(3, width);
        camera.set(4, height);

        if (!camera.isOpened()) {
            System.out.println("Error, camera not found");
        } else {
            watcher = new Watcher(camera, width, height, sensitivity, maxArea);
            watcher.start();
        }
    }

    public static Watcher getWatcher() {
        return watcher;
    }

    public static class Watcher extends Thread {
        private static final Scalar scalarBlue = new Scalar(0.0D, 0.0D, 255.0D);
        private static final Scalar scalarGreen = new Scalar(0.0D, 255.0D, 0.0D);
        private static final Scalar scalarRed = new Scalar(255.0D, 0.0D, 0.0D);
        private static final Size size = new Size(3.0D, 3.0D);
        private static final Mat v = new Mat();

        private Mat frameCurrent;
        private Mat framePrevious;
        private Mat frameResult;
        private final Mat frame;

        private VideoCapture camera;
        private int sensitivity;
        private double maxArea;
        private int fps = 0;
        private boolean first = true;
        private long lastTime = System.currentTimeMillis();

        public Watcher(VideoCapture camera, int width, int height, int sensitivity, double maxArea) {
            this.camera = camera;
            this.sensitivity = sensitivity;
            this.maxArea = maxArea;
            frame = new Mat(width, height, CvType.CV_8UC3);
            frameCurrent = new Mat(width, height, CvType.CV_8UC3);
            framePrevious = new Mat(width, height, CvType.CV_8UC3);
            frameResult = new Mat(width, height, CvType.CV_8UC3);
        }

        @Override
        public void run() {
            while (this.isAlive()) {
                while (!camera.read(frame)) {
                    try {
                        sleep(1000 / (fps > 0 ? fps : 1));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (frame) {
                    frame.notifyAll();
                    fps++;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTime > 1000L) {
                        System.out.println("Fps: " + fps);
                        lastTime = currentTime;
                        fps = 0;
                    }
                    frame.copyTo(frameCurrent);
                    Imgproc.GaussianBlur(frameCurrent, frameCurrent, size, 500.0D);
                    if (!first) {
                        Core.subtract(framePrevious, frameCurrent, frameResult);
                        Imgproc.cvtColor(frameResult, frameResult, 7);
                        Imgproc.threshold(frameResult, frameResult, sensitivity, 255.0D, 0);
                        ArrayList<MatOfPoint> contours = new ArrayList<>();
                        Imgproc.findContours(frameResult, contours, v, 1, 2);
                        v.release();
                        boolean found = false;
                        for (int tempx = 0; tempx < contours.size(); ++tempx) {
                            Mat contour = contours.get(tempx);
                            double contourarea = Imgproc.contourArea(contour);
                            if (contourarea > maxArea) {
                                found = true;
                                Rect r = Imgproc.boundingRect(contours.get(tempx));
                                Imgproc.drawContours(frame, contours, tempx, scalarRed);
                                Imgproc.rectangle(frame, r.br(), r.tl(), scalarBlue, 1);
                            }
                            contour.release();
                        }
                        if (found) {

                        }
                    }
                    frame.notifyAll();
                    try {
                        frame.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                first = false;
                frameCurrent.copyTo(framePrevious);
                frame.release();
                frameResult.release();
                frameCurrent.release();
            }
            camera.release();
        }

        public Image getCurrentImage() {
            synchronized (frame) {
                try {
                    frame.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MatOfByte byteMat = new MatOfByte();
                Imgcodecs.imencode(".bmp", frame, byteMat);
                frame.notifyAll();
                return new Image(new ByteArrayInputStream(byteMat.toArray()));
            }
        }
    }
}
