package org.quilombo.audioscape;

import com.google.common.io.Files;
import it.tadbir.net.Google.Googler;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.quilombo.audioscape.download.Downloader;
import org.quilombo.audioscape.download.DownloaderConfig;
import org.quilombo.audioscape.gui.VideoPlayer;
import org.quilombo.audioscape.gui.Words;
import org.quilombo.audioscape.speech.SpeechToText;
import org.quilombo.audioscape.util.ImageConverter;
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
    private String lastProcessionVideo = null;

    public AudioScape() throws Exception {
        config = AudioScapeConfig.load();
    }

    AudioScapeStates lastState = AudioScapeStates.INITIALIZING;

    private UUID lastSessionId;
    private int lastSessionRepeatCounter = 0;
    private boolean lastSessionAlternateFlag;

    private String currentProcessingVideo = "videos/processing.mp4";

    public void loop() throws Exception {

        if (lastState != state) {
            isFirstStateExecution = true;
            lastState = state;
        }
        AudioScapeStates currentState = state;

        if (currentState == AudioScapeStates.INITIALIZING) {
            disableAnnoyingJNativeHookLogsAndAddGlobalKeyListener();
            createFullscrenVideoInterface();
            setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
        } else if (currentState == AudioScapeStates.CHOOSING_RANDOM_VIDEO) {
            if (lastSessionId != null && lastSessionAlternateFlag && lastSessionRepeatCounter < 3) {
                lastSessionRepeatCounter++;
                repeatLastSession();
            } else {
                chooseRandomVideo();
            }
            lastSessionAlternateFlag = !lastSessionAlternateFlag;
            setState(AudioScapeStates.SHOWING_VIDEO);
        } else if (currentState == AudioScapeStates.SHOWING_VIDEO) {
            if (!player.isPlaying()) {
                setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
            }
        } else if (currentState == AudioScapeStates.SHOWING_INSTRUCTIONS) {
            showVideoLoop("videos/instructions.mp4");
        } else if (currentState == AudioScapeStates.ATTENTION) {
            showVideoLoop("videos/attention.mp4");
            if (elapsedInState() > 4000)
                setState(AudioScapeStates.RECORDING);
        } else if (currentState == AudioScapeStates.RECORDING) {
            if (isFirstStateExecution) {
                createUserSession();
                startRecording();
            }
            showVideoLoop("videos/recording.mp4");
        } else if (currentState == AudioScapeStates.PROCESSING) {
            if (isFirstStateExecution) {
                stopRecording();
                startProcessing();
            }
            if (!processingThread.running) {
                if (processingThread.errors) {
                    setState(AudioScapeStates.CLEANUP_ON_ERROR);
                } else {
                    setState(AudioScapeStates.SHOWING_RESULT);
                }
            }
            if (!currentProcessingVideo.equals(lastProcessionVideo)) {
                lastProcessionVideo = currentProcessingVideo;
                player.stop();
            }
            showVideoLoop(currentProcessingVideo);
        } else if (currentState == AudioScapeStates.SHOWING_RESULT) {
            if (isFirstStateExecution) {
                File randomVideo = Util.randomFileInDirectory(new File("data/result/" + sessionId + "/mix"));
                player.stop();
                player.prepare(randomVideo.getAbsolutePath());
                player.start();
            } else {
                if (!player.isPlaying()) {
                    setState(AudioScapeStates.APPROVE);
                }
            }
        } else if (currentState == AudioScapeStates.APPROVE) {
            showVideoLoop("videos/approve.mp4");
        } else if (currentState == AudioScapeStates.APPROVE_YES) {
            showVideoLoop("videos/approve_yes.mp4");
            if (elapsedInState() > 5000)
                setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
        } else if (currentState == AudioScapeStates.APPROVE_NO) {
            if (isFirstStateExecution) {
                cleanUserSession(sessionId.toString());
            }
            showVideoLoop("videos/approve_no.mp4");
            if (elapsedInState() > 5000)
                setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
        } else if (currentState == AudioScapeStates.CLEANUP_ON_ERROR) {
            if (isFirstStateExecution) {
                cleanUserSession(sessionId.toString());
            }
            setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
        }
        isFirstStateExecution = false;
    }

    long startTime = System.currentTimeMillis();

    public long elapsedInState() {
        return System.currentTimeMillis() - startTime;
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
        playSessionMix(randomSession);
    }

    private void repeatLastSession() throws Exception {
        File last = new File(RESULTS_DIRECTORY, lastSessionId.toString());
        playSessionMix(last);
    }

    private void playSessionMix(File sessionDirectory) throws Exception {
        File mixDirectory = new File(sessionDirectory, "mix");
        if (!mixDirectory.exists()) {
            cleanUserSession(sessionDirectory.getName());
            return;
        }
        File mixVideo = Util.randomFileInDirectory(mixDirectory); //TODO não pode repetir o mesmo...
        player.stop();
        player.prepare(mixVideo.getAbsolutePath());
        player.start();
    }

    boolean isFirstStateExecution = true;

    private void showVideoLoop(String filename) throws Exception {
        if (isFirstStateExecution) {
            player.stop();
        }
        if (!player.isPlaying()) {
            player.stop();
            player.prepare(filename);
            player.start();
        }
    }

    UUID sessionId;
    String recordResultDirectory;
    VideoRecorder videoRecorder;
    String userVideoFilename;
    ProcessingThread processingThread;

    public void createUserSession() {
        sessionId = UUID.randomUUID();
        recordResultDirectory = "data/result/" + sessionId + "/record";
        new File(recordResultDirectory).mkdirs();
    }

    public void cleanUserSession(String sessionId) throws Exception {
        deleteDirectory(new File("data/result/" + sessionId));
    }

    public void startRecording() throws Exception {
        userVideoFilename = recordResultDirectory + "/record.flv";
        Util.createDirForFile(userVideoFilename);
        videoRecorder = new VideoRecorder(config.recorderVideo, config.recorderAudio, userVideoFilename);
        videoRecorder.start();
    }

    public void stopRecording() throws Exception {
        videoRecorder.stop();
    }

    public void startProcessing() throws Exception {
        processingThread = new ProcessingThread();
        processingThread.setDaemon(true);
        processingThread.start();
        while (processingThread.running == false) {
            Thread.sleep(10);
        }
    }


    public class ProcessingThread extends Thread {
        public boolean running = false;
        public boolean errors = false;

        public void run() {
            running = true;
            try {
                //PROCESSING STARTS HERE

                currentProcessingVideo = "videos/extracting.mp4";

                //EXTRACT AUDIO FROM VIDEO
                String userAudioFilename = recordResultDirectory + "/audio.wav";
                VideoAudioUtils.extractAudioFromVideo(userVideoFilename, userAudioFilename, config.recorderAudio);

                currentProcessingVideo = "videos/text_to_audio.mp4";

                //CONVERT AUDIO TO TEXT
                SpeechToText stt = new SpeechToText();
                String transcription = stt.convert(config.recorderAudio, userAudioFilename);

                String userTranscriptFilename = recordResultDirectory + "/transcription.txt";
                Util.writeToFile(transcription, userTranscriptFilename);

                //EXTRACT EMOTION DATA

                //disabled for now
                //TextAnalysisConfig textAnalysisConfig = TextAnalysisConfig.load();
                //TextAnalysis tte = new TextAnalysis(textAnalysisConfig);
                //String emotionResult = tte.getEmotion(10, transcription);
                //String userEmotionFilename = recordResultDirectory + "/emotion.txt";
                //Util.writeToFile(emotionResult, userEmotionFilename);

                currentProcessingVideo = "videos/downloading.mp4";

                //DOWNLOAD ALL WORDS IN TRANSCRIPTION
                Downloader down = new Downloader();
                String[] words = transcription.trim().split(" ");
                String wordcloud = "baixando palavras espere baixando palavras espere baixando palavras espere";
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
                    wordcloud = wordcloud + " " + word;
                    String wordsCloudFile = "data/result/" + sessionId + "/record/wordcloud.png";
                    Words.generateCloud(wordcloud, wordsCloudFile);
                    currentProcessingVideo = wordsCloudFile;
                }


                //SANITIZE ALL DOWNLOADED WORDS
                for (String word : words) {
                    File directory = new File("data/download/" + word);
                    for (File tmp : directory.listFiles()) {
                        ImageConverter.sanitizeJpgImage(tmp);
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
                        File versionFile = files[Math.min(t, files.length - 1)];
                        String extension = Files.getFileExtension(versionFile.getName());
                        Files.copy(versionFile, new File(target, "image_" + count + "." + extension.toLowerCase()));
                    }
                }

                // MIX AUDIO, AND IMAGES

                currentProcessingVideo = "videos/mixing.mp4";

                VideoMixer mixer = new VideoMixer();
                for (int t = 0; t < 3; t++) {
                    String mixFilename = "data/result/" + sessionId + "/mix/mix_" + t + ".mp4";
                    String slideFilename = "data/result/" + sessionId + "/slide/slide_" + t + ".mp4";
                    Util.createDirForFile(mixFilename);
                    Util.createDirForFile(slideFilename);
                    mixer.mix(transcription, versionsDirectory + "/version_" + t, userAudioFilename, slideFilename, mixFilename);
                }

                //REPEAT LAT SESSION SOME TIMES
                lastSessionId = sessionId;
                lastSessionRepeatCounter = 0;
                lastSessionAlternateFlag = true;

            } catch (Exception e) {
                e.printStackTrace();
                errors = true;
            }
            System.out.println("THE END");
            running = false;
        }
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
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

        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
            if (e.getKeyCode() == NativeKeyEvent.VC_F) {
                if (player.isFullscreen())
                    player.disableFullScreen();
                else
                    player.enableFullScreen();
            }
            if (isInState(AudioScapeStates.CHOOSING_RANDOM_VIDEO) || isInState(AudioScapeStates.SHOWING_VIDEO)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_P) {
                    setState(AudioScapeStates.SHOWING_INSTRUCTIONS);
                }
            } else if (isInState(AudioScapeStates.SHOWING_INSTRUCTIONS)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_O) {
                    setState(AudioScapeStates.CHOOSING_RANDOM_VIDEO);
                } else if (e.getKeyCode() == NativeKeyEvent.VC_L) {
                    setState(AudioScapeStates.ATTENTION);
                }
            } else if (isInState(AudioScapeStates.ATTENTION)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_K) {
                    setState(AudioScapeStates.SHOWING_INSTRUCTIONS);
                }
            } else if (isInState(AudioScapeStates.RECORDING)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_K) {
                    setState(AudioScapeStates.PROCESSING);
                }
            } else if (isInState(AudioScapeStates.APPROVE)) {
                if (e.getKeyCode() == NativeKeyEvent.VC_M) {
                    setState(AudioScapeStates.APPROVE_YES);
                } else if (e.getKeyCode() == NativeKeyEvent.VC_N) {
                    setState(AudioScapeStates.APPROVE_NO);
                }
            }

        }


    }

    private boolean isInState(AudioScapeStates currentState) {
        return AudioScape.state == currentState;
    }

    private void setState(AudioScapeStates currentState) {
        System.out.println("CHANGING STATE FROM " + AudioScape.state + " TO " + currentState);
        startTime = System.currentTimeMillis();
        AudioScape.state = currentState;
    }


    public static void main(String[] args) throws Exception {
        AudioScape scape = new AudioScape();

        while (true) {
            scape.loop();
            Thread.sleep(10);
        }

    }

    static Random random = new Random();


}
