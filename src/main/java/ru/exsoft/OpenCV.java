package ru.exsoft;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_features2d.BFMatcher;
import org.bytedeco.opencv.opencv_xfeatures2d.SURF;
import org.opencv.core.Rect;
import ru.exsoft.config.Config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_video.estimateRigidTransform;
import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvSaveImage;

public class OpenCV {
    private static Watcher watcher;

    public static void init(Config config, boolean isGui) {
        watcher = new Watcher(config, isGui);
        watcher.start();
    }

    public static Watcher getWatcher() {
        return watcher;
    }

    public static class Watcher extends Thread {
        private static final CvScalar scalarRed = new CvScalar(0.0D, 0.0D, 255.0D, 255.0D);
        private static final CvScalar scalarGreen = new CvScalar(0.0D, 255.0D, 0.0D, 255.0D);
        private static final CvScalar scalarBlue = new CvScalar(255.0D, 0.0D, 0.0D, 255.0D);
        private static final long startWatcherTime = System.currentTimeMillis();
        private static final OpenCVFrameConverter.ToIplImage converterImage = new OpenCVFrameConverter.ToIplImage();
        private static final OpenCVFrameConverter.ToMat converterMat = new OpenCVFrameConverter.ToMat();

        private static CustomRecorder currentRecorder;
        private static Config config;
        private static IplImage clearFrame;
        private static IplImage currentImage;
        private static IplImage prevImage;
        private static IplImage diffImage;
        private static OpenCVFrameGrabber grabber;
        private static CvMemStorage storage;
        private static CanvasFrame canvasFrame;
        private static CanvasFrame canvasFrameDiff;
        private static ArrayList<IplImage> grabbedActiveFrames = new ArrayList<>();
        private static Boolean needImage = false;
        private static Boolean needVideo = false;

        private static int fps = 30;
        private static boolean isGui;
        private static boolean startDetecting;
        private static byte activeFrames = 0;
        private static long lastActiveFrame;
        private static boolean recordActive;
        private static boolean ownerInHome;

        public Watcher(Config config, boolean isGui) {
            Watcher.config = config;
            Watcher.isGui = isGui;
            currentImage = IplImage.create(config.width, config.height, 8, 1);
            diffImage = IplImage.create(config.width, config.height, 8, 1);
            grabber = new OpenCVFrameGrabber(0);
            grabber.setImageWidth(config.width);
            grabber.setImageHeight(config.height);
            storage = CvMemStorage.create();
            clearFrame = IplImage.create(config.width, config.height, 8, 3);
            currentRecorder = new CustomRecorder(config);
            if (isGui) {
                canvasFrame = new CanvasFrame("HomeWatcher");
                canvasFrame.setCanvasSize(config.width, config.height);
                canvasFrameDiff = new CanvasFrame("HomeWatcher Diff");
                canvasFrameDiff.setCanvasSize(config.width, config.height);
            }
        }

