package org.ilanguage.fielddbsessionrecorder;

public class Datum {
	String _id;
	String utterance;
	String morphemes;
	String gloss;
	String translation;
	String judgement;
	String comments;

	public Datum(String _id, String utterance, String morphemes, String gloss,
			String translation, String judgement, String comments) {
		this._id = _id;
		this.utterance = utterance;
		this.morphemes = morphemes;
		this.gloss = gloss;
		this.translation = translation;
		this.judgement = judgement;
		this.comments = comments;
	}

	public String getInfo() {
		return this._id + ", " + this.utterance + ", " + this.morphemes + ", " + this.gloss + ", " + this.translation + ", " + this.judgement + ", " + this.comments;
	}
}
