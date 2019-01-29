package colin.mckay.campbell.rfh_mobile;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Colin on 28/04/2018.
 * This class handles filesystem scanning, it returns a Queue of "myFile" objects which represents every file in the scanned file tree.
 */

public class AsyncFileScan extends AsyncTask<String, Void, ConcurrentLinkedQueue<myFile>>{

    // vars
    Activity activity;
    ConcurrentLinkedQueue<myFile> fileQueue;
    ProgressBar progress;
    AsyncResponse delegate; // Callback interface
    // end of vars

    public AsyncFileScan(Activity a, AsyncResponse b){
        activity = a;
        delegate = b;
        progress = activity.findViewById(R.id.progressBar);
    }

    @Override
    protected void onPreExecute(){
        fileQueue = new ConcurrentLinkedQueue<myFile>();
        progress.setVisibility(View.VISIBLE); // Show loading spinner
    }

    @Override
    protected ConcurrentLinkedQueue<myFile> doInBackground(String... strings){

        File root = new File(strings[0]); // root directory
        File[] fileList;
        ArrayList<File> dirList = new ArrayList<File>();
        dirList.add(root);
        while(!dirList.isEmpty()) { // Repeat until dirList is empty
            root = dirList.get(0); //
            dirList.remove(0); // Take first directory from dirList
            fileList = root.listFiles(); // List all filesystem objects in directory

            for (File f : fileList) { // For each object in file list
                if (f.isDirectory()) {
                    Log.v("Found directory", "" + f.getAbsoluteFile());
                    dirList.add(f); // Add directories to dirList to be processed
                } else {
                    if (f.exists() && f.canRead()) { // If it is an accessible file
                        Log.v("Found file", "" + f.getPath());
                        fileQueue.add(new myFile(f)); // Add file to list to be hashed
                    }
                }
            }

        }
        return fileQueue;
    }

    @Override
    protected void onPostExecute(ConcurrentLinkedQueue<myFile> f){
        delegate.process(f); // Send file list to callback
    }
}
