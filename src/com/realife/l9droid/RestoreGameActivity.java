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
import android.widget.TextView;

public class RestoreGameActivity extends Activity implements OnItemClickListener {
	
	Library lib;
	
	ListView lvStates;
	TextView tvNoFiles;
	
	ArrayList<Map<String, Object>> data;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restore_game);

		lib = new Library();
		String gamepath=getIntent().getStringExtra("gamepath");
		data=lib.getSaved(gamepath);
		
		tvNoFiles =  (TextView) findViewById(R.id.tvNoFiles);
		tvNoFiles.setVisibility((data.size()==0)?View.VISIBLE:View.GONE);
		
		String[] from = { Library.ATTR_NAME, Library.ATTR_DATE, Library.ATTR_IMAGE };
		int[] to = { R.id.tvName, R.id.tvDate, R.id.ivPic };
		SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.restore_game_item, from, to);
		
		lvStates = (ListView) findViewById(R.id.lvStates);
		lvStates.setAdapter(sAdapter);
		lvStates.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		String p=(String)(data.get(position).get(Library.ATTR_PATH));
		Intent intent = new Intent();
  	  	intent.putExtra("restoregame", p);
  	  	setResult(RESULT_OK,intent);
  	  	finish();		
	};
}
