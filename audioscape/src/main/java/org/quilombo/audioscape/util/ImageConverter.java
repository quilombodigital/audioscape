package org.quilombo.audioscape.util;

import java.io.File;

public class ImageConverter {

    // EXPLANATION: some images from google are in strange jpg formats, and ffmpeg gets a hard time trying to recognize them,
    // for this reason e try to convert the image to a common recognizable format.

    public static void sanitizeJpgImage(File original) throws Exception {
        File temp = File.createTempFile("audioscape", ".bmp");
        int success1 = Util.execute("tools/imagemagick/magick.exe convert \"" + original.getAbsolutePath() + "\" " + temp);
        int success2 = Util.execute("tools/imagemagick/magick.exe convert \"" + temp + "\" " + original.getAbsolutePath());
        if ((success1 != 0) || (success2 != 0))
            original.delete(); //probably wrong download!
    }


}
