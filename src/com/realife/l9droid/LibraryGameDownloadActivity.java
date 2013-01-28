package com.realife.l9droid;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LibraryGameDownloadActivity extends Activity implements OnClickListener {

	final int LG_ReadyToDownload=0;
	final int LG_Downloading=1;
	final int LG_Unzipping=2;
	final int LG_Installed=3;
	final int LG_Canceled=4;
	
	TextView tvGameName;
	TextView tvCategory;
	TextView tvNoSources;
	
	Library lib;
	
	String game;
	GameInfo gi;
	
	boolean cancelPressed=false;
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.library_game_download);
	    
	    lib = Library.getInstance();
	    game=getIntent().getStringExtra("selectedgame");
	    gi=lib.getGameInfo(this,game);
	    tvGameName = (TextView)findViewById(R.id.tvGameName);
		tvNoSources = (TextView)findViewById(R.id.tvNoSources);
		
		tvGameName.setText(gi.getTitle().toUpperCase());
		
	    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
	    ivBack.setOnClickListener(this);
		
		FillSourcesInfo();
	    
	};
	
	void FillSourcesInfo() {
		cancelPressed=false;
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.llSources);
		linLayout.removeAllViews();
	    LayoutInflater ltInflater = getLayoutInflater();
	    if (gi.getNumberOfPaths()>0) tvNoSources.setVisibility(View.GONE);
	    else tvNoSources.setVisibility(View.VISIBLE);
	    
	    for (int i=0;i<gi.getNumberOfPaths();i++) {
			View item = ltInflater.inflate(R.layout.library_game_download_item, linLayout, false);
			TextView tvVersion = (TextView) item.findViewById(R.id.tvLink);
			//fills info about this version, based on tags from parent dir
			tvVersion.setText(gi.getPath(i)+" "+gi.getTags(i));
			Button bDownload = (Button) item.findViewById(R.id.bDownload);
			bDownload.setOnClickListener(this);
			Button bCancel = (Button) item.findViewById(R.id.bCancel);
			bCancel.setOnClickListener(this);
			if (lib.checkPathInLibrary(gi.getId()+" "+gi.getTags(i))) {
				bDownload.setText("Installed");
				bDownload.setEnabled(false);
			} else if (lib.checkFileInCache(gi.getPath(i))!=null) bDownload.setText("Install");
			
			item.setTag(i);
			linLayout.addView(item);
	    };
	};
	
	public void onClick(View v) {
		if (v.getId()==R.id.ivBack) {
			onBackPressed();
			return;
		};
		
		View p=(View)v.getParent();

		if ((p!=null) && (p.getTag()!=null)) {
			int index=(Integer) p.getTag();
			switch (v.getId()) {
			case R.id.bDownload:
				
				LinearLayout linLayout = (LinearLayout) findViewById(R.id.llSources);
				for (int i=0; i<linLayout.getChildCount();i++) {
					linLayout.getChildAt(i).findViewById(R.id.bDownload).setEnabled(false);
				};
				
				//Toast.makeText(this, "download pressed: "+gi.getPath(index), Toast.LENGTH_SHORT).show();
				DownloadInstallFileTask mt = new DownloadInstallFileTask(p);
			    mt.execute(gi.getPath(index),gi.getFiles(index), gi.getId()+" "+gi.getTags(index));
			    
			    break;
			case R.id.bCancel:
				cancelPressed=true;
				break;
			}
			
		};
	};
	
	class DownloadInstallFileTask extends AsyncTask<String, Integer, Void> {

		View v;
		int operation=0;
		
		DownloadInstallFileTask(View view) {
			v=view;
		};
		
	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    	if (v!=null) {
				TextView tvStatus=(TextView)v.findViewById(R.id.tvStatus);
				ProgressBar pbProgress=(ProgressBar) v.findViewById(R.id.pbProgress);
				tvStatus.setText("Downloading:");
				tvStatus.setVisibility(View.VISIBLE);
				pbProgress.setIndeterminate(true);
				pbProgress.setVisibility(View.VISIBLE);
	    	  
		  		Button bDownload = (Button) v.findViewById(R.id.bDownload);
				Button bCancel = (Button) v.findViewById(R.id.bCancel);
	
				bDownload.setVisibility(View.INVISIBLE);
				bCancel.setVisibility(View.VISIBLE);  
	    	}
	    }

	    @Override
	    protected Void doInBackground(String... params) {
	    	
	    	String downloadedPath = lib.downloadFileToCache(params[0],this);
	    	if (downloadedPath!=null) {
	    		operation=1;
				if (lib.unzipFile(downloadedPath, params[1], params[2],this)) {
					//download goog, unzipped good
				} else {
					//download good, unzipped with error;
				}
			} else {
				//download with error
			};
	    	
	    /*
	      try {
	        for (int i=0; i<10; i++) {
	        	TimeUnit.SECONDS.sleep(1);
	        	doProgressUpdate(i, 9);
	        }
	      } catch (InterruptedException e) {
	        e.printStackTrace();
	      }
	      
	      operation=1;
	      
	      try {
		        for (int i=0; i<10; i++) {
		        	TimeUnit.SECONDS.sleep(1);
		        	doProgressUpdate(i, 9);
		        }
		      } catch (InterruptedException e) {
		        e.printStackTrace();
		      }
	      */
	      
	      
	      return null;
	    }
	    
	    boolean doProgressUpdate(int current, int max) {
	    	publishProgress(current, max);
	    	return cancelPressed;
	    }

	    @Override
	    protected void onProgressUpdate(Integer... values) {
	      super.onProgressUpdate(values);
	      if (v!=null) {
	    	  if (operation==1) {
	    		  TextView tv=(TextView)v.findViewById(R.id.tvStatus);
		    	  tv.setText("Installing:");
		    	  operation=2;
	    	  };
	    	  ProgressBar pbProgress=(ProgressBar) v.findViewById(R.id.pbProgress);
	    	  if (values[1]>0) {
	    		  pbProgress.setIndeterminate(false);
	    		  pbProgress.setMax(values[1]);
	    		  pbProgress.setProgress(values[0]);
	    	  } else {
	    		  pbProgress.setIndeterminate(true);
	    		  //no info about file size, but downloading started
	    	  }
	      }
	    }
	    
	    @Override
	    protected void onPostExecute(Void result) {
	      super.onPostExecute(result);
	      if (v!=null) {
	    	  TextView tv=(TextView)v.findViewById(R.id.tvStatus);
	    	  tv.setText("Download&Install complete");
	    	  ProgressBar pbProgress=(ProgressBar) v.findViewById(R.id.pbProgress);
	    	  pbProgress.setVisibility(View.INVISIBLE);
	      }
	      FillSourcesInfo();
	    }
	  }
	
}
