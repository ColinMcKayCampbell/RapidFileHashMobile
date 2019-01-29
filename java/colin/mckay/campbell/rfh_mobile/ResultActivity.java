package colin.mckay.campbell.rfh_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_results:
                    return true;
                case R.id.navigation_history:
                    intent = new Intent(getApplicationContext(),HistoryActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        // Set up UI
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_results);
        mTextMessage.setText(R.string.title_dashboard);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        SharedPreferences sharedPref = ResultActivity.this.getPreferences(Context.MODE_PRIVATE);
        // End of set up UI

        Intent intent = getIntent(); // Get intent used to open page
        String file = "";
        String key = "last";
        if(intent != null){
            file = intent.getStringExtra("file"); // Get file name from intent, if possible
        }
        if(file != null && file != "") { // If a file has been passed
            resultFill results = new resultFill();
            mTextMessage.setText(file); // Set page message to file name
            // Shared Preferences
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putString(key, file); // Saves last opened file in shared Preferences
            edit.commit();
            // End of Shared Preferences
            results.execute(new File(file)); // Fill ListView
        }
        else{ // If no file has been passed to load, open last opened file
            file = sharedPref.getString(key,null);  // Get last opened file from Shared Preferences
            if(file != null){
                resultFill results = new resultFill();
                mTextMessage.setText("Last opened : "+file);
                results.execute(new File(file)); // Fill ListView
            }
            else{
                // If no previously opened file is found
                ProgressBar progress = findViewById(R.id.resultProgress);
                progress.setVisibility(View.INVISIBLE); // Hide loading spinner
                Toast.makeText(getApplicationContext(),"Run a scan first.",Toast.LENGTH_LONG).show();
            }
        }
        // Save previous and open on launch
    }
    private File getDir(Context c, File f){
        File file = new File(c.getExternalFilesDir(null)+"/"+f.getName());
        return file;
    }
    class resultFill extends AsyncTask<File,Void,String[]> {  // Functionally the same as HistoryFill


        protected String[] doInBackground(File... file){
            ArrayList<String> lines = new ArrayList<String>();
            File f =getDir(ResultActivity.this,file[0]); // Get file directory
            try(BufferedReader br = new BufferedReader(new FileReader(f))){ // Read file in as lines

                String line;
                while((line = br.readLine()) != null){
                    lines.add(line); // Add to array of lines
                }
            }
            catch(IOException e){e.printStackTrace();}
            String[] returnStrings = new String[lines.size()];
            returnStrings = lines.toArray(returnStrings); // Convert from ArrayList to String[]
            return returnStrings;
        }
        protected void onPostExecute(String[] strings){
            ListView list = findViewById(R.id.listViewResult);
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(ResultActivity.this,android.R.layout.simple_list_item_1,strings); // Give ListView strings
            list.setAdapter(itemsAdapter);
            // Stop loading spinner
            ProgressBar progress = findViewById(R.id.resultProgress);
            progress.setVisibility(View.INVISIBLE);
            // End of stop loading spinner
        }
    }
}
