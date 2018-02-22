package org.quilombo.audioscape;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.quilombo.audioscape.video.AudioConfig;
import org.quilombo.audioscape.video.VideoConfig;

import java.io.File;

public class AudioscapeConfig {

    public AudioscapeConfig() {
    }

    public VideoConfig recorderVideo = new VideoConfig();
    public AudioConfig recorderAudio = new AudioConfig();


    public static AudioscapeConfig load() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("data/audioscape.json"), AudioscapeConfig.class);
    }

}
