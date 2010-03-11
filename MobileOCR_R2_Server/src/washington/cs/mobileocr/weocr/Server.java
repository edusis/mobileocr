package washington.cs.mobileocr.weocr;

/**
 * Copyright 2010, Josh Scotland & Hussein Yapit
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided you follow the BSD license.
 * 
 * 
 * This class handles the server connection. It sends a bitmap image
 * and receives text in return.
 * TODO Set timeout limit
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.util.Log;

public class Server {

	public static String doFileUpload(Bitmap b) {

		final String TAG = "Server";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String fileName = "doOCR.png";

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;
		String responseFromServer = "";
		String urlString = "http://abstract.cs.washington.edu/~koemon/mobileocr/upload.php";
		
		try {
			//Client Request
			Log.d(TAG,"Beginning client request");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] bmpBytes = baos.toByteArray(); 
			InputStream inputStream = new ByteArrayInputStream(bmpBytes);

			//Open a URL connection to the Servlet
			URL url = new URL(urlString);

			//Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();

			//Allow inputs and outputs
			conn.setDoInput(true);
			conn.setDoOutput(true);

			//Don't use a cached copy.
			conn.setUseCaches(false);

			//Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			dos = new DataOutputStream( conn.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
			dos.writeBytes(lineEnd);

			Log.d(TAG,"Headers are written");

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
			Log.d(TAG,"File is written on server");
			inputStream.close();
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex) {
			Log.e(TAG, "error: " + ex.getMessage(), ex);
		}

		catch (IOException ioe) {
			Log.e(TAG, "error: " + ioe.getMessage(), ioe);
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
			Log.e(TAG, "error: " + ioex.getMessage(), ioex);
		}
		
		Log.d(TAG,"Server Response: " + responseFromServer.trim());
		return responseFromServer.trim();
	}

}