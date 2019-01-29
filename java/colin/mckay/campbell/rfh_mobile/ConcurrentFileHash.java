package colin.mckay.campbell.rfh_mobile;

import android.app.Activity;
import android.os.Handler;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Colin on 28/04/2018.
 * I wrote my own thread pool manager
 */

public class ConcurrentFileHash{
    // vars
    ConcurrentLinkedQueue<myFile> files;
    ConcurrentLinkedQueue<myFile> results;
    int threads;
    // end of vars

    public ConcurrentFileHash(ConcurrentLinkedQueue<myFile> fileQueue){
        files = fileQueue;
        int cores = Runtime.getRuntime().availableProcessors(); // Get available CPUs
        results = new ConcurrentLinkedQueue<myFile>();
        // Make enough threads

        if(files.size() > cores*2){ // If there's going to be more files than threads
            threads = cores*2;
        }
        else{
            threads = files.size(); // Otherwise make a file for each thread
        }
        // End of make threads
    }
    public ConcurrentLinkedQueue<myFile> execute(){
        //Spawn worker threads
        ArrayList<Thread> threadQueue = new ArrayList<>();
        for(int x = 0; x < threads; x++){
             threadQueue.add(new worker(files,results));
             threadQueue.get(x).start();
        }
        // End of spawning worker threads
        for(Thread t : threadQueue){
            try{t.join();}catch(InterruptedException e){} // Wait for all to finish
        }
        return results;
    }
    class worker extends Thread{ // Worker threads take from communal myFile queue, run the object's hash function, put results in results queue.
        ConcurrentLinkedQueue<myFile> files;
        ConcurrentLinkedQueue<myFile> results;
        public worker(ConcurrentLinkedQueue<myFile> f, ConcurrentLinkedQueue<myFile> re){
               files = f;
               results = re;
        }
        public void run(){
            while(!files.isEmpty()){ // Repeat until no files are left
                myFile current = files.poll();
                current.doHash();
                results.add(current);
            }
        }
    }
}
