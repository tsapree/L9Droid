package com.realife.l9droid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LibraryGameInfoActivity extends Activity implements OnClickListener {
	
	TextView tvGameName;
	TextView tvCategory;
	TextView tvAbout;
	TextView tvAuthors;
	
	//http://ifarchive.org/if-archive/games/spectrum/level9.zip
	//level9.zip/SNA/V3/COLOSSAL.SNA
	//Colossal Adventure S48/COLOSSAL.SNA
	//L9DROID/CACHE/LKFBFHH/level9.zip
	
	Library lib;
	
	Button bInstall;
	
	String game;
	GameInfo gi;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.library_game_info);
	    
	    bInstall=(Button) findViewById(R.id.bInstall);
	    bInstall.setOnClickListener(this);
	    
	    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
	    ivBack.setOnClickListener(this);

		game=getIntent().getStringExtra("selectedgame");
	    
		tvGameName = (TextView)findViewById(R.id.tvGameName);
		tvCategory = (TextView)findViewById(R.id.tvCategory);
		tvAbout = (TextView)findViewById(R.id.tvAbout);
		tvAuthors = (TextView)findViewById(R.id.tvAuthors);
		

	}
	
	protected void onResume() {
    	super.onResume();
    	
	    lib = Library.getInstance();
		fillInfo();
		
	    gi=lib.getGameInfo(this,game);

	    tvCategory.setText(gi.getCategory().toUpperCase());
		tvGameName.setText(gi.getTitle());
		tvAbout.setText(gi.getAbout());
		
	    if (gi.getId().startsWith("info_")) {
	    	//info, not game
	    	
	    	((TextView)findViewById(R.id.tvAuthorsLabel)).setVisibility(View.GONE);
	    	((TextView)findViewById(R.id.tvAboutLabel)).setVisibility(View.GONE);
	    	tvAuthors.setVisibility(View.GONE);
	    	bInstall.setVisibility(View.GONE);
	    	
	    } else {
			tvAuthors.setText(gi.getAuthors());
	    }
    	
    }
	

	public void onClick(View v) {
		View p=(View)v.getParent();
		switch (v.getId()) {
		case R.id.ibPlay:
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
			View w = p.findViewById(R.id.rlMarks);
			if (w.getVisibility()==View.GONE) w.setVisibility(View.VISIBLE);
			else w.setVisibility(View.GONE);
			break;
		case R.id.ibInstalled:
			p.setVisibility(View.GONE);
			p = (View)p.getParent();
			((ImageView)p.findViewById(R.id.ivMark)).setImageResource(Library.MARK_PICTURES_RESID[Library.MARK_INSTALLED]);
			if ((p!=null) && (p.getTag()!=null)) {
				lib.setMark((String)p.getTag(), Library.MARK_INSTALLED);
			};
			break;
		case R.id.ibRateDown:
			p.setVisibility(View.GONE);
			p = (View)p.getParent();
			((ImageView)p.findViewById(R.id.ivMark)).setImageResource(Library.MARK_PICTURES_RESID[Library.MARK_RATE_DOWN]);
			if ((p!=null) && (p.getTag()!=null)) {
				lib.setMark((String)p.getTag(), Library.MARK_RATE_DOWN);
			};
			break;
		case R.id.ibRateUp:
			p.setVisibility(View.GONE);
			p = (View)p.getParent();
			((ImageView)p.findViewById(R.id.ivMark)).setImageResource(Library.MARK_PICTURES_RESID[Library.MARK_RATE_UP]);
			if ((p!=null) && (p.getTag()!=null)) {
				lib.setMark((String)p.getTag(), Library.MARK_RATE_UP);
			};
			break;
		case R.id.ibDone:
			p.setVisibility(View.GONE);
			p = (View)p.getParent();
			((ImageView)p.findViewById(R.id.ivMark)).setImageResource(Library.MARK_PICTURES_RESID[Library.MARK_COMPLETED]);
			if ((p!=null) && (p.getTag()!=null)) {
				lib.setMark((String)p.getTag(), Library.MARK_COMPLETED);
			};
			break;
		case R.id.bInstall:
			Intent intent=new Intent(this, LibraryGameInstallActivity.class);
			intent.putExtra("selectedgame", game);
			startActivity(intent);
			break;
		case R.id.ivBack:
			onBackPressed();
			break;
		}
	};
	
	private void fillInfo() {
	    ArrayList<String> versions = lib.getInstalledVersions(game);
		
	    //Заполняю информацию о инсталлированных версиях
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.llInstalled);
		linLayout.removeAllViews();
	    LayoutInflater ltInflater = getLayoutInflater();
	    for (int i=0;i<versions.size();i++) {
			View item = ltInflater.inflate(R.layout.library_game_info_item, linLayout, false);
			TextView tvVersion = (TextView) item.findViewById(R.id.tvVersion);
			//fills info about this version, based on tags from parent dir
			tvVersion.setText(lib.getTags(versions.get(i)));
			Button bProperties = (Button) item.findViewById(R.id.bProperties);
			ImageButton ibPlay = (ImageButton) item.findViewById(R.id.ibPlay);
			item.setTag(versions.get(i));
			bProperties.setOnClickListener(this);
			ibPlay.setOnClickListener(this);
			
			ImageView ivMark = (ImageView) item.findViewById(R.id.ivMark);
			ivMark.setImageResource(Library.MARK_PICTURES_RESID[lib.getMark(versions.get(i))]);

			ImageButton ibDelete = (ImageButton) item.findViewById(R.id.ibDelete);
			ibDelete.setOnClickListener(this);
			ImageButton ibInstalled = (ImageButton) item.findViewById(R.id.ibInstalled);
			ibInstalled.setOnClickListener(this);
			ImageButton ibRateDown = (ImageButton) item.findViewById(R.id.ibRateDown);
			ibRateDown.setOnClickListener(this);
			ImageButton ibRateUp = (ImageButton) item.findViewById(R.id.ibRateUp);
			ibRateUp.setOnClickListener(this);
			ImageButton ibDone = (ImageButton) item.findViewById(R.id.ibDone);
			ibDone.setOnClickListener(this);

			linLayout.addView(item);
	    };
	};
	
};
	
