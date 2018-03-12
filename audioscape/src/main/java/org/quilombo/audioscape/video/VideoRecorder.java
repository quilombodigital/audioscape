package org.quilombo.audioscape.video;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class VideoRecorder {

    private VideoConfig config;
    private AudioConfig audioConfig;
    private String filename;

    private FFmpegFrameRecorder recorder;
    VideoRecorderGrabber grabberThread;
    VideoRecorderAudio audioThread;
    private long recordStart;


    public VideoRecorder(VideoConfig config, AudioConfig audioConfig, String filename) {
        this.config = config;
        this.audioConfig = audioConfig;
        this.filename = filename;
        VideoAudioUtils.showAudioDevices();
    }

    public void start() throws Exception {

        grabberThread = new VideoRecorderGrabber(this);

        recorder = new FFmpegFrameRecorder(
                filename,
                config.width, config.height, audioConfig.audioChannels);
        recorder.setInterleaved(true);
        // https://trac.ffmpeg.org/wiki/StreamingGuide)
        recorder.setVideoOption("tune", "zerolatency");
        // (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("preset", "ultrafast");
        // Constant Rate Factor (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("crf", "28");
        recorder.setVideoBitrate(config.videoBitrate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(config.frameRate);
        recorder.setGopSize(config.gopFrameRate);

        // We don't want variable bitrate audio
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(audioConfig.quality);
        recorder.setAudioBitrate(audioConfig.bitrate);
        recorder.setSampleRate(audioConfig.samplerate);
        recorder.setAudioChannels(audioConfig.audioChannels);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

        recorder.start();

        audioThread = new VideoRecorderAudio(this);

        audioThread.start();
        grabberThread.start();
        recordStart = System.currentTimeMillis();
        System.out.println("started recording");
    }

    public void stop() throws Exception {
        System.out.println("stopping recording");
        running = false;
        grabberThread.shutdown();
        audioThread.shutdown();
        recorder.stop();
        System.out.println("stopped recording");
    }

    boolean running = true;

    public boolean isRunning() {
        return running;
    }

    public VideoConfig getVideoConfig() {
        return config;
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    public FFmpegFrameRecorder getRecorder() {
        return recorder;
    }

    public long recordElapsed(){
        return System.currentTimeMillis() - recordStart;
    }

    public static void main(String[] args) throws Exception {
        VideoRecorder videoRecorder = new VideoRecorder(new VideoConfig(), new AudioConfig(), "teste.flv");
        videoRecorder.start();
        Thread.sleep(5000);
        videoRecorder.stop();

    }


}
