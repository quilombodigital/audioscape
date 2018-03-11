package org.quilombo.audioscape.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class TextAnalysisConfig {
    public String accessKey;
    public String host;
    public String sentimentPath;
    public String keyPhrasePath;

    public TextAnalysisConfig() {
    }

    public static TextAnalysisConfig load() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("keys/text_analysis.json"), TextAnalysisConfig.class);
    }
}