        @Override
        public void run() {
            try {
                grabber.start();
                int framesForCurrentSecond = 0;
                long lastFpsComputing = System.currentTimeMillis();
                Frame capturedFrame = null;
                Mat prevImageMat = converterMat.convertToMat(grabber.grabFrame());
                Mat currentImageMat = null;
                while ((capturedFrame = grabber.grab()) != null) {
                    currentImageMat = converterMat.convertToMat(capturedFrame);
                    //Mat mat = estimateRigidTransform(prevImageMat, currentImageMat, false);
                    //warpAffine(currentImageMat, currentImageMat, mat, new Size(config.width, config.height));
                    framesForCurrentSecond++;
                    if (System.currentTimeMillis() - lastFpsComputing >= 1000) {
                        fps = framesForCurrentSecond;
                        framesForCurrentSecond = 0;
                        System.out.println(fps);
                        lastFpsComputing = System.currentTimeMillis();
                    }
                    cvClearMemStorage(storage);
                    clearFrame = converterImage.convert(converterImage.convert(currentImageMat));
                    cvCvtColor(clearFrame, currentImage, CV_RGB2GRAY);
                    cvSmooth(currentImage, currentImage, CV_GAUSSIAN, 9, 9, 2, 2);
                    if (!startDetecting && System.currentTimeMillis() - startWatcherTime >= 1000 * config.waitSeconds) {
                        startDetecting = true;
                        System.out.println("Detector stabilized");
                    }
                    if (ownerInHome){
                        TimeUnit.SECONDS.sleep(1);
                    }
                    if (prevImage != null && startDetecting && !ownerInHome) {
                        cvAbsDiff(currentImage, prevImage, diffImage);
                        cvThreshold(diffImage, diffImage, config.sensitivity, 255, CV_ADAPTIVE_THRESH_MEAN_C);
                        CvSeq contour = new CvSeq(null);
                        cvFindContours(diffImage, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
                        boolean found = false;
                        //int minX = 0;
                        //int minY = 0;
                        //int maxX = 0;
                        //int maxY = 0;
                        while (contour != null && !contour.isNull()) {
                            if (contour.elem_size() > 0) {
                                double contourarea = cvContourArea(contour);
                                if (contourarea >= config.minArea) {
                                    found = true;
                                    cvDrawContours(clearFrame, contour, scalarRed, scalarRed, 0);
                                    //CvRect r = cvBoundingRect(contour);
                                    //cvRectangleR(clearFrame, r, scalarRed, 1, CV_AA, 0);
                                    //if (minX > r.x() - (r.width() / 2)) {
                                    //    minX = r.x() - (r.width() / 2);
                                    //}
                                    //if (minY > r.y() - (r.height() / 2)) {
                                    //    minY = r.y() - (r.height() / 2);
                                    //}
                                    //if (maxX < r.x() - (r.width() / 2)) {
                                    //    maxX = r.x() - (r.width() / 2);
                                    //}
                                    //if (maxY < r.y() - (r.height() / 2)) {
                                    //    maxY = r.y() - (r.height() / 2);
                                    //}
                                }
                            }
                            contour = contour.h_next();
                        }
                        //if (found) {
                        //    int averageWidth = maxX - minX;
                        //    int averageHeight = maxY - minY;
                        //    cvRectangleR(clearFrame, new CvRect(minX, minY, averageWidth, averageHeight), scalarRed, 2, CV_AA, 0);
                        //    //cvRectangle(clearFrame, new CvPoint(minX, minY), new CvPoint(maxX, maxY), scalarRed, 2, CV_AA, 0);
                        //}
                        if (needVideo){
                            needVideo = false;
                            grabbedActiveFrames.add(clearFrame.clone());
                            recordActive = true;
                            lastActiveFrame = System.currentTimeMillis();
                            currentRecorder.start(fps, grabbedActiveFrames);
                        }
                        if (found && !recordActive) {
                            activeFrames++;
                            grabbedActiveFrames.add(clearFrame.clone());
                            if (activeFrames >= config.framesTrigger) {
                                recordActive = true;
                                lastActiveFrame = System.currentTimeMillis();
                                currentRecorder.start(fps, grabbedActiveFrames);
                            }
                        } else if (found && recordActive) {
                            lastActiveFrame = System.currentTimeMillis();
                        } else if (!recordActive) {
                            activeFrames = 0;
                            grabbedActiveFrames.forEach(AbstractIplImage::release);
                            grabbedActiveFrames.clear();
                        }
                        if (recordActive) {
                            currentRecorder.record(clearFrame.clone());
                            if (System.currentTimeMillis() - lastActiveFrame >= (1000 * config.endRecordSec)) {
                                recordActive = false;
                                new Thread(() -> currentRecorder.stop()).start();
                            }
                        }
                        prevImage.release();
                        prevImageMat.release();
                    }
                    if (isGui) {
                        canvasFrame.showImage(converterImage.convert(clearFrame));
                        canvasFrameDiff.showImage(converterImage.convert(diffImage));
                    }
                    if (needImage) {
                        synchronized (clearFrame) {
                            clearFrame.notifyAll();
                            clearFrame.wait();
                        }
                        needImage = false;
                    }
                    prevImage = currentImage.clone();
                    prevImageMat = currentImageMat.clone();
                    diffImage.release();
                    currentImage.release();
                    currentImageMat.release();
                    diffImage = IplImage.create(config.width, config.height, 8, 1);
                    currentImage = IplImage.create(config.width, config.height, 8, 1);
                }
                grabber.stop();
                canvasFrame.dispose();
            } catch (FrameGrabber.Exception | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public static File getCurrentImage() {
            synchronized (clearFrame) {
                try {
                    needImage = true;
                    clearFrame.wait();
                    if (config.rotate) {
                        IplImage rotatedFrame = IplImage.create(config.width, config.height, 8, 3);
                        cvWarpAffine(clearFrame, rotatedFrame, new CvMat(getRotationMatrix2D(new Point2f(config.width / 2, config.height / 2), Math.round(config.angle / 180) * 180, 1)));
                        cvSaveImage(config.tempImagePatch, rotatedFrame);
                        rotatedFrame.release();
                    } else {
                        cvSaveImage(config.tempImagePatch, clearFrame);
                    }
                    return new File(config.tempImagePatch);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    clearFrame.notifyAll();
                }
            }
        }

        public static void setOwnerInHome(boolean bool){
            ownerInHome = bool;
        }


        public static void setNeedVideo(Boolean needVideo) {
            Watcher.needVideo = needVideo;
        }

    }

    static class CustomRecorder {
        private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
        private FFmpegFrameRecorder recorder = null;
        private OpenCVFrameConverter.ToIplImage converter;
        private Config config;
        private File tempVideoFile;

        CustomRecorder(Config config) {
            this.config = config;
            String path = String.format(config.tempVideoPatch, sdf.format(new Date()));
            tempVideoFile = new File(path);
            recorder = new FFmpegFrameRecorder(tempVideoFile, config.width, config.height, 0);
            converter = new OpenCVFrameConverter.ToIplImage();
            recorder.setInterleaved(true);
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("crf", "28");
            recorder.setVideoBitrate(2000000);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            //recorder.setVideoCodec(avcodec.AV_CODEC_ID_H265);
            recorder.setFormat("flv");
        }

        public void start(int fps, ArrayList<IplImage> startFrames) {
            try {
                recorder.setFrameRate(fps);
                recorder.start();
                System.out.println("Record started");
                for (IplImage image : startFrames) {
                    record(image);
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        public void record(IplImage frame) {
            try {
                if (config.rotate) {
                    IplImage rotatedFrame = IplImage.create(config.width, config.height, 8, 3);
                    cvWarpAffine(frame, rotatedFrame, new CvMat(getRotationMatrix2D(new Point2f(config.width / 2, config.height / 2), Math.round(config.angle / 180) * 180, 1)));
                    recorder.record(converter.convert(rotatedFrame));
                    rotatedFrame.release();
                    frame.release();
                } else {
                    recorder.record(converter.convert(frame));
                    frame.release();
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            try {
                System.out.println("Recorded!");
                recorder.flush();
                Telegram.sendVideo(tempVideoFile, "Обнаружено движение!", Telegram.MYCHAT);
                //recorder.release();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }

    }
}
