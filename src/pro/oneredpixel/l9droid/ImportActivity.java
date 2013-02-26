package pro.oneredpixel.l9droid;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ImportActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {

	ListView lvMain;
	Library lib;
	ArrayAdapter<String> lvAdapter;
	String rootElement;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.import_files);
	    
	    lvMain = (ListView) findViewById(R.id.importlist);
	    lvAdapter = new ArrayAdapter<String>(this, R.layout.log_list_item, new ArrayList<String>());
	    lvMain.setAdapter(lvAdapter);
	    
	    lvMain.setOnItemClickListener(this);
	    lvMain.setOnItemLongClickListener(this);
    
	    rootElement="/mnt/sdcard";
	    getFolderContent(rootElement);
	};
	
	void getFolderContent(String path) {
		if (path==null) path="/";
		lvAdapter.clear();
		//lvAdapter.add(path);

		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdpath=new File(path);
			if (!(path.equalsIgnoreCase(rootElement))) {
				String parent=sdpath.getParent();
				if (parent!=null) lvAdapter.add(sdpath.getParent());
			}
			String [] FileList=sdpath.list();
			for (int i=0; i<FileList.length;i++) {
				lvAdapter.add(path+"/"+FileList[i]);
			};
		};
		
	};


	//@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String selFile=lvAdapter.getItem(position);
		File sdpath=new File(selFile);
		if (sdpath.isDirectory()) getFolderContent(selFile);
		else SelectFolder(selFile);
	}

	//@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		String selFile=lvAdapter.getItem(position);
		//File sdpath=new File(selFile);
		//TODO: убрать возможность долгого клика по отцовской папке
		SelectFolder(selFile);
		return true;
	}
	
	void SelectFolder(String filename) {
		Intent intent = new Intent(this, ImportSelectFolderName.class);
		intent.putExtra("importname", filename );
	    startActivity(intent);
	    //finish();
	}

}
