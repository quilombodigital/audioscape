package org.quilombo.audioscape.video;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.quilombo.audioscape.util.Util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoRecorderGrabber extends Thread {

    ScheduledThreadPoolExecutor exec = Util.createScheduledExecutor(1);

    private VideoRecorder videoRecorder;
    private OpenCVFrameGrabber grabber;

    private long startTime = 0;
    private long videoTS = 0;

    boolean isRunning = false;

    public VideoRecorderGrabber(VideoRecorder videoRecorder) throws Exception {
        this.videoRecorder = videoRecorder;
        grabber = new OpenCVFrameGrabber(videoRecorder.getVideoConfig().videoDeviceIndex);
        grabber.setImageWidth(videoRecorder.getVideoConfig().width);
        grabber.setImageHeight(videoRecorder.getVideoConfig().height);
        grabber.start();
    }

    public void run() {
        isRunning = true;

        exec.scheduleAtFixedRate(new Runnable() {

            public void run() {
                try {

                    Frame capturedFrame = grabber.grab();

                    if (capturedFrame != null) {

                        if (startTime == 0)
                            startTime = System.currentTimeMillis();

                        videoTS = 1000 * (System.currentTimeMillis() - startTime);

                        // Check for AV drift
                        if (videoTS > videoRecorder.getRecorder().getTimestamp()) {
                            videoRecorder.getRecorder().setTimestamp(videoTS);
                        }

                        if (videoRecorder.isRunning())
                            videoRecorder.getRecorder().record(capturedFrame);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, (long) 1000 / videoRecorder.getVideoConfig().frameRate, TimeUnit.MILLISECONDS);

    }

    public void shutdown() throws Exception {
        isRunning = false;
        exec.shutdownNow();
        exec.awaitTermination(60, TimeUnit.SECONDS);
        grabber.stop();
    }
}
