package edu.washington.cs.main;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
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
			int readByteCount = 0;
			ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			while((readByteCount = is.read(buffer)) != -1)
			{
				imageBuffer.write(buffer, 0, readByteCount);
			}
			
			//TODO: preprocessing here with imageBuffer.toArray()
			
			//package as jpeg, send to ocr server
			
			BufferedImage img = toImage(320, 480, imageBuffer.toByteArray());
			
			//test, write to file
			File testImage = new File("testimage.jpg");
			ImageIO.write(img, "jpg", testImage);
			
			WeOCRClient weOCRClient = new WeOCRClient(null);
	        String result = weOCRClient.doOCR(img);        
	        
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
	 
	    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.TYPE_YCbCr);
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
}
