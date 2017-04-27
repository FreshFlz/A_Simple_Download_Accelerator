/**
 *
 * @author FreshFLZ
 */

package downloadaccelerator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;

public class Download extends Observable implements Runnable{
    
    private URL url; //the url we get from user
    private int connections; // number of connections user wants
    private int size; // number of bytes to download
    private int dCount; // number of bytes downloaded till now
    private int status; // current status
    private String desFolder; // Destination folder to save the file
    private String fileName;
    
    //List of download threads
    private ArrayList<Connections> DLThreadList;

    //---------------------------------------------------------------
    public static final String Statuses[] = {"Downloading","Paused", "Completed", "Cancelled", "Error"};
    
    public int getStatus() {
        return status;
    }
    
    //codes of different statuses
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETED = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    //---------------------------------------------------------------
    public Download(URL url , int con , String df){ //constructor
        this.url = url;
        this.connections = con;
        this.desFolder = df;
        this.fileName = getFileName(url);
        size = -1;
        dCount = 0;
        status = DOWNLOADING;
        System.out.println("File name: " + fileName);

        DLThreadList = new ArrayList<Connections>();
        beginDownload();
    }
    //----------------------------------------------------------------
    public String getUrl(){ 
        return url.toString(); //to use the url as a string
    }
    
    private String getFileName(URL url){ //finding the file name from the given URL
        String fName = url.getFile();
        return fName.substring(fName.lastIndexOf('/') + 1);
    }
    
    public float getProgress(){
        return ((float) dCount / size ) * 100;
    }
    
    public int getSize(){
        return size;
    }
    
    public synchronized void downloaded(int value) {
		dCount += value;
		stateChanged();
	}
    
    //----------------------------------------------------------------    
    private void beginDownload(){
        
        Thread thread = new Thread(this);
        thread.start();
        
    }
    //-----------------------------pause-------------------------------
    public void pause() {
        status = PAUSED;
        stateChanged();
    }
    //-----------------------------resume-------------------------------
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        beginDownload();
    }
    //-----------------------------cancel-------------------------------
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }
    //--------------------------has_an_Error----------------------------
    private void error() {
        status = ERROR;
        stateChanged();
    }
    //------------------------------------------------------------------
    private void stateChanged() {//To notify observers
        setChanged();
        notifyObservers();
    }
    //-------------------------------------------------------------------
    //-------------------------------------------------------------------
    private static final int BLOCK_SIZE = 4096;
    private static final int BUFFER_SIZE = 4096;
    private static final int MIN_DOWNLOAD_SIZE = BLOCK_SIZE * 100;
    
    public void run(){
        
        HttpURLConnection connection = null;
        
        try{
             // Open connection to URL
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            
            connection.connect();// Connect to server
            
            // Make sure response code is in the 200 range
            if (connection.getResponseCode() / 100 != 2) {
                error();
            }
            
            int contentLength = connection.getContentLength(); 
            if (contentLength < 1) { // Check if content length is valid
                error();
            }
            
            if (size == -1) { // set the size if it wasn't set before
                size = contentLength;
                stateChanged();
            }
            
            String index;
            index = desFolder + fileName.substring(fileName.lastIndexOf('.'));
            // if the state is DOWNLOADING (no error) -> start downloading
            if (status == DOWNLOADING) {
                // check whether we have list of download threads or not, if not -> init download
            	if (DLThreadList.size() == 0)
            	{
                    if (size > MIN_DOWNLOAD_SIZE) {
		        // downloading size for each thread
			int partSize = Math.round(((float)size / connections) / BLOCK_SIZE) * BLOCK_SIZE;
			System.out.println("part size range : " + partSize);
						
			// start/end Byte for each thread
                        int startByte = 0;
			int endByte = partSize - 1;
                        
			HttpThread t1 = new HttpThread(1, url, index , startByte, endByte);
			DLThreadList.add(t1);
			int i = 2;
			while (endByte < size) {
                            startByte = endByte + 1;
                            endByte += partSize;
                            t1 = new HttpThread(i, url, index , startByte, endByte);
                            DLThreadList.add(t1);
                            ++i;
			}
                    } else
            		{
                            HttpThread t1 = new HttpThread(1, url, index, 0, size);
                            DLThreadList.add(t1);
            		}
            	} else { // resume all downloading threads
            		for (int i = 0; i < DLThreadList.size(); ++i) {
            			if (!DLThreadList.get(i).isFinished())
            				DLThreadList.get(i).download();
            		}
                    }
                // waiting for all threads to complete
                for (int i = 0; i < DLThreadList.size(); ++i) {
                    DLThreadList.get(i).waitFinish();
		}
		
                // check the current state again
		if (status == DOWNLOADING) {
                    status = COMPLETED;
                    stateChanged();
		}
            }
                
        }catch (Exception e) {
            error();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
                
    }
    //---------------------------------------------------------------------------------------------------
    /**
    * Thread using Http protocol to download a part of file
    */
    private class HttpThread extends Connections {
        
        public HttpThread(int threadID, URL url, String OutFile, int startByte, int endByte) {
            super(threadID, url, OutFile, startByte, endByte);
	}
        
        public void run() {
            BufferedInputStream bin = null;
            RandomAccessFile raf = null;
			
            try {
		// open Http connection to URL
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				
		// set the range of byte to download
		String byteRange = StartByte + "-" + EndByte;
		conn.setRequestProperty("Range", "bytes=" + byteRange);
		System.out.println("bytes=" + byteRange);
				
		// connect to server
		conn.connect();
				
		// Make sure the response code is in the 200 range.
	        if (conn.getResponseCode() / 100 != 2) {
                    error();
	        }
				
                // get the input stream
		bin = new BufferedInputStream(conn.getInputStream());
				
		// open the output file and seek to the start location
		raf = new RandomAccessFile(OutFile, "rw");
		raf.seek(StartByte);
				
		byte data[] = new byte[BUFFER_SIZE];
		int numRead;
		while((status == DOWNLOADING) && ((numRead = bin.read(data,0,BUFFER_SIZE)) != -1))
		{
                    // write to buffer
                    raf.write(data,0,numRead);
                    // increase the startByte for resume later
                    StartByte += numRead;
                    // increase the downloaded size
                    downloaded(numRead);
		}
				
                if (status == DOWNLOADING) {
                    finished = true;
		}
	} catch (IOException e) {
            error();
	} finally {
		if (raf != null) {
                    try {
			raf.close();
			} catch (IOException e) {}
		}
				
		if (bin != null) {
                    try {
			bin.close();
			} catch (IOException e) {}
		}
        }
			
	System.out.println("End thread " + tID);
        
        }
    }   
}
