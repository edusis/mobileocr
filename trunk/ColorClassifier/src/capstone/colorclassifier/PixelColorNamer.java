package capstone.colorclassifier;

/*
 * PixelColorNamer
 * by Will Johnson  
 * modified by Shiri Azenkot 
 * 
 * This class determines the color for a given pixel. 
 * When you have the RGB value for a pixel, call on the classifyPixel(...)
 * function to get an integer representation of the pixel color. 
 * You may then call on getColorName(...) to get the color name as
 * a string.
 *
 * You may modify this code if you'd like. We're providing it simply to 
 * allow you to focus on the other aspects of the application development.
 *
 * Note that this 'class' is kind of pseudo-singleton. All state is static.
 */
public class PixelColorNamer {
	
	// Color constant declarations.
	// Note that dropping the ones digit converts a color
	// the corresponding canonical color.
	public static final int WHITE = 0;
	public static final int GRAY = 10;
	public static final int BLACK = 20;
	public static final int RED = 30;
	public static final int MAROON = 31;
	public static final int GREEN = 40;
	public static final int OLIVE = 41;
	public static final int CHARTREUSE = 42;
	public static final int EMERALD = 43;
	public static final int SPRING_GREEN = 44;
	public static final int BLUE = 50;
	public static final int NAVY_BLUE = 51;
	public static final int CYAN = 52;
	public static final int TEAL = 53;
	public static final int YELLOW = 60;
	public static final int TRADITIONAL_CHARTREUSE = 61;
	public static final int BEIGE = 62;
	public static final int ORANGE = 70;
	public static final int BROWN = 100;
	public static final int PURPLE = 80;
	public static final int MAGENTA = 81;
	public static final int VIOLET = 82;
	public static final int MAGENTA_DYE = 83;
	public static final int PINK = 90;
	public static final int ROSE = 91;
	
	public static String getColorName(int color) {
		switch(color) {
		case WHITE:
			return "white";
		case GRAY:
			return "gray";
		case BLACK:
			return "black";
		case RED:
			return "red";
		case MAROON:
			return "maroon";
		case GREEN:
			return "green";
		case OLIVE:
			return "olive";
		case CHARTREUSE:
			return "chartreuse";
		case EMERALD:
			return "emerald";
		case SPRING_GREEN:
			return "spring green";
		case BLUE:
			return "blue";
		case NAVY_BLUE:
			return "navy blue";
		case CYAN:
			return "cyan";
		case TEAL:
			return "teal";
		case YELLOW:
			return "yellow";
		case TRADITIONAL_CHARTREUSE:
			return "traditional chartreuse";
		case BEIGE:
			return "beige";
		case ORANGE:
			return "orange";
		case BROWN:
			return "brown";
		case PURPLE:
			return "purple";
		case MAGENTA:
			return "magenta";
		case VIOLET:
			return "violet";
		case MAGENTA_DYE:
			return "magenta_dye";
		case PINK:
			return "pink";
		case ROSE:
			return "rose";
		default:
			return "unknown";
			
		}
	}
	
	// Set the HSV (Hue, Saturation, and value) for a given RGB value.
	// Reference: "HSL and HSV" in Wikipedia, http://en.wikipedia.org/wiki/HSL_and_HSV
	private static void calculateHSV(int red, int green, int blue, int [] hsv) {

		int [] rgb = new int[3];
		rgb[0] = red;
		rgb[1] = green;
		rgb[2] = blue;
		int max = Math.max(rgb[0],rgb[1]);
		max = Math.max(max,rgb[2]);
		int min = Math.min(rgb[0],rgb[1]);
		min = Math.min(min,rgb[2]);
		if(max == min)
			hsv[0] = 0;
		else if(max == rgb[0])
			hsv[0] = 60*(rgb[1] - rgb[2])/(max - min) + 360;
		else if(max == rgb[1])
			hsv[0] = 60*(rgb[2] - rgb[0])/(max - min) + 120;
		else
			hsv[0] = 60*(rgb[0] - rgb[1])/(max - min) + 240;
		hsv[0] %= 360;
		if(max == 0)
			hsv[1] = 0;
		else
			hsv[1] = 100*(max - min)/max;
		hsv[2] = max*100/256;
	}

	// Return the UV value for a RGB value.
	// This functional actually calculates something proportional to the magnitude squared
	// of the UV component.  It measures the extent to which the color was chromatic, 
	// as opposed to white/black/gray, for all of which this function returns zero.
	private static double calculateUV(int red, int green, int blue) {
		int [] rgb = new int[3];
		rgb[0] = red;
		rgb[1] = green;
		rgb[2] = blue;
		double total = 0;
		double totalsq = 0;
		for(int i = 0; i < 3; i++) {
			double x = rgb[i];
			total += x;
			totalsq += x*x;
		}
		total /= 3;
		totalsq /= 3;
		totalsq -= total*total;
		return totalsq;
	}

	// Return the color for a single pixel, given
	// the pixel's RGB values. You can get a string representing
	// the color name by calling getColorName on the return value.
	public static int classifyPixel(int red, int green, int blue) {
		int [] hsv = new int[3];
		calculateHSV(red,green,blue,hsv);
		int hue = hsv[0];
		int sat = hsv[1];
		int val = hsv[2];
		int chrome = (int) calculateUV(red, green, blue);
		
		if(chrome < 100) {
			if(val > 75)
				return WHITE;
			if(val < 26)
				return BLACK;
			return GRAY;
		}

		if(hue > 160 && hue <= 210) {
			if(val > 60)
				return CYAN;
			else
				return TEAL;
		}
		if(hue > 210 && hue <= 260) {
			if(val > 60)
				return BLUE;
			else
				return NAVY_BLUE;
		}
		if((hue > 300 || hue <= 15) && sat < 60 && val > 60)
			return PINK;
		if(hue > 260 && hue <=347) {
			if(val > 75) {
				if(hue >= 330)
					return ROSE;
				else
					return MAGENTA;
			}
			if(hue < 285)
				return VIOLET;
			if(hue > 315)
				return MAGENTA_DYE;
			return PURPLE;
		}
		if(hue > 15 && hue <= 45 && val < 70 || sat < 50) {
			return BROWN;
		}
		if(hue > 347 || hue <= 15) {
			if(val > 60)
				return RED;
			else
				return MAROON;
		}
		if(hue > 15 && hue <= 45) {
			return ORANGE;
		}
		if(hue > 45 && hue <= 80 && val <= 70)
			return OLIVE;
		if(hue > 45 && hue <= 80) {
			if(sat < 40 && hue <= 67)
				return BEIGE;
			if(hue > 67)
				return TRADITIONAL_CHARTREUSE;
			return YELLOW;
		}
		if(hue > 80 && hue <= 100) {
			return CHARTREUSE;
		}
		if(hue > 100 && hue <= 160) {
			if(hue >= 140 && val > 75) {
				if(sat < 70)
					return EMERALD;
				else
					return SPRING_GREEN;
			}
			return GREEN;
		}
		return -1;
	}
}
