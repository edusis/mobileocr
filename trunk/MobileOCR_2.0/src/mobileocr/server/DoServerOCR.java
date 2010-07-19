package mobileocr.server;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import mobileocr.tts.TTSHandler;

import android.util.Log;

/*
 * This class handles the server connection. It sends the jpeg byte[]
 * to the server for OCR processing.
 */

public class DoServerOCR {

	public static String getOCRResponse(byte[] data) {

		final String TAG = "DoServerOCR";

		// Server variables
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;

		String fileName = "doOCR.jpeg";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";
		String responseFromServer = "";
		String urlString = "http://abstract.cs.washington.edu/~koemon/mobileocr/upload.php";
		//String urlString = "http://mobileocr.cs.washington.edu/process/upload.php";

		try {
			Log.i(TAG,"Beginning client request");

			InputStream inputStream = new ByteArrayInputStream(data);

			//Open a URL connection to the server
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);

			//Use a multipart POST
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			dos = new DataOutputStream( conn.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
			dos.writeBytes(lineEnd);

			Log.i(TAG,"Headers are written");

			//Create a buffer of maximum size
			bytesAvailable = inputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			//Read file and write it into form...
			bytesRead = inputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = inputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = inputStream.read(buffer, 0, bufferSize);
			}

			//Send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			//Close streams
			Log.i(TAG,"File is written on server");
			inputStream.close();
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex) {
			Log.e(TAG, "MalformedURLException: " + ex.getMessage(), ex);
			responseFromServer = "There was an error in talking to the Mobile OCR servers, please try again or wait for the server to become available.";
		}
		catch (IOException ioe) {
			Log.e(TAG, "IOException: " + ioe.getMessage(), ioe);
			responseFromServer = "There was an error in connecting to the WeOCR servers, please try again or wait for the server to become available.";
		}

		//Read the server response
		try {
			inStream = new DataInputStream ( conn.getInputStream());
			String str = "";
			while ((str = inStream.readLine()) != null) {
				responseFromServer += "\n" + str;
			}
			inStream.close();
		}
		catch (IOException ioex) {
			Log.e(TAG, "IOException: " + ioex.getMessage(), ioex);
			responseFromServer = "There was an error in connecting to the WeOCR servers, please try again or wait for the server to become available.";
		}

		Log.i(TAG,"Server Response: " + responseFromServer.trim());
		return responseFromServer.trim();
	}

}