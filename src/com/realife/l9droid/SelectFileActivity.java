package com.realife.l9droid;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SelectFileActivity extends Activity implements OnItemClickListener{
	Library lib;
	
	ListView lvFiles;
	
	ArrayList<Map<String, Object>> data;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_file);

		lib = new Library();
		data=lib.getFilesInFolder("/mnt");
		
		String[] from = { Library.ATTR_NAME, Library.ATTR_DATE, Library.ATTR_IMAGE };
		int[] to = { R.id.tvName, R.id.tvDate, R.id.ivPic };
		SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.select_file_item, from, to);
		
		lvFiles = (ListView) findViewById(R.id.lvFiles);
		lvFiles.setAdapter(sAdapter);
		lvFiles.setOnItemClickListener(this);

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		String p=(String)(data.get(position).get(Library.ATTR_PATH));
		Intent intent = new Intent();
  	  	intent.putExtra("restoregame", p);
  	  	setResult(RESULT_OK,intent);
  	  	finish();		
	};
}
