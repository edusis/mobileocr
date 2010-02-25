package edu.washington.cs.main;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

class ConnectionHandler extends Thread {
	
	Socket client;
	DataInputStream is;
	DataOutputStream os;

	public ConnectionHandler(Socket s) { // constructor
		System.out.println("[  OK  ] Creating socket handler");
		
		client = s;
		try {
			is = new DataInputStream(client.getInputStream());
			os = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			System.out.println("IOException: "+e.toString());
		}
	
		this.start();
	}

	public void run() {
		try {
			System.out.println("[  OK  ] Thread running.");
			int readByteCount = 0;
			ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
			FileOutputStream fos = new FileOutputStream(new File("test.jpg"));
			byte[] buffer = new byte[4096];
			while((readByteCount = is.read(buffer)) != -1)
			{
				imageBuffer.write(buffer, 0, readByteCount);
				fos.write(buffer, 0, readByteCount);
			}
			
			fos.close();
			//TODO: preprocessing here with imageBuffer.toArray()
			
			//package as jpeg, send to ocr server
			//byte[] rgbData = new byte[imageBuffer.size()];
			//decodeYUV(rgbData, imageBuffer.toByteArray(),240, 320);

			//toRGB565(imageBuffer.toByteArray(), 240, 320, rgbData);
			
			/*System.out.println("[  OK  ] Creating buffered image: " + imageBuffer.toByteArray().length);
			BufferedImage img = toImage(240, 320, rgbData);
			System.out.println("[  OK  ] BufferedImage created");
			*/
			//test, write to file
			/*File testImage = new File("testimage.jpg");
			ImageIO.write(img, "jpg", testImage);
			
			WeOCRClient weOCRClient = new WeOCRClient(null);
	        String result = weOCRClient.doOCR(img);        
	        */
		} catch (Exception e){
			System.out.println("Exception: " + e.toString());
		}
	}
	
	private static BufferedImage toImage(int w, int h, byte[] data) {
	    DataBuffer buffer = new DataBufferByte(data, w*h);
	 
	    int pixelStride = 4; //assuming r, g, b, skip, r, g, b, skip...
	    int scanlineStride = 4*w; //no extra padding   
	    int[] bandOffsets = {0, 1, 2}; //r, g, b
	    WritableRaster raster = Raster.createInterleavedRaster(buffer, w, h, scanlineStride, pixelStride, bandOffsets, null);
	 
	    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	    boolean hasAlpha = false;
	    boolean isAlphaPremultiplied = false;
	    int transparency = Transparency.OPAQUE;
	    int transferType = DataBuffer.TYPE_BYTE;
	    ColorModel colorModel = new ComponentColorModel(colorSpace, hasAlpha, isAlphaPremultiplied, transparency, transferType);
	 
	    return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
	}
	
	private void decodeYUV(int[] out, byte[] fg, int width, int
    		height) throws NullPointerException, IllegalArgumentException {
    			final int sz = width * height;
    			if(out == null) throw new NullPointerException("buffer 'out' is null");
    			if(out.length < sz) throw new IllegalArgumentException("buffer 'out' size " + out.length + " < minimum " + sz);
    			if(fg == null) throw new NullPointerException("buffer 'fg' is null");
    			if(fg.length < sz) throw new IllegalArgumentException("buffer 'fg'	size " + fg.length + " < minimum " + sz * 3/ 2);
    			int i, j;
    			int Y, Cr = 0, Cb = 0;
    			for(j = 0; j < height; j++) {
    				int pixPtr = j * width;
    				final int jDiv2 = j >> 1;
    				for(i = 0; i < width; i++) {
    					Y = fg[pixPtr]; if(Y < 0) Y += 255;
    					if((i & 0x1) != 1) {
    						final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
    						Cb = fg[cOff];
    						if(Cb < 0) Cb += 127; else Cb -= 128;
    						Cr = fg[cOff + 1];
    						if(Cr < 0) Cr += 127; else Cr -= 128;
    					}
    					int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
    					if(R < 0) R = 0; else if(R > 255) R = 255;
    					int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >>
    		3) + (Cr >> 4) + (Cr >> 5);
    					if(G < 0) G = 0; else if(G > 255) G = 255;
    					int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
    					if(B < 0) B = 0; else if(B > 255) B = 255;
    					out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
    				}
    			}
    		}
	
	private void toRGB565(byte[] yuvs, int width, int height, byte[] rgbs) {
	    //the end of the luminance data
	    final int lumEnd = width * height;
	    //points to the next luminance value pair
	    int lumPtr = 0;
	    //points to the next chromiance value pair
	    int chrPtr = lumEnd;
	    //points to the next byte output pair of RGB565 value
	    int outPtr = 0;
	    //the end of the current luminance scanline
	    int lineEnd = width;

	    while (true) {

	        //skip back to the start of the chromiance values when necessary
	        if (lumPtr == lineEnd) {
	            if (lumPtr == lumEnd) break; //we've reached the end
	            //division here is a bit expensive, but's only done once per scanline
	            chrPtr = lumEnd + ((lumPtr  >> 1) / width) * width;
	            lineEnd += width;
	        }

	        //read the luminance and chromiance values
	        final int Y1 = yuvs[lumPtr++] & 0xff; 
	        final int Y2 = yuvs[lumPtr++] & 0xff; 
	        final int Cr = (yuvs[chrPtr++] & 0xff) - 128; 
	        final int Cb = (yuvs[chrPtr++] & 0xff) - 128;
	        int R, G, B;

	        //generate first RGB components
	        B = Y1 + ((454 * Cb) >> 8);
	        if(B < 0) B = 0; else if(B > 255) B = 255; 
	        G = Y1 - ((88 * Cb + 183 * Cr) >> 8); 
	        if(G < 0) G = 0; else if(G > 255) G = 255; 
	        R = Y1 + ((359 * Cr) >> 8); 
	        if(R < 0) R = 0; else if(R > 255) R = 255; 
	        //NOTE: this assume little-endian encoding
	        rgbs[outPtr++]  = (byte) (((G & 0x3c) << 3) | (B >> 3));
	        rgbs[outPtr++]  = (byte) ((R & 0xf8) | (G >> 5));

	        //generate second RGB components
	        B = Y2 + ((454 * Cb) >> 8);
	        if(B < 0) B = 0; else if(B > 255) B = 255; 
	        G = Y2 - ((88 * Cb + 183 * Cr) >> 8); 
	        if(G < 0) G = 0; else if(G > 255) G = 255; 
	        R = Y2 + ((359 * Cr) >> 8); 
	        if(R < 0) R = 0; else if(R > 255) R = 255; 
	        //NOTE: this assume little-endian encoding
	        rgbs[outPtr++]  = (byte) (((G & 0x3c) << 3) | (B >> 3));
	        rgbs[outPtr++]  = (byte) ((R & 0xf8) | (G >> 5));
	    }
	}
}
