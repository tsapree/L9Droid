package pro.oneredpixel.l9droid;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

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
	DownloadInstallFileTask mt=null;
	LibraryGameDownloadActivity activity;
	

	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.library_game_download);
	    
	    lib = Library.getInstance();
	    
	    activity = this;
	    
        mt = (DownloadInstallFileTask) getLastNonConfigurationInstance();
	    if (mt != null) {
	    	mt.act=this;
	    };
	    
	    game=getIntent().getStringExtra("selectedgame");
	    gi=lib.getGameInfo(this,game);
	    tvGameName = (TextView)findViewById(R.id.tvGameName);
		tvNoSources = (TextView)findViewById(R.id.tvNoSources);
		
		tvGameName.setText(gi.getTitle().toUpperCase());
		
	    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
	    ivBack.setOnClickListener(this);
		
		FillSourcesInfo();
		
		if (!lib.checkIfSDCardPresent()) Toast.makeText(this, "No sdcard found", Toast.LENGTH_SHORT).show();
	    
	};
	
    public Object onRetainNonConfigurationInstance() {
    	if (mt!=null) {
    		mt.act=null;
    	};
	    return mt;
	};
	
	public void FillSourcesInfo() {
		boolean downloading = mt!=null; 
		boolean sd = lib.checkIfSDCardPresent();
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.llSources);
		linLayout.removeAllViews();
	    LayoutInflater ltInflater = getLayoutInflater();
	    if (gi.getNumberOfPaths()>0) tvNoSources.setVisibility(View.GONE);
	    else tvNoSources.setVisibility(View.VISIBLE);
	    
	    for (int i=0;i<gi.getNumberOfPaths();i++) {
			View item = ltInflater.inflate(R.layout.library_game_download_item, linLayout, false);
			TextView tvVersion = (TextView) item.findViewById(R.id.tvLink);
			//fills info about this version, based on tags from parent dir
			tvVersion.setText(lib.decodeTags(gi.getTags(i)));
			TextView tvStatus = (TextView) item.findViewById(R.id.tvStatus);
			tvStatus.setVisibility(View.GONE);
			ProgressBar pbProgress=(ProgressBar) item.findViewById(R.id.pbProgress);
			pbProgress.setVisibility(View.GONE);
			
			Button bDownload = (Button) item.findViewById(R.id.bDownload);
			bDownload.setOnClickListener(this);
			bDownload.setVisibility(View.VISIBLE);
			bDownload.setEnabled(sd && !downloading);
			Button bCancel = (Button) item.findViewById(R.id.bCancel);
			bCancel.setOnClickListener(this);
			bCancel.setVisibility(View.INVISIBLE);
			if (lib.checkPathInLibrary(gi.getId()+" "+gi.getTags(i))) {
				bDownload.setText("Installed");
				bDownload.setEnabled(false);
			} else if (lib.checkFileInCache(gi.getPath(i))!=null) {
				bDownload.setText("Install");
				tvStatus.setText("Cached");
				tvStatus.setVisibility(View.VISIBLE);
			};
			
			item.setTag(i);
			if (mt!=null && mt.param.equalsIgnoreCase(gi.getPath(i)+gi.getFiles(i)+gi.getId()+" "+gi.getTags(i))) {
				mt.v=item;
				bDownload.setVisibility(View.INVISIBLE);
				bCancel.setVisibility(View.VISIBLE);
				pbProgress.setIndeterminate(true);
				pbProgress.setVisibility(View.VISIBLE);
			};
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
				
				mt = new DownloadInstallFileTask(p);
			    mt.execute(gi.getPath(index),gi.getFiles(index), gi.getId()+" "+gi.getTags(index));
			    mt.act = activity;
			    
			    break;
			case R.id.bCancel:
				showCancelDialog();
				break;
			}
			
		};
	};
	
	public void onBackPressed() {
		if (mt!=null && !mt.isCancelled() && !mt.cancelPressed) {
			showCancelDialog();
		} else super.onBackPressed();
	}
	
	private void showCancelDialog() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Cancelling...")
		.setMessage("Are you sure you want to cancel?")
		.setCancelable(true)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			if ((mt!=null) && (mt.v!=null)) {
				TextView tv=(TextView)mt.v.findViewById(R.id.tvStatus);
		    	  Button bCancel = (Button) mt.v.findViewById(R.id.bCancel);
		    	  bCancel.setEnabled(false);
			};
			if (mt!=null) mt.cancelPressed=true;
			}
		})
		.setNegativeButton("No", null)
		.show();
	}
}

