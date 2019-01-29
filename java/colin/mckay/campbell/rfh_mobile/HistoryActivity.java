package colin.mckay.campbell.rfh_mobile;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class HistoryActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener // Handles navbar
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
                    intent = new Intent(getApplicationContext(),ResultActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_history:
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        // Set up UI
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_history);
        mTextMessage.setText(R.string.title_notifications);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        // End of set up UI

        // Fill list view
        historyFill fillList = new historyFill();
        fillList.execute(getDir(this));
        // End of fill list view

        final ListView list = findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListAdapter adapter = list.getAdapter();
                //adapter.get
                Intent intent = new Intent(HistoryActivity.this,ResultActivity.class);
                intent.putExtra("file", adapter.getItem(i).toString());
                startActivity(intent);
            }
        });


    }

    private File getDir(Context c){ // Utility for getting directory again, this one adds a slash required for the historyFill class.
        File file = new File(c.getExternalFilesDir(null)+"/");
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }
    class historyFill extends AsyncTask<File,Void,File[]>{


        protected File[] doInBackground(File... dir){
            return dir[0].listFiles();
        }
        protected void onPostExecute(File[] files){
            ListView list = findViewById(R.id.listView);
            String[] strings = new String[files.length];
            for(int x = 0; x< files.length; x++){ // For each file in output dir
                strings[x] = files[x].getName(); // Add name to list of strings
            }
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(HistoryActivity.this,android.R.layout.simple_list_item_1,strings); // Give ListView array of files from output dir
            list.setAdapter(itemsAdapter);

                // Hide loading spinner
                ProgressBar progress = findViewById(R.id.historyProgress);
                progress.setVisibility(View.INVISIBLE);
                // End of hide loading spinner
        }
    }
}
