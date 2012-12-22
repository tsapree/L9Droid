package com.realife.l9droid;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
	
	//http://ifarchive.org/if-archive/games/spectrum/level9.zip
	//level9.zip/SNA/V3/COLOSSAL.SNA
	//Colossal Adventure S48/COLOSSAL.SNA
	//L9DROID/CACHE/LKFBFHH/level9.zip
	
	Library lib;
	
	Button bPlay;
	Button bInstall;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    lib = new Library();
	    
	    setContentView(R.layout.library_game_info);
	    
	    bInstall=(Button) findViewById(R.id.bInstall);
	    bInstall.setOnClickListener(this);
	    
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
		
	    tvCategory.setText(gi.getCategory().toUpperCase());
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
		case R.id.bInstall:
			String downloadedPath = lib.downloadFileToCache("http://ifarchive.org/if-archive/games/spectrum/level9.zip");
			if (downloadedPath!=null) {
				Toast.makeText(this, "Downloaded ok: "+downloadedPath, Toast.LENGTH_SHORT).show();
				if (lib.unzipFile(downloadedPath, "SNA/V3/COLOSSAL.SNA", "Colossal Adventure V3 S48")) {
					Toast.makeText(this, "UnZipped well!", Toast.LENGTH_SHORT).show();
				} else Toast.makeText(this, "UnZip error!", Toast.LENGTH_SHORT).show();;
			}
			else Toast.makeText(this, "Download error!", Toast.LENGTH_SHORT).show();
			break;
		}
	};
	
	
}
