package org.quilombo.audioscape.video;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class VideoAudioUtils {

    public static void showAudioDevices() {
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        System.out.println("Audio Devices:");
        for (int i = 0; i < mixerInfo.length; i++)
            System.out.println("- " + i + " " + mixerInfo[i].getName());
    }

    public static void extractAudioFromVideo(String inputVideo, String outputAudio, AudioConfig audioConfig) throws Exception {
        System.out.println("Started extracting Audio from Video");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputVideo);
        grabber.start();

        FrameRecorder recorder = new FFmpegFrameRecorder(outputAudio, audioConfig.audioChannels);
        recorder.setSampleRate(audioConfig.samplerate);
        recorder.setAudioBitrate(audioConfig.bitrate);
        recorder.start();

        Frame frame;
        while ((frame = grabber.grabFrame(true, false, false, false)) != null) {
            recorder.record(frame);
        }
        recorder.stop();
        grabber.stop();
        System.out.println("Finished extracting Audio from Video");
    }

}
