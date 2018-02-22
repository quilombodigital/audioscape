package org.quilombo.audioscape;

import com.google.common.io.Files;
import it.tadbir.net.Google.Googler;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.quilombo.audioscape.analysis.TextAnalysis;
import org.quilombo.audioscape.analysis.TextAnalysisConfig;
import org.quilombo.audioscape.download.Downloader;
import org.quilombo.audioscape.download.DownloaderConfig;
import org.quilombo.audioscape.speech.SpeechToText;
import org.quilombo.audioscape.util.Util;
import org.quilombo.audioscape.video.VideoAudioUtils;
import org.quilombo.audioscape.video.VideoRecorder;
import org.quilombo.audioscape.videomixer.VideoMixer;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static it.tadbir.net.Google.Constants.Search.Image.FILE_TYPE_KEY_JPG;
import static it.tadbir.net.Google.Constants.Search.Image.SIZE_KEY_MEDIUM;

public class AudioScape {

    public static final String STATE_WAITING_USER_PRESS = "waiting_user";
    public static final String STATE_RECORDING = "recording";
    public static final String STATE_PROCESSING = "processing";
    public static final String STATE_SHOWING = "showing";

    public String currentState = STATE_WAITING_USER_PRESS;
    AudioscapeConfig config;

    public AudioScape() throws Exception {
        config = AudioscapeConfig.load();
    }

    public class GlobalKeyListener implements NativeKeyListener {
        @Override
        public void nativeKeyTyped(NativeKeyEvent e) {
        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            if ((currentState == STATE_WAITING_USER_PRESS) && (e.getKeyCode() == NativeKeyEvent.VC_SPACE)) {
                currentState = STATE_RECORDING;
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
            if ((currentState == STATE_RECORDING) && (e.getKeyCode() == NativeKeyEvent.VC_SPACE)) {
                currentState = STATE_PROCESSING;
            }
        }
    }

    public void flow() throws Exception {

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new GlobalKeyListener());


        //CREATE SESSION ID
        UUID sessionId = UUID.randomUUID();
        String recordResultDirectory = "data/result/" + sessionId + "/record";
        new File(recordResultDirectory).mkdirs();

        //RECORD USER HERE
        String userVideoFilename = recordResultDirectory + "/record.flv";
        Util.createDirForFile(userVideoFilename);
        VideoRecorder videoRecorder = new VideoRecorder(config.recorderVideo, config.recorderAudio, userVideoFilename);

        while (currentState == STATE_WAITING_USER_PRESS) {
            Thread.sleep(100);
        }

        videoRecorder.start();

        //TODO elapsed too small throw message and retry

        while (currentState == STATE_RECORDING) {
            Thread.sleep(100);
        }
        videoRecorder.stop();

        GlobalScreen.unregisterNativeHook();

        //PROCESSING STARTS HERE

        //EXTRACT AUDIO FROM VIDEO
        String userAudioFilename = recordResultDirectory + "/audio.wav";
        VideoAudioUtils.extractAudioFromVideo(userVideoFilename, userAudioFilename);

        //CONVERT AUDIO TO TEXT
        SpeechToText stt = new SpeechToText();
        String transcription = stt.convert(config.recorderAudio, userAudioFilename);

        String userTranscriptFilename = recordResultDirectory + "/transcription.txt";
        Util.writeToFile(transcription, userTranscriptFilename);

        //EXTRACT EMOTION DATA
        TextAnalysis tte = new TextAnalysis(new TextAnalysisConfig());
        String emotionResult = tte.getEmotion(10, transcription);
        String userEmotionFilename = recordResultDirectory + "/emotion.txt";
        Util.writeToFile(emotionResult, userEmotionFilename);

        //DOWNLOAD ALL WORDS IN TRANSCRIPTION

        Downloader down = new Downloader();
        String[] words = transcription.trim().split(" ");
        for (String word : words) {
            File directory = new File("data/download/" + word);
            if (!directory.exists()) {
                System.out.println("downloading: " + word);
                ArrayList<Googler> queries = new ArrayList<>();
                Googler query = new Googler();
                query.setQuery(word).setOption().setFileType(FILE_TYPE_KEY_JPG).setSize(SIZE_KEY_MEDIUM, null, 0, 0);
                queries.add(query);
                down.SAVE_PATH = "data/download/";
                down.download(new DownloaderConfig(), queries);
            }
        }

        //CHOOSE WORD IMAGES TO GENERATE VIDEO, and generate 3 versions
        String versionsDirectory = "data/result/" + sessionId + "/versions";
        for (int t = 0; t < 3; t++) {
            File target = new File(versionsDirectory + "/version_" + t);
            target.mkdirs();
            int count = 0;
            for (String word : words) {
                count++;
                File directory = new File("data/download/" + word);
                File[] files = directory.listFiles();
                File randomFile = files[random.nextInt(files.length)];
                Files.copy(randomFile, new File(target, "image_" + count + ".jpg"));
            }
        }

        // MIX AUDIO, AND IMAGES

        VideoMixer mixer = new VideoMixer();
        for (int t = 0; t < 3; t++) {
            String mixFilename = "data/result/" + sessionId + "/mix/mix_" + t + ".mp4";
            String slideFilename = "data/result/" + sessionId + "/slide/slide_" + t + ".mp4";
            Util.createDirForFile(mixFilename);
            Util.createDirForFile(slideFilename);
            mixer.mix(transcription, versionsDirectory + "/version_" + t, userAudioFilename, slideFilename, mixFilename);
        }

        System.out.println("THE END");
    }


    public static void main(String[] args) throws Exception {
        AudioScape scape = new AudioScape();
        scape.flow();

    }

    static Random random = new Random();
}
