package org.quilombo.audioscape.util;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

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

    static Random random= new Random();

    public static File randomFileInDirectory(File file){
        File[] files = file.listFiles();
        if (file==null)
            return null;
        return files[random.nextInt(files.length)];
    }

}
