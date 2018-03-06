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
import org.quilombo.audioscape.gui.VideoPlayer;
import org.quilombo.audioscape.speech.SpeechToText;
import org.quilombo.audioscape.util.Util;
import org.quilombo.audioscape.video.VideoAudioUtils;
import org.quilombo.audioscape.video.VideoRecorder;
import org.quilombo.audioscape.videomixer.VideoMixer;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.tadbir.net.Google.Constants.Search.Image.FILE_TYPE_KEY_JPG;
import static it.tadbir.net.Google.Constants.Search.Image.SIZE_KEY_MEDIUM;

public class AudioScape {

    File RESULTS_DIRECTORY = new File("data/result");

    public static AudioScapeStates state = AudioScapeStates.INITIALIZING;
    private AudioScapeConfig config;
    private VideoPlayer player;

    public AudioScape() throws Exception {
        config = AudioScapeConfig.load();
    }

    public void loop() throws Exception {

        AudioScapeStates currentState = state;

        if (currentState == AudioScapeStates.INITIALIZING) {
            disableAnnoyingJNativeHookLogsAndAddGlobalKeyListener();
            createFullscrenVideoInterface();
            setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
        } else if (currentState == AudioScapeStates.CHOOSING_RANDOM_VIDEO) {
            chooseRandomVideo();
            setState(AudioScapeStates.SHOWING_VIDEO);
        } else if (currentState == AudioScapeStates.SHOWING_VIDEO) {
            showVideo();
        } else if (currentState == AudioScapeStates.PREPARE_INSTRUCTIONS) {
            player.stop();
            setState(AudioScapeStates.SHOWING_INSTRUCTIONS);
        } else if (currentState == AudioScapeStates.SHOWING_INSTRUCTIONS) {
            showingInstructions();
        } else if (currentState == AudioScapeStates.PREPARE_FOR_RECORDING) {

        } else if (currentState == AudioScapeStates.RECORDING) {

        } else if (currentState == AudioScapeStates.PROCESSING) {

        } else if (currentState == AudioScapeStates.SHOWING_RESULT) {

        } else if (currentState == AudioScapeStates.CLEANUP_ON_ERROR) {

        }

    }

    private void createFullscrenVideoInterface() {
        player = new VideoPlayer();
    }

    private void chooseRandomVideo() throws Exception {
        File randomSession = Util.randomFileInDirectory(RESULTS_DIRECTORY);
        if (randomSession == null) {
            setState(AudioScapeStates.SHOWING_INSTRUCTIONS);
            return;
        }
        File randomVideo = Util.randomFileInDirectory(new File(randomSession, "mix")); //TODO não pode repetir o mesmo...
        player.stop();
        player.prepare(randomVideo.getAbsolutePath());
        player.start();
    }

    private void showVideo() throws Exception {
        if (!player.isPlaying()) {
            setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
        }
    }

    private void showingInstructions() throws Exception {
        if (!player.isPlaying()) {
            player.stop();
            player.prepare("videos/instructions.mp4");
            player.start();
        }
    }

    private void showingPrepareForRecording() throws Exception {
        if (!player.isPlaying()) {
            player.stop();
            player.prepare("videos/prepare.mp4");
            player.start();
        }
    }

    public void flow() throws Exception {


        //CREATE SESSION ID
        UUID sessionId = UUID.randomUUID();
        String recordResultDirectory = "data/result/" + sessionId + "/record";
        new File(recordResultDirectory).mkdirs();

        //RECORD USER HERE
        String userVideoFilename = recordResultDirectory + "/record.flv";
        Util.createDirForFile(userVideoFilename);
        VideoRecorder videoRecorder = new VideoRecorder(config.recorderVideo, config.recorderAudio, userVideoFilename);

        //while (state == STATE_WAITING_USER_PRESS) {
        //    Thread.sleep(100);
        //}

        videoRecorder.start();

        //TODO elapsed too small throw message and retry

        //while (state == STATE_RECORDING) {
        //     Thread.sleep(100);
        // }
        videoRecorder.stop();

        GlobalScreen.unregisterNativeHook();

        //PROCESSING STARTS HERE

        //EXTRACT AUDIO FROM VIDEO
        String userAudioFilename = recordResultDirectory + "/audio.wav";
        VideoAudioUtils.extractAudioFromVideo(userVideoFilename, userAudioFilename, config.recorderAudio);

        //CONVERT AUDIO TO TEXT
        SpeechToText stt = new SpeechToText();
        String transcription = stt.convert(config.recorderAudio, userAudioFilename);

        String userTranscriptFilename = recordResultDirectory + "/transcription.txt";
        Util.writeToFile(transcription, userTranscriptFilename);

        //EXTRACT EMOTION DATA
        TextAnalysisConfig textAnalysisConfig = TextAnalysisConfig.load();
        TextAnalysis tte = new TextAnalysis(textAnalysisConfig);
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


    private void disableAnnoyingJNativeHookLogsAndAddGlobalKeyListener() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new AudioScapeKeyListener());
    }


    public class AudioScapeKeyListener implements NativeKeyListener {
        @Override
        public void nativeKeyTyped(NativeKeyEvent e) {

        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {


            /*if ((AudioScape.state == STATE_WAITING_USER_PRESS) && (e.getKeyCode() == NativeKeyEvent.VC_SPACE)) {
                state = STATE_RECORDING;
            }*/
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
            if (e.getKeyCode() == NativeKeyEvent.VC_F) {
                if (player.isFullscreen())
                    player.disableFullScreen();
                else
                    player.enableFullScreen();
            }
            if (isInState(AudioScapeStates.PREPARE_INSTRUCTIONS) || isInState(AudioScapeStates.SHOWING_INSTRUCTIONS)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_O) {
                    setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
                }
            } else if (isInState(AudioScapeStates.CHOOSING_RANDOM_VIDEO) || isInState(AudioScapeStates.SHOWING_VIDEO)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_P) {
                    setState(AudioScapeStates.PREPARE_INSTRUCTIONS);
                }
            }
            /*if ((state == STATE_RECORDING) && (e.getKeyCode() == NativeKeyEvent.VC_SPACE)) {
                state = STATE_PROCESSING;
            }*/
        }


    }

    private boolean isInState(AudioScapeStates currentState) {
        return AudioScape.state == currentState;
    }

    private void setState(AudioScapeStates currentState) {
        System.out.println("CHANGING STATE FROM " + AudioScape.state + " TO " + currentState);
        AudioScape.state = currentState;
    }


    public static void main(String[] args) throws Exception {
        AudioScape scape = new AudioScape();

        while (true) {
            scape.loop();
        }

    }

    static Random random = new Random();


}
