package com.realife.l9droid;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LibraryGameDownloadActivity extends Activity {
	
	TextView tvGameName;
	TextView tvCategory;
	
	Library lib;
	
	String game;
	GameInfo gi;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.library_game_download);
	    
	    lib = new Library();
	    game=getIntent().getStringExtra("selectedgame");
	    gi=lib.getGameInfo(this,game);
	    tvGameName = (TextView)findViewById(R.id.tvGameName);
		tvCategory = (TextView)findViewById(R.id.tvCategory);
		tvCategory.setText(gi.getCategory().toUpperCase());
		tvGameName.setText(gi.getTitle());
	    
	    //Button bInstallDownload = (Button) findViewById(R.id.bInstallDownload);
	    //bInstallDownload.setOnClickListener(this);
		
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.llSources);
		linLayout.removeAllViews();
	    LayoutInflater ltInflater = getLayoutInflater();
	    for (int i=0;i<gi.getNumberOfPaths();i++) {
			View item = ltInflater.inflate(R.layout.library_game_download_item, linLayout, false);
			TextView tvVersion = (TextView) item.findViewById(R.id.tvLink);
			//fills info about this version, based on tags from parent dir
			tvVersion.setText(gi.getPath(i)+" "+gi.getTags(i));
			//Button bProperties = (Button) item.findViewById(R.id.bProperties);
			//Button bPlay = (Button) item.findViewById(R.id.bPlay);
			//item.setTag(versions.get(i));
			//bProperties.setOnClickListener(this);
			//bPlay.setOnClickListener(this);
			linLayout.addView(item);
	    };
	    
	};
	
}
