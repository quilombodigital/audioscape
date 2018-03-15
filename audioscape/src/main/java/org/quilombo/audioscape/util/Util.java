package org.quilombo.audioscape.util;

import com.google.common.io.Files;

import java.io.*;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public class Util {

    public static File tmpFile() throws IOException {
        return tmpFile("tmp");
    }

    public static File tmpFile(String sufix) throws IOException {
        return File.createTempFile("audioscape", "." + sufix);
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

    static SecureRandom random = new SecureRandom();

    public static File randomFileInDirectory(File file) {
        File[] files = file.listFiles();
        if (file == null || files.length == 0)
            return null;
        return files[random.nextInt(files.length)];
    }

    public static ExecuteResult execute(String command) throws Exception {
        System.out.println("Executing command: " + command);
        Process process = Runtime.getRuntime().exec(command);
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), x -> {
                    //System.out.println(x);
                    output.append(x + '\n');
                });
        StreamGobbler streamGobblerError =
                new StreamGobbler(process.getErrorStream(), x -> {
                    //System.out.println(x);
                    error.append(x + '\n');
                });
        Thread o = new Thread(streamGobbler);
        o.setDaemon(true);
        Thread e = new Thread(streamGobblerError);
        e.setDaemon(true);
        o.start();
        e.start();
        // Executors.newSingleThreadExecutor().submit(streamGobbler);
        // Executors.newSingleThreadExecutor().submit(streamGobblerError);
        int exitCode = process.waitFor();
        process.destroyForcibly();
        ExecuteResult result = new ExecuteResult();
        result.exitCode = exitCode;
        result.output = output.toString();
        result.error = error.toString();
        return result;
    }

    public static class ExecuteResult {
        public int exitCode;
        public String output;
        public String error;
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

    public static String pad(int number) {
        return String.format("%04d", number);
    }

}
