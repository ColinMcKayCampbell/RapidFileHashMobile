package colin.mckay.campbell.rfh_mobile;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
/* This class processes the blacklist file and creates the output file */
public class AsyncFileWrite extends AsyncTask<ConcurrentLinkedQueue<myFile>, Void, ConcurrentLinkedQueue<myFile>>{

    ConcurrentLinkedQueue<myFile> resultQueue;
    AsyncResponse delegate;
    Context con;
    File blackList;
    Long timeStamp;
    ArrayList<String> badHash;
    public AsyncFileWrite(Context a, AsyncResponse b, File c){
        con = a;
        delegate = b;
        blackList = c;
    }
    protected void onPreExecute(){
        timeStamp = System.currentTimeMillis()/1000; // Get current timestamp
         badHash = new ArrayList<String>();
    }

    @Override
    protected ConcurrentLinkedQueue<myFile> doInBackground(ConcurrentLinkedQueue<myFile>... files){
        resultQueue = files[0];
        FileOutputStream outStream;
        String fileContents;

        File outFile = new File(getDir(con)+"/ResultFile"+ timeStamp); // Create output file name

        try(BufferedReader br = new BufferedReader(new FileReader(blackList))){

            String line;
            while((line = br.readLine()) != null){
                badHash.add(line); // Get lines from blackList file, add them to array
            }
        }catch(IOException e){e.printStackTrace();}

        try {
           outStream = new FileOutputStream(outFile);
            while(!resultQueue.isEmpty()){
                myFile f = resultQueue.poll(); // Take front of result queue
                Boolean bad = false;
                for(String s : badHash){  // Iterate blacklist file, determine if current file is bad
                    if(Objects.equals(s,f.hash)){
                      bad = true;
                    }
                }
                fileContents = "Path: " + f.fileHandle.getPath() + "\r\n" + "Hash: " + f.hash + "\r\nKnown Bad: "+bad+"\r\n\n"; // Create string to write to file
                outStream.write(fileContents.getBytes()); // Write
            }
            outStream.close();
            resultQueue.add(new myFile(outFile)); // Add output file to result queue for passing back to the main thread
        }catch(Exception e){
            e.printStackTrace();
        }
        return resultQueue;
    }
    private String getDir(Context c){ // This function gets the app's internal directory
        File file = c.getExternalFilesDir(null);
        if(!file.exists()){
            file.mkdirs();
        }
        return file.toString();
    }

    @Override
    protected void onPostExecute(ConcurrentLinkedQueue<myFile> f){
        delegate.process(f);
    } // Send data to callback
}
