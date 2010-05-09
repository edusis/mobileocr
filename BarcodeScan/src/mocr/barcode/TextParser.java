
package mocr.barcode;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class is a simple text parser that parses sentences and words
 * out the text received by OCR. 
 */

import android.util.Log;

public class TextParser {
	
	private static final String TAG = "TextParser";
	
	private static String cleanerPassedString = "";
	
	//Parses a block of text into sentences based on the punctuation
	public static String[] sentenceParse(String passedString) {
		String delims = "[.?!]+|\n";
		String[] tokens = passedString.trim().split(delims);
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = tokens[i].trim();
		Log.d(TAG, "SentenceParse = " + arrayToString(tokens));
		return tokens;
	}
	
	//Parses a block of text into words based on the spaces
	public static String[] wordParse(String[] passedSentences) {
		String delims = "[ ]+";
		cleanerPassedString = passedSentences[0].trim();
		for (int i = 1; i < passedSentences.length; i++)
			cleanerPassedString += " " + passedSentences[i];
		String[] tokens = cleanerPassedString.trim().split(delims);
		Log.d(TAG, "WordParse = " + arrayToString(tokens));
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
        Log.d(TAG, "WordCount[0] = " + wordsInSentence[0]);
        return wordsInSentence;
    }
	
    //Helper method for debugging. Displays what is in the array.
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
