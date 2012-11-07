package com.realife.l9droid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class LibraryActivity extends Activity {
	
	ListView lvMain;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.library);
	    
	    lvMain = (ListView) findViewById(R.id.lvMain);

	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	        this, R.array.lib_paths, android.R.layout.simple_list_item_1);
	    lvMain.setAdapter(adapter);

	    lvMain.setOnItemClickListener(new OnItemClickListener() {
	      public void onItemClick(AdapterView<?> parent, View view,
	          int position, long id) {
	        //Log.d(LOG_TAG, "itemClick: position = " + position + ", id = "
	        //    + id);
	    	  //Toast.makeText(this, view.toString(), Toast.LENGTH_LONG).show();
	    	  finish();
	      }
	    });
	    
	    
	  }
}
