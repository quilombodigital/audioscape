package org.quilombo.audioscape.gui;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Words {

    public static void generateCloud(String words, String outputFile) throws Exception {
        final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        List<String> text = new ArrayList<>();
        text.add(words);
        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(text);
        final Dimension dimension = new Dimension(600, 600);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
        wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile(outputFile);
    }

    public static void main(String[] args) throws Exception {
        Words.generateCloud("data/result/312297a2-1dc5-4c6b-8c38-42ec0610114c/record/transcription.txt", "word_cloud.png");
    }
}
