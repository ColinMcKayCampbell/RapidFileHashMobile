package colin.mckay.campbell.rfh_mobile;


import android.os.AsyncTask;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Colin on 28/04/2018.
 * This class handles the ConcurrentFileHash object asynchonously
 */

public class AsyncFileHash extends AsyncTask<ConcurrentLinkedQueue<myFile>, Void, ConcurrentLinkedQueue<myFile>> {

    // vars
    ConcurrentLinkedQueue<myFile> resultQueue;
    ConcurrentFileHash hasher;
    AsyncResponse delegate;
    // end vars
    public AsyncFileHash(AsyncResponse b){
        delegate = b;
    }

    @Override
    protected void onPreExecute(){
        resultQueue = new ConcurrentLinkedQueue<myFile>();

    }

    @Override
    protected ConcurrentLinkedQueue<myFile> doInBackground(ConcurrentLinkedQueue<myFile>... files){
        hasher = new ConcurrentFileHash(files[0]);
        return hasher.execute(); //  Set up and execute concurrent hasher against file queue passed in
    }

    @Override
    protected void onPostExecute(ConcurrentLinkedQueue<myFile> f){
        delegate.process(f);
    } // Send output to callback
}
