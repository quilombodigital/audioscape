package org.quilombo.audioscape.analysis;

class Document {
    public String id, language, text;
    public double score;

    public Document(String id, String language, String text) {
        this.id = id;
        this.language = language;
        this.text = text;
    }
}