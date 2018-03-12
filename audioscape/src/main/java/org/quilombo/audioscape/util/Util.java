package org.quilombo.audioscape.util;

import com.google.common.io.Files;

import java.io.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class Util {

    public static File tmpFile() throws IOException {
        return File.createTempFile("audioscape", ".tmp");
    }

    public static void writeToFile(String text, String path) throws IOException {
        Files.write(text.getBytes(), new File(path));
    }

    public static void createDirForFile(String userVideoFilename) {
        new File(userVideoFilename).getParentFile().mkdirs();
    }

    public static String executeAndReturnString(String... commands) throws Exception {
        StringBuilder text = new StringBuilder();
        ProcessBuilder pb = new ProcessBuilder(commands);
        final Process p = pb.start();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            text.append(line + "\n");
        }
        return text.toString();
    }

    public static double audioDuration(String audioFilename) throws Exception {
        String result = Util.executeAndReturnString(
                "tools/ffmpeg_x64/bin/ffprobe.exe",
                "-v",
                "error",
                "-show_entries",
                "format=duration",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                audioFilename
        );
        return Double.parseDouble(result);
    }

    static Random random = new Random();

    public static File randomFileInDirectory(File file) {
        File[] files = file.listFiles();
        if (file == null || files.length == 0)
            return null;
        return files[random.nextInt(files.length)];
    }

    public static int execute(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        return exitCode;
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    public static ScheduledThreadPoolExecutor createScheduledExecutor(int number) {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(number, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        });
        return pool;
    }

    public static String pad(int number){
        return String.format("%04d", number);
    }

}
