package washington.cs.mobileocr.weocr;

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
	
	//TODO Set timeout limit

	public static String doFileUpload(Bitmap b) {

		final String TAG = "Server";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String exsistingFileName = "doOCR.jpeg";

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;
		String responseFromServer = "";
		String urlString = "http://128.208.4.56/tesseract/upload.php";
		
		try {
			//------------------ CLIENT REQUEST

			Log.e(TAG,"Inside second Method");

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] by = baos.toByteArray(); 
			InputStream inputStream = new ByteArrayInputStream(by);

			// open a URL connection to the Servlet

			URL url = new URL(urlString);


			// Open a HTTP connection to the URL

			conn = (HttpURLConnection) url.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			dos = new DataOutputStream( conn.getOutputStream() );

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + exsistingFileName +"\"" + lineEnd);
			dos.writeBytes(lineEnd);

			Log.e(TAG,"Headers are written");

			// create a buffer of maximum size

			bytesAvailable = inputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...

			bytesRead = inputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = inputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = inputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			Log.e(TAG,"File is written");
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

		//Read the SERVER RESPONSE
		try {
			inStream = new DataInputStream ( conn.getInputStream() );
			String str = "";
			while (( str = inStream.readLine()) != null) {
				responseFromServer += "\n" + str;
				Log.e(TAG,"Server Response"+responseFromServer);
			}
			inStream.close();
		}
		catch (IOException ioex) {
			Log.e(TAG, "error: " + ioex.getMessage(), ioex);
		}
		
		return responseFromServer.trim();
	}

}