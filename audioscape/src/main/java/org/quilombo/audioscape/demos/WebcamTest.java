package org.quilombo.audioscape.demos;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

public class WebcamTest {

    public static void main(String[] args) throws Exception {
        Webcam webcam = Webcam.getDefault();

        Dimension[] nonStandardResolutions = new Dimension[]{
                WebcamResolution.PAL.getSize(),
                WebcamResolution.HD.getSize(),
                new Dimension(2000, 1000),
                new Dimension(1000, 500),
        };

        webcam.setCustomViewSizes(nonStandardResolutions);
        webcam.setViewSize(WebcamResolution.HD.getSize());
        //webcam.setViewSize(new Dimension(640, 480));
        webcam.open();
        Thread.sleep(1000);
        ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));
    }

}
