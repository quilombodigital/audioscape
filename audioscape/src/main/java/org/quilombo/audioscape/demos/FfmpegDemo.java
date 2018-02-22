package org.quilombo.audioscape.demos;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.quilombo.audioscape.util.Util;

import java.io.File;
import java.nio.file.Files;

public class FfmpegDemo {


    public static void main(String[] args) throws Exception {


        //ffmpeg -f concat -i input.txt -vsync vfr -pix_fmt yuv420p output.mp4


        //create input file
        File directory = new File("result/try_1");
        File[] files = directory.listFiles();
        String input = "";
        File last = null;
        for (File file : files) {
            input = input + "file " + file.getAbsolutePath().replaceAll("\\\\", "/") + "\n";
            input = input + "duration 0.5\n";
            last = file;
        }
        //input=input+"file "+last.getAbsolutePath().replaceAll("\\\\","/"); //ffmpeg quirk, repeat last
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
                .addOutput("output.mp4")
                .setVideoFilter("hflip,scale=1920:1080")
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
                .addInput("output.mp4")

                .addInput("teste.wav")

                .overrideOutputFiles(true)
                .addOutput("output_with_audio.mp4")
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
}
