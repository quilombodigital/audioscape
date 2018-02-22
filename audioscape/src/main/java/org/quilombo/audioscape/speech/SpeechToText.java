package org.quilombo.audioscape.speech;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.quilombo.audioscape.video.AudioConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SpeechToText {
    SpeechClient speech;

    public SpeechToText() {
    }

    public String convert(AudioConfig audioConfig, String fileName) throws Exception {
        System.out.println("Started Audio to Text");
        speech = SpeechClient.create();
        Path path = Paths.get(fileName);
        byte[] data = Files.readAllBytes(path);
        ByteString audioBytes = ByteString.copyFrom(data);

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(audioConfig.samplerate)
                .setLanguageCode("pt-BR")
                .build();
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(audioBytes)
                .build();

        RecognizeResponse response = speech.recognize(config, audio);
        List<SpeechRecognitionResult> results = response.getResultsList();
        speech.close();

        StringBuilder tmp = new StringBuilder();
        for (SpeechRecognitionResult result : results) {
            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
            System.out.printf("Transcription: %s%n", alternative.getTranscript());
            tmp.append(alternative.getTranscript());
            tmp.append(" ");
        }
        System.out.println("Finished Audio to Text");
        return tmp.toString().trim();
    }


}
