package org.quilombo.audioscape.demos;

import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import javax.swing.*;
import java.awt.*;

public class PlayVideoExample {

    public static void main(String[] args) {
        NativeLibrary.addSearchPath("libvlc", "F:\\mostradearte\\audioscape\\tools\\vlc-2.2.8");
        boolean found = new NativeDiscovery().discover();
        System.out.println(found);
        System.out.println(LibVlc.INSTANCE.libvlc_get_version());
        //.setProperty("jna.library.path", "F:\\mostradearte\\audioscape\\tools\\vlc-2.2.8");
        //System.setProperty("VLC_PLUGIN_PATH", "");
        Canvas canvas = new Canvas();
        canvas.setBackground(Color.black);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.black);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(canvas, BorderLayout.CENTER);

        JFrame frame = new JFrame("Capture");
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(50, 50);
        frame.setSize(800, 600);

        MediaPlayerFactory factory = new MediaPlayerFactory();
        EmbeddedMediaPlayer mediaPlayer = factory.newEmbeddedMediaPlayer();

        CanvasVideoSurface videoSurface = factory.newVideoSurface(canvas);
        mediaPlayer.setVideoSurface(videoSurface);

        // Reproduce el vídeo.
        frame.setVisible(true);

        mediaPlayer.prepareMedia("output_with_audio.mp4");
        mediaPlayer.setRepeat(true);
        mediaPlayer.start();
    }
}
