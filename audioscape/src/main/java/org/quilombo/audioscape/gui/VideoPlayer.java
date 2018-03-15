package org.quilombo.audioscape.gui;

import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class VideoPlayer {

    private JFrame frame;
    private EmbeddedMediaPlayer mediaPlayer;

    public VideoPlayer() {
        NativeLibrary.addSearchPath("libvlc", new File("tools\\vlc-2.2.8").getAbsolutePath());
        boolean found = new NativeDiscovery().discover();
        System.out.println(found);
        System.out.println(LibVlc.INSTANCE.libvlc_get_version());

        Canvas canvas = new Canvas();
        canvas.setBackground(Color.black);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.black);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(canvas, BorderLayout.CENTER);

        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setContentPane(contentPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //mediaPlayer.release();
                //mediaPlayer = null;
                System.exit(0); //calling the method is a must
            }
        });
        //frame.setLocation(50, 50);
        //frame.setSize(800, 600);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "blank cursor");
        frame.getContentPane().setCursor(blankCursor);



        frame.setMinimumSize(new Dimension(640, 480));

        enableFullScreen();

        MediaPlayerFactory factory = new MediaPlayerFactory();
        mediaPlayer = factory.newEmbeddedMediaPlayer();

        try {
            Robot robot = new Robot();
            robot.mouseMove((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        }catch (Exception e){
        }
        CanvasVideoSurface videoSurface = factory.newVideoSurface(canvas);
        mediaPlayer.setVideoSurface(videoSurface);
        mediaPlayer.setFullScreen(true);
        frame.setVisible(true);
    }

    public void enableFullScreen() {
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
    }

    public boolean isFullscreen() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getFullScreenWindow() != null;
    }

    public void disableFullScreen() {
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
        frame.setVisible(true);
    }

    public void prepare(String videoFilename) {
        System.out.println("PLAYING: " + videoFilename);
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.playMedia(videoFilename);
        //mediaPlayer.prepareMedia(videoFilename);
    }

    public void start() throws InterruptedException {
        mediaPlayer.start();
        while (!mediaPlayer.isPlaying()) { //TODO DETECT PLAYING ERRORS?
            Thread.sleep(100);
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {

        //mediaPlayer.pause();
        mediaPlayer.stop();
//        mediaPlayer.stop();

    }

    public void waitForEnd() throws InterruptedException {
        while (mediaPlayer.isPlaying()) {
            Thread.sleep(100);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        VideoPlayer player = new VideoPlayer();
        player.prepare("test.png");
        player.start();
        Thread.sleep(1500);
        player.prepare("test.png");
        player.start();
        player.waitForEnd();
        System.out.println("yes!!!");
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

}
