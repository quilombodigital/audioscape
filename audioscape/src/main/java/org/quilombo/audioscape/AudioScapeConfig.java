package org.quilombo.audioscape;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.quilombo.audioscape.video.AudioConfig;
import org.quilombo.audioscape.video.VideoConfig;

import java.io.File;

public class AudioScapeConfig {

    public AudioScapeConfig() {
    }

    public VideoConfig recorderVideo = new VideoConfig();
    public AudioConfig recorderAudio = new AudioConfig();

    public int recordInterval;
    public int minRecordTime;
    public int maxRecordTime;

    public static AudioScapeConfig load() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("data/audioscape.json"), AudioScapeConfig.class);
    }

}
