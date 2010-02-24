/**
 * @author Hussein Yapit
 * 
 * based on WeSnapOCR
 */
package edu.washington.cs.main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.AbstractHttpEntity;

import com.sun.image.codec.jpeg.*;

public class WeOCRFormEntity extends AbstractHttpEntity {
    
	private static final String CRLF = "\r\n";
	
    private static final String BOUNDARY = "--------------GfHioqf1adDgeQwHF2fp9";  // monkey-typed random string
    private static final String CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;
    
    private static final String BODY_HEADER = 
        "--" + BOUNDARY + CRLF +
        "Content-Disposition: form-data; name=\"userfile\"; filename=\"text.jpg\"" + CRLF +
        "Content-Type: image/jpeg" + CRLF +
        "Content-Transfer-Encoding: binary" + CRLF + 
        "\r\n";
    private static final String BODY_TRAILER = 
    	CRLF + 
        "--" + BOUNDARY + CRLF +
        "Content-Disposition: form-data; name=\"outputformat\"" + CRLF + 
        CRLF +
        "txt" + CRLF +
        "--" + BOUNDARY + CRLF +
        "Content-Disposition: form-data; name=\"outputencoding\"" + CRLF +
        CRLF + 
        "utf-8" + CRLF +
        "--" + BOUNDARY + "--" + CRLF;
    
    private static final int STREAM_BUFFER_SIZE = 2560;
    private ByteArrayOutputStream mImageStream;
    
    public WeOCRFormEntity (BufferedImage img, int quality) throws IOException {
        
    	// Write compressed image to memory; we need the content length
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream(STREAM_BUFFER_SIZE);
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(imageStream);
        encoder.encode(img);
        imageStream.close();
        
        mImageStream = imageStream;
        setContentType(CONTENT_TYPE);
        setChunked(false);
    }
    
    public WeOCRFormEntity (BufferedImage img) throws IOException {
        this(img, 100);
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("WeOCRFormEntity does not support getContent()");
    }

    public long getContentLength() {
        return mImageStream.size() + BODY_HEADER.length() + BODY_TRAILER.length();
    }

  
    public boolean isRepeatable() {
        return true;
    }


    public boolean isStreaming() {
        // TODO Auto-generated method stub
        return false;  // FIXME
    }

    
    public void writeTo(OutputStream os) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        os.write(BODY_HEADER.getBytes("ascii"));  // XXX check
        mImageStream.writeTo(os);
        os.write(BODY_TRAILER.getBytes("ascii")); // XXX check
        os.flush();
    }

}
