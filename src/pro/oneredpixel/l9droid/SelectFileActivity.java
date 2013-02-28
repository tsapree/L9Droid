package pro.oneredpixel.l9droid;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SelectFileActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {
	Library lib;
	
	ListView lvFiles;
	TextView tvFolder;
	
	ArrayList<Map<String, Object>> data;
	SimpleAdapter sAdapter;
	
	String folder;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_file);

		lib = Library.getInstance();
		
        SharedPreferences sPref=getPreferences(MODE_PRIVATE);
        folder = sPref.getString("selectfileactivity_folder", null);
        if (folder==null || (!(new File(folder)).exists())) {
			String sdState = android.os.Environment.getExternalStorageState();
			if (sdState.equals(android.os.Environment.MEDIA_MOUNTED))
				folder = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
			else folder = "/";
        };
		data=lib.getFilesInFolder(folder);

		
		tvFolder = (TextView) findViewById(R.id.tvFolder);
		tvFolder.setText(folder);
		
		String[] from = { Library.ATTR_NAME, Library.ATTR_DATE, Library.ATTR_IMAGE, Library.ATTR_SIZE };
		int[] to = { R.id.tvName, R.id.tvDate, R.id.ivPic, R.id.tvSize };
		sAdapter = new SimpleAdapter(this, data, R.layout.select_file_item, from, to);
		
		lvFiles = (ListView) findViewById(R.id.lvFiles);
		lvFiles.setAdapter(sAdapter);
		lvFiles.setOnItemClickListener(this);
		lvFiles.setOnItemLongClickListener(this);
		
	    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
	    ivBack.setOnClickListener(this);

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
		tvFolder.setText(path);
		data.clear();
		data.addAll(lib.getFilesInFolder(path));
		sAdapter.notifyDataSetChanged();
		lvFiles.setSelection(0);
		folder = path;
	}
	
	void selectFolder(String filename) {
		Intent intent = new Intent(this, ImportSelectFolderName.class);
		intent.putExtra("selectedpath", filename );
	    startActivity(intent);
	    finish();
	}

	public void onClick(View v) {
		if (v.getId()==R.id.ivBack) {
			onBackPressed();
		};
	}
	
	protected void onDestroy() {
		super.onDestroy();
		SharedPreferences sPref=getPreferences(MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putString("selectfileactivity_folder", folder);
		ed.commit();
	}
	

}
