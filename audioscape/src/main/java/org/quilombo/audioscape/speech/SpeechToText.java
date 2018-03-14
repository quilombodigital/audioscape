package org.quilombo.audioscape.speech;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.quilombo.audioscape.video.AudioConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SpeechToText {

    public SpeechToText() throws Exception {
    }

    public String convert(AudioConfig audioConfig, String fileName) throws Exception {
        System.out.println("Started Audio to Text " + fileName);

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
        StringBuilder tmp = new StringBuilder();
        try (SpeechClient speech = SpeechClient.create()) {
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();
            System.out.println("TTS result count" + response.getResultsCount());
            for (SpeechRecognitionResult result : results) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
                if (alternative.getTranscript().length() > 5) {
                    tmp.append(alternative.getTranscript());
                    tmp.append(" ");
                }
            }
        }
        //speech.close();
        System.out.println("Finished Audio to Text");
        String transcript = tmp.toString().replace("\n", "").replace("\r", "");
        System.out.println(transcript);
        return transcript;
    }


}
