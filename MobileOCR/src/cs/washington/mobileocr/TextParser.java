package cs.washington.mobileocr;

/*
 * Team Sparkplugs (Josh Scotland and Hussein Yapit)
 * This class parses the text into an array that will be read using TTS
 */

public class TextParser {
	
	/* 
	 * Given sentences, will parse them.
	 */
	public static String[] sentenceParse(String passedString) {
		String delims = "[.?!]+";
		String[] tokens = passedString.split(delims);
		return tokens;
	}
	
	/*
	 * Given a sentence, will parse out the words
	 */
	public static String[] wordParse(String passedString) {
		String delims = "[ ]+";
		String[] tokens = passedString.split(delims);
		return tokens;
	}
}
