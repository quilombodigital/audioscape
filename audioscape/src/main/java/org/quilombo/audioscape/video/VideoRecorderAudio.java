package org.quilombo.audioscape.video;

import org.quilombo.audioscape.util.Util;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoRecorderAudio extends Thread {

    private VideoRecorder videoRecorder;
    ScheduledThreadPoolExecutor exec = Util.createScheduledExecutor(1);
    boolean isRunning = false;

    public VideoRecorderAudio(VideoRecorder videoRecorder) {
        this.videoRecorder = videoRecorder;
    }

    public void shutdown() throws Exception {
        isRunning = false;
        exec.shutdownNow();
        exec.awaitTermination(60, TimeUnit.SECONDS);
    }

    public void run() {
        isRunning = true;
        AudioFormat audioFormat = new AudioFormat(videoRecorder.getAudioConfig().samplerate, videoRecorder.getAudioConfig().sampleSizeInBits, videoRecorder.getAudioConfig().audioChannels, true, false);

        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
        Mixer mixer = AudioSystem.getMixer(minfoSet[videoRecorder.getAudioConfig().deviceIndex]);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            // TargetDataLine line = (TargetDataLine)mixer.getLine(dataLineInfo);
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();

            final int sampleRate = (int) audioFormat.getSampleRate();
            final int numChannels = audioFormat.getChannels();

            int audioBufferSize = sampleRate * numChannels;
            final byte[] audioBytes = new byte[audioBufferSize];

            exec.scheduleAtFixedRate(new Runnable() {

                public void run() {
                    try {
                        int nBytesRead = 0;
                        while (nBytesRead == 0) {
                            nBytesRead = line.read(audioBytes, 0, line.available());
                        }

                        int nSamplesRead = nBytesRead / 2;
                        short[] samples = new short[nSamplesRead];

                        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                        ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                        if (videoRecorder.isRunning())
                            videoRecorder.getRecorder().recordSamples(sampleRate, numChannels, sBuff);
                    } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 0, (long) 1000 / videoRecorder.getVideoConfig().frameRate, TimeUnit.MILLISECONDS);
        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
        }
    }

}
