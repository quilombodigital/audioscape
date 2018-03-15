package org.quilombo.audioscape.audio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.quilombo.audioscape.util.Util;

import java.io.File;

public class AudioTricks {

    static ObjectMapper mapper = new ObjectMapper();

    public static void normalize(String filename, String targetFilename) throws Exception {

        Util.ExecuteResult result = Util.execute(String.format("tools\\ffmpeg_x64\\bin\\ffmpeg.exe -i \"" +
                "%s\" -af loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json -f null -", filename));
        if (result.exitCode == 0) {
            String response = result.error.substring(result.error.lastIndexOf(']') + 1);
            //System.out.println(response);
            NormalizeJson info = mapper.readValue(response, NormalizeJson.class);

            result = Util.execute(String.format("tools\\ffmpeg_x64\\bin\\ffmpeg.exe -y -i \"%s\" -af loudnorm=I=-16:TP=-1.5:LRA=11" +
                            ":measured_I=%s" +
                            ":measured_LRA=%s" +
                            ":measured_TP=%s" +
                            ":measured_thresh=%s" +
                            ":offset=%s" +
                            ":linear=true:print_format=summary -ar 22050 \"%s\"",
                    filename, info.input_i, info.input_lra, info.input_tp, info.input_thresh, info.target_offset, targetFilename));
        } else {
            System.out.println(result.error);
        }
    }

    public static void trim(String originalFilename, String targetFilename) throws Exception {
        Util.ExecuteResult result = Util.execute(String.format("tools\\sox-14-4-2\\sox.exe \"%s\" \"%s\" silence 1 0.1 1%% reverse silence 1 0.1 1%% reverse", originalFilename, targetFilename));
    }


    public static class NormalizeJson {
        public String input_i;
        public String input_tp;
        public String input_lra;
        public String input_thresh;
        public String output_i;
        public String output_tp;
        public String output_lra;
        public String output_thresh;
        public String normalization_type;
        public String target_offset;
    }


    public static void main(String[] args) throws Exception {
        File results = new File("data/result");
        File[] sessions = results.listFiles();
        for (File session : sessions) {
            File original = new File(session, "record/record.wav");
            if (original.exists()) {
                File target = new File(session, "record/record_normalized.wav");
                if (target.exists())
                    target.delete();
                System.out.println("Normalizing " + original.getAbsolutePath() + " to " + target.getAbsolutePath());
                AudioTricks.normalize(original.getAbsolutePath(), target.getAbsolutePath());
                AudioTricks.trim(target.getAbsolutePath(), new File(session, "record/record_trimmed.wav").getAbsolutePath());
                File[] mixes = new File(session, "mix").listFiles();
                for (File mix : mixes) {
                    File tmp = Util.tmpFile("mp4");
                    String originalMix = mix.getAbsolutePath();
                    AudioTricks.normalize(originalMix, tmp.getAbsolutePath());
                    if (mix.delete())
                        Files.copy(tmp, new File(originalMix));
                }
            } else {
                System.out.println("RECORD " + original.getAbsolutePath() + " DOES NOT EXIST!");
            }
        }

    }


}
