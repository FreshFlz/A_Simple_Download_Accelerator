/**
 *
 * @author FreshFLZ
 */

package downloadaccelerator;

import java.net.URL;


public abstract class Connections implements Runnable{
    
    protected int tID; // Thread ID
    private URL url;
    protected String OutFile;
    protected int StartByte;
    protected int EndByte;
    protected boolean finished;
    private Thread thread;
    
    //----------------------------------------------------
    
    public Connections (int threadID, URL url , String outF , int startByte, int endByte) {
        tID = threadID;
        url = url;
        OutFile = outF;
        StartByte = startByte;
        EndByte = endByte;
        finished = false;
	
        download();
    }
    //----------------------------------------------------
    public boolean isFinished() {
        return finished;
    }
    //----------------------------------------------------
    public void download() {
	thread = new Thread(this);
	thread.start();
    }
    //----------------------------------------------------
    public void waitFinish() throws InterruptedException {
        thread.join();
    }
}
