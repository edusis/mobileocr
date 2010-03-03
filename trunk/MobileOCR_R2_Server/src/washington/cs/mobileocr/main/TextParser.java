package washington.cs.mobileocr.main;

import android.util.Log;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This class parses the text into an array that will be read using TTS
 */

public class TextParser {
	
	private static final String TAG = "TextParser";
	private static String cleanerPassedString = "";
	
	//Parses a block of text into sentences by the punctuation
	public static String[] sentenceParse(String passedString) {
		String delims = "[.?!]+|\n";
		String[] tokens = passedString.trim().split(delims);
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = tokens[i].trim();
		Log.e(TAG, "SentenceParse = " + arrayToString(tokens));
		return tokens;
	}
	
	//Parses a block of text into words by the spaces
	public static String[] wordParse(String[] passedSentences) {
		String delims = "[ ]+";
		cleanerPassedString = passedSentences[0].trim();
		for (int i = 1; i < passedSentences.length; i++)
			cleanerPassedString += " " + passedSentences[i];
		String[] tokens = cleanerPassedString.trim().split(delims);
		Log.e(TAG, "WordParse = " + arrayToString(tokens));
		return tokens;
	}
	
	//Counts the number of words in each of the sentences
    public static int[] countWordsInSentence(String[] passedSentences) {
        String delims = "[ ]+";
        int count = 0;
        int[] wordsInSentence = new int[passedSentences.length];
        for (int i = 0; i < wordsInSentence.length; i++) { 
                count += passedSentences[i].trim().split(delims).length;
                wordsInSentence[i] = count;
        }
        Log.e(TAG, "WordCount[0] = " + wordsInSentence[0]);
        return wordsInSentence;
    }
	
	public static String arrayToString(String[] a) {
	    String result = "";
	    if (a.length > 0) {
	        result = a[0];
	        for (int i=1; i<a.length; i++) {
	            result = result + "," + a[i];
	        }
	    }
	    return result;
	}
}
