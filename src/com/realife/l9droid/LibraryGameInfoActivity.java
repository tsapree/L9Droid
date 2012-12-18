package com.realife.l9droid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LibraryGameInfoActivity extends Activity implements OnClickListener {
	
	int selected_game_id;
	TextView tvGameName;
	TextView tvCategory;
	TextView tvAbout;
	TextView tvAuthors;
	
	Library lib;
	
	Button bPlay;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    lib = new Library();
	    
	    setContentView(R.layout.library_game_info);
	    
		String game=getIntent().getStringExtra("selectedgame");
	    ArrayList<String> versions = lib.getInstalledVersions(game);
		
	    //Заполняю информацию о инсталлированных версиях
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.llInstalled);
	    LayoutInflater ltInflater = getLayoutInflater();
	    for (int i=0;i<versions.size();i++) {
			View item = ltInflater.inflate(R.layout.library_game_info_item, linLayout, false);
			TextView tvVersion = (TextView) item.findViewById(R.id.tvVersion);
			//fills info about this version, based on tags from parent dir
			tvVersion.setText(lib.getTags(versions.get(i)));
			Button bProperties = (Button) item.findViewById(R.id.bProperties);
			Button bPlay = (Button) item.findViewById(R.id.bPlay);
			item.setTag(versions.get(i));
			bProperties.setOnClickListener(this);
			bPlay.setOnClickListener(this);
			linLayout.addView(item);
	    };

	    //int info[]={0,0,0,0};
	    //lib.getGameInfo(game,info);
	    
	    GameInfo gi=lib.getGameInfo(this,game);
	    
		tvGameName = (TextView)findViewById(R.id.tvGameName);
		tvCategory = (TextView)findViewById(R.id.tvCategory);
		tvAbout = (TextView)findViewById(R.id.tvAbout);
		tvAuthors = (TextView)findViewById(R.id.tvAuthors);
		
	    tvCategory.setText(gi.getCategory());
		tvGameName.setText(gi.getTitle());
		tvAbout.setText(gi.getAbout());
		tvAuthors.setText(gi.getAuthors());
	}

	public void onClick(View v) {
		View p=(View)v.getParent();
		switch (v.getId()) {
		case R.id.bPlay: // кнопка ввода команды
			if ((p!=null) && (p.getTag()!=null)) {
				Intent intent = new Intent();
		  	  	intent.putExtra("opengame", (String)p.getTag());
		  	  	setResult(RESULT_OK,intent);
		  	  	finish();
			};
			break;
		case R.id.bProperties:
			if ((p!=null) && (p.getTag()!=null)) {
				Toast.makeText(this, "Path: "+p.getTag(), Toast.LENGTH_SHORT).show();
			};
			break;
		}

		
		
	};
}
