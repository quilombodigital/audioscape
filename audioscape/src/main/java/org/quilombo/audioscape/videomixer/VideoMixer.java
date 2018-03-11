package org.quilombo.audioscape.videomixer;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.quilombo.audioscape.util.Util;

import java.io.File;
import java.nio.file.Files;

public class VideoMixer {

    public VideoMixer() {
    }

    public void mix(String transcription, String imageDirectory, String audioFilename, String slideFilename, String outputFilename) throws Exception {

        //DISCOVER FILE LENGTH
        double audioDuration = Util.audioDuration(audioFilename);
        String[] words = transcription.trim().split(" ");
        int allWordsLength = 0;
        for (String word : words)
            allWordsLength += word.length();
        Double[] durations = new Double[words.length];
        int count = 0;
        for (String word : words) {
            durations[count] = audioDuration * word.length() / allWordsLength;
            count++;
        }

        //create input file
        File directory = new File(imageDirectory);
        File[] files = directory.listFiles();
        String input = "";
        File last = null;
        count = 0;
        for (File file : files) {
            input = input + "file " + file.getAbsolutePath().replaceAll("\\\\", "/") + "\n";
            input = input + "duration " + durations[count] + "\n";
            last = file;
            count++;
        }
        //FFMPEG BUG, repeat last entry
        input = input + "file " + last.getAbsolutePath().replaceAll("\\\\", "/") + "\n";
        input = input + "duration " + durations[count - 1];

        System.out.println(input);
        File tempInput = Util.tmpFile();
        Files.write(tempInput.toPath(), input.getBytes());


        FFmpeg ffmpeg = new FFmpeg("tools/ffmpeg_x64/bin/ffmpeg.exe");
        FFprobe ffprobe = new FFprobe("tools/ffmpeg_x64/bin/ffprobe.exe");

        FFmpegBuilder builder = new FFmpegBuilder();
        builder.addExtraArgs("-safe", "0", "-noautorotate")
                .setInput(tempInput.getAbsolutePath())
                .setFormat("concat")
                .overrideOutputFiles(true)
                .addOutput(slideFilename)
                //.setVideoFilter("hflip,scale=1920:1080")
                .setVideoFilter("scale=1920:1080")
                .setVideoCodec("libx264")
                //.setVideoPixelFormat("yuv420p")
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        for (String tmp : builder.build())
            System.out.print(tmp + " ");
        System.out.println();
        executor.createJob(builder).run();

        builder = new FFmpegBuilder();
        builder
                .addInput(slideFilename)

                .addInput(audioFilename)

                .overrideOutputFiles(true)
                .addOutput(outputFilename)
                .addExtraArgs("-c:v", "copy")
                .addExtraArgs("-map", "0:v:0")
                .addExtraArgs("-map", "1:a:0")
                .addExtraArgs("-c:a", "aac")
                .addExtraArgs("-b:a", "192k")
                //.addExtraArgs("-shortest")
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();
        executor = new FFmpegExecutor(ffmpeg, ffprobe);
        for (String tmp : builder.build())
            System.out.print(tmp + " ");
        System.out.println();
        executor.createJob(builder).run();
    }


    public static void main(String[] args) throws Exception {


    }
}
