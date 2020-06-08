import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class WebWorker extends Thread {
    private String urlString;
    private WebFrame frame;
    private int row;
    public WebWorker(String urlString, int row, WebFrame frame){
        this.urlString = urlString;
        this.row = row;
        this.frame = frame;
    }

    public void run(){
        frame.acquireWorker();
        frame.releaseWorker(download(), row);
    }

 	public String download() {
        int size = 0;
        long started = System.currentTimeMillis();
		InputStream input = null;
		StringBuilder contents = null;
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				contents.append(array, 0, len);
				Thread.sleep(100);
				size += len;
			}
			long finished = System.currentTimeMillis();
            return new SimpleDateFormat("HH:mm:ss").format(new Date(finished)) + "  "
                    + (finished - started) + "ms  " +size+"bytes";
			// Successful download if we get here
			
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {}
		catch(InterruptedException exception) {
            return "InterruptedException";
			// deal with interruption
		}
		catch(IOException ignored) {}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) { }
        }
		return "error occured";
		}
	
}