class DownloadInstallFileTask extends AsyncTask<String, Integer, Void> {

	Library lib;
	View v;
	LibraryGameDownloadActivity act = null;
	int operation=0;
	String param;
	String returnMessage = null;
	String errorDescription = null;
	boolean cancelPressed=false;
	
	DownloadInstallFileTask(View view) {
		v=view;
	};
	
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
        lib = Library.getInstance();

    	if (v!=null) {
			TextView tvStatus=(TextView)v.findViewById(R.id.tvStatus);
			ProgressBar pbProgress=(ProgressBar) v.findViewById(R.id.pbProgress);
			tvStatus.setText("Downloading...");
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
    	param=params[0]+params[1]+params[2];
    	
    	String downloadedPath = lib.downloadFileToCache(params[0],this);
    	if (downloadedPath!=null) {
    		operation=1;
			if (lib.unzipFile(downloadedPath, params[1], params[2],this)) {
				//download good, unzipped good
			} else {
				//download good, unzipped with error;
				returnMessage = "Unzipped with error: "+errorDescription;
				lib.deleteFile(downloadedPath);
			}
		} else {
			//download with error
			returnMessage = "Download error: "+errorDescription;
		};

		//uncomment to testing status messages instead download
		/*
      try {
        for (int i=0; i<10; i++) {
        	TimeUnit.SECONDS.sleep(1);
        	doProgressUpdate(i, 9);
        	if (cancelPressed) {
        		operation=-1;
        		break;
        	}
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      operation=1;
      
      try {
	        for (int i=0; i<10; i++) {
	        	TimeUnit.SECONDS.sleep(1);
	        	doProgressUpdate(i, 9);
	        	if (cancelPressed) {
	        		operation=-1;
	        		break;
	        	}
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
	      ProgressBar pbProgress=(ProgressBar) v.findViewById(R.id.pbProgress);
    	  if (operation==1) {
    		  TextView tv=(TextView)v.findViewById(R.id.tvStatus);
	    	  tv.setText("Installing...");
	    	  operation=2;
    	  };
    	  if (operation==-1) {
    		  TextView tv=(TextView)v.findViewById(R.id.tvStatus);
	    	  tv.setText("Canceling...");
	    	  pbProgress.setIndeterminate(true);
    	  } else {
	    	  if (values[1]>0) {
	    		  
	    		  pbProgress.setIndeterminate(false);
	    		  pbProgress.setMax(values[1]);
	    		  pbProgress.setProgress(values[0]);

	    		  TextView tv=(TextView)v.findViewById(R.id.tvStatus);
	    		  if (operation==0) {
	    			  tv.setText(String.format("Downloading %d / %d kB",values[0]/1024,values[1]/1024) );
	    		  } else {
	    			  tv.setText(String.format("Installing %d / %d kB",values[0]/1024,values[1]/1024) );
	    		  }
	    			  
	    		  
	    	  } else {
	    		  pbProgress.setIndeterminate(true);
	    		  //no info about file size, but downloading started
	    	  }
    	  };
      }
    }
    
    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      lib.invalidateInstalledVersions();
      if (returnMessage!=null && act!=null) Toast.makeText(act, returnMessage, Toast.LENGTH_SHORT).show();
      if (act!=null) {
    	  act.mt=null;
    	  act.FillSourcesInfo();
      };
    }
  }

