package org.quilombo.audioscape.audio;

import org.quilombo.audioscape.video.AudioConfig;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    TargetDataLine line;
    private AudioFormat format;
    private File filename;

    public AudioRecorder(AudioConfig config) {
        AudioFormat format = new AudioFormat(config.samplerate, config.sampleSizeInBits,
                config.audioChannels, true, false);
        this.format = format;
    }

    RecordigThread rec;

    public void start(File filename) throws Exception {
        this.filename = filename;
        rec = new RecordigThread();
        rec.setDaemon(true);
        rec.start();
        while (!rec.isAlive()) {
            Thread.sleep(10);
        }
    }

    public void stop() {
        line.stop();
        line.close();
        System.out.println("Recording Finished");
    }

    public class RecordigThread extends Thread {

        public void run() {
            try {

                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                // checks if system supports the data line
                if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("Line not supported");
                    System.exit(0);
                }
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();   // start capturing

                System.out.println("Start capturing...");

                AudioInputStream ais = new AudioInputStream(line);

                System.out.println("Start recording...");

                AudioSystem.write(ais, fileType, filename);

            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws Exception {

        AudioRecorder recorder = new AudioRecorder(new AudioConfig());

        recorder.start(new File("C:/tmp/1.wav"));
        Thread.sleep(5000);
        recorder.stop();
        Thread.sleep(1000);
        recorder.start(new File("C:/tmp/2.wav"));
        Thread.sleep(5000);
        recorder.stop();

    }
}
