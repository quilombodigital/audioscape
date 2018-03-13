package org.quilombo.audioscape.util;

import java.io.File;

public class ImageConverter {

    // EXPLANATION: some images from google are in strange jpg formats, and ffmpeg gets a hard time trying to recognize them,
    // for this reason e try to convert the image to a common recognizable format.

    public static void sanitizeJpgImage(File original, int counter) throws Exception {
        File temp = File.createTempFile("audioscape", ".bmp");
        Util.execute("tools/imagemagick/magick.exe convert \"" + original.getAbsolutePath() + "\" " + temp);
        String targetname = new File(original.getParentFile(),"img_" + Util.pad(counter)+".jpg").getAbsolutePath();
        System.out.println("sanitizing " + original.getAbsolutePath() + " to " + targetname);
        original.delete();
        Util.execute("tools/imagemagick/magick.exe convert \"" + temp + "\" " + targetname);
    }


}
