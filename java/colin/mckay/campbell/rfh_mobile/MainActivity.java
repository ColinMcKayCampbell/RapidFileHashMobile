package colin.mckay.campbell.rfh_mobile;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        // Handle navbar touch events
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_results: // Results icon
                    intent = new Intent(getApplicationContext(),ResultActivity.class);
                    startActivity(intent); // Change page
                    return true;
                case R.id.navigation_history: //
                    intent = new Intent(getApplicationContext(),HistoryActivity.class);
                    startActivity(intent); // Change page
                    return true;
            }
            return false; // Shouldn't be able to get here, but just in case
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case 0:{
                Log.i("StoragePerm", "Success");
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast notifier = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        // OnClick listeners
        TextView start = (TextView) findViewById(R.id.startDir);
        start.setOnClickListener(this);

        TextView blacklist = (TextView) findViewById(R.id.blackFile);
        blacklist.setOnClickListener(this);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        // End OnClick Listeners

        // Permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            notifier.setText("Storage permission required for use of this app.");
            notifier.show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);

        }
        // End permissions

        // Set up UI
        mTextMessage = (TextView) findViewById(R.id.message); // Gets page name object
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home); // Set highlighted nav icon to current page
        mTextMessage.setText(R.string.title_home); // Sets page name
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener); // Start nav listener
        // End set up UI
    }
// On Click Handler
    public void onClick(View v){
        Intent intent;
        switch(v.getId()){
            case R.id.startDir:{ // OnClick Start Directory text box
                try{intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY); // Start Storage Access Framework with directory filter
                }
                catch(ActivityNotFoundException e){
                    Log.e("ActivityNtFundException","");
                }
                break;
            }
            case R.id.blackFile:{ // OnClick Blacklist file text box
                try{
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/*");
                    startActivityForResult(intent, REQUEST_CODE_OPEN_FILE); // Start Storage Access Framework with txt file filter
                }
                catch(ActivityNotFoundException e){
                    Log.e("ActivityNtFundException","");
                }
                break;
            }
            case R.id.button:{
                if((StartDir!=null) && (BlackList!=null)) {
                    start(); // If a directory and blacklist file have been selected, start
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please select a start directory AND a blacklist file.",Toast.LENGTH_LONG).show(); // Otherwise inform user
                }
                break;
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        // Storage Access Framework response
        switch(requestCode){
            case REQUEST_CODE_OPEN_DIRECTORY:{ // Response for directory selection
                if(data != null){ // If there is a selection
                    // Process returned data
                    uri = data.getData();
                    StartDir = uri.getPath();
                    String[] parts = StartDir.split(":"); // Split directory from rest of data
                    // End of process returned data
                    if (parts.length > 1) {
                        StartDir = "/storage/emulated/0/" + parts[1]; // Attach directory to known rootdir, this method breaks SD card and USB access. TODO
                    }
                    else{
                        StartDir = "/storage/emulated/0/";  // Root dir being picked returns nothing, so set to root
                    }
                    Log.i("StartDir", StartDir);
                    TextView t = (TextView) findViewById(R.id.startDir);
                    t.setText(StartDir); // Update textbox to show selection
                }
                break;
            }

            case REQUEST_CODE_OPEN_FILE:{ // On file selection response
                if(data != null){
                    // Process data
                    uri = data.getData();
                    BlackList = uri.getPath();
                    String[] parts = BlackList.split(":");
                    // End of Process data

                    if (parts.length > 1) {
                        BlackList = "/storage/emulated/0/" + parts[1]; // Same deal as directory selection
                    }
                    else{
                        BlackList = "/storage/emulated/0/"; // This should never be able to happen
                    }
                    Log.i("Blacklist", BlackList);
                    TextView t = (TextView) findViewById(R.id.blackFile);
                    t.setText(BlackList); // Update UI
                }
                break;
            }
        }
        // End of Storage Access Framework response
    }

    public void start(){
        // Start file gathering task
        AsyncFileScan scan = new AsyncFileScan(this, new AsyncResponse(){
            @Override
            public void process(ConcurrentLinkedQueue<myFile> fileQueue){ // AsyncFileScan callback
                if(!fileQueue.isEmpty()) {
                    Log.d("Files found", " "+fileQueue.size());
                    processFiles(fileQueue); // Pass fileQueue to next stage
                }
                else{
                    Toast.makeText(getApplicationContext(),"No files found in directory.",Toast.LENGTH_LONG).show();
                }
            }
        } ); // End of AsyncFileScan callback
        scan.execute(StartDir);
    }
    public void processFiles(ConcurrentLinkedQueue<myFile> fileQueue){ // Set up hashing process
        if(!fileQueue.isEmpty()){
            AsyncFileHash hashTask = new AsyncFileHash(new AsyncResponse(){
                @Override
                public void process(ConcurrentLinkedQueue<myFile> fileQueue){ // AsyncFileHash callback
                    if(!fileQueue.isEmpty()){
                        // AsyncWriteTask Set Up
                        AsyncFileWrite write = new AsyncFileWrite(getApplicationContext(),new AsyncResponse(){ // AsyncFileWrite callback
                            @Override
                            public void process(ConcurrentLinkedQueue<myFile> returnedFileQueue){
                                ProgressBar progress = findViewById(R.id.progressBar);
                                progress.setVisibility(View.INVISIBLE); // Remove loading spinner
                                Intent intent = new Intent(getApplicationContext(),ResultActivity.class); // Set up new intent to open Results page
                                String file = returnedFileQueue.poll().fileHandle.getPath(); // Create path to output file
                                intent.putExtra("file",file);
                                startActivity(intent); // Switch page
                                Toast.makeText(getApplicationContext(),"Result file written to "+ file,Toast.LENGTH_LONG).show(); // Inform user of file location
                            }
                        },new File(BlackList));
                        // End of AsyncFileWrite callback
                        write.execute(fileQueue);

                        // End of AsyncWriteTask set up
                    }else{
                        Log.e("HASH", "ERROR"); // Debug thing
                    }
                }
            });
            hashTask.execute(fileQueue);

        }
    }

    private String StartDir;
    private String BlackList;
    private final int REQUEST_CODE_OPEN_DIRECTORY = 10;
    private final int REQUEST_CODE_OPEN_FILE = 11;

}

