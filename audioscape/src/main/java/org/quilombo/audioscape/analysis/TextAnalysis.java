package org.quilombo.audioscape.analysis;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class TextAnalysis {

    private TextAnalysisConfig textAnalysisConfig;

    public TextAnalysis(TextAnalysisConfig textAnalysisConfig) {
        this.textAnalysisConfig = textAnalysisConfig;
    }

    public String getEmotion(int wordsLimit, String message) throws Exception {
        Documents documents = textToDocuments(wordsLimit, message);
        String result = doRequest(textAnalysisConfig.sentimentPath, documents);
        return result;
    }

    public String getKeyPhrases(int wordsLimit, String message) throws Exception {
        Documents documents = textToDocuments(wordsLimit, message);
        String result = doRequest(textAnalysisConfig.keyPhrasePath, documents);
        return result;
    }

    private Documents textToDocuments(int wordsLimit, String message) {
        String[] words = message.split(" ");
        Documents documents = new Documents();
        String currentText = "";
        int wordIndex = 0;
        int phraseIndex = 0;
        for (int i = 0; i < words.length; i += 1) {
            currentText = currentText + words[i] + " ";
            wordIndex++;
            if ((wordIndex == wordsLimit) || (wordIndex == (words.length - 1))) {
                phraseIndex++;
                System.out.println("checking text: " + currentText);
                documents.add("" + phraseIndex, "pt-PT", currentText);
                currentText = "";
                wordIndex = 0;
            }
        }
        return documents;
    }

    private String doRequest(String path, Documents documents) throws Exception {
        String text = new Gson().toJson(documents);
        byte[] encoded_text = text.getBytes("UTF-8");

        URL url = new URL(textAnalysisConfig.host + path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/json");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", textAnalysisConfig.accessKey);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(encoded_text, 0, encoded_text.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    public static void main(String[] args) throws Exception {
        TextAnalysis tte = new TextAnalysis(new TextAnalysisConfig());
        String text = "quem deu a ideia foram eles então eles têm que ajudar porque não adianta botar fogo em tudo e depois chegar e falar agora vocês façam tudo vocês podem pensar em tudo mas não dão o material para trabalhar então ele devia ter pensado nisso antes e eles deviam eles deviam ter limitado a gente para a gente saber que tinha limites";
        String response = tte.getEmotion(10, text);
        System.out.println(response);
        //response = tte.getKeyPhrases(10, text);
        //System.out.println(response);
    }

}
