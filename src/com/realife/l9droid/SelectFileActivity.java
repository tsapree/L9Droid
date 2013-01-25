package com.realife.l9droid;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SelectFileActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {
	Library lib;
	
	ListView lvFiles;
	
	ArrayList<Map<String, Object>> data;
	SimpleAdapter sAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_file);

		lib = Library.getInstance();
		
		data=lib.getFilesInFolder("/mnt/sdcard/l9droid/Snowball V3 S48");	//TODO: исправить
		
		String[] from = { Library.ATTR_NAME, Library.ATTR_DATE, Library.ATTR_IMAGE, Library.ATTR_SIZE };
		int[] to = { R.id.tvName, R.id.tvDate, R.id.ivPic, R.id.tvSize };
		sAdapter = new SimpleAdapter(this, data, R.layout.select_file_item, from, to);
		
		lvFiles = (ListView) findViewById(R.id.lvFiles);
		lvFiles.setAdapter(sAdapter);
		lvFiles.setOnItemClickListener(this);
		lvFiles.setOnItemLongClickListener(this);
		
		Button bBack = (Button) findViewById(R.id.bBack);
	    bBack.setOnClickListener(this);

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		String p=(String)(data.get(position).get(Library.ATTR_PATH));
		if ((Integer)(data.get(position).get(Library.ATTR_TYPE))==Library.TYPE_FILE) {
			selectFolder(p);
		} else {
			showFolderContent(p);
		};
	};

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		String p=(String)(data.get(position).get(Library.ATTR_PATH));
		if ((Integer)(data.get(position).get(Library.ATTR_TYPE))!=Library.TYPE_PARENT_FOLDER) {
			selectFolder(p);
		};		
		return true;
	};
	
	void showFolderContent(String path) {
		data.clear();
		data.addAll(lib.getFilesInFolder(path));
		sAdapter.notifyDataSetChanged();
		lvFiles.setSelection(0);
	}
	
	void selectFolder(String filename) {
		Intent intent = new Intent(this, ImportSelectFolderName.class);
		intent.putExtra("selectedpath", filename );
	    startActivity(intent);
	    finish();
	}

	public void onClick(View v) {
		if (v.getId()==R.id.bBack) {
			onBackPressed();
		};
	}

}
