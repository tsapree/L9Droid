package pro.oneredpixel.l9droid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LibraryGameInstallActivity extends Activity implements OnClickListener {
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.library_game_install);
	    
	    Button bInstallDownload = (Button) findViewById(R.id.bInstallDownload);
	    bInstallDownload.setOnClickListener(this);
	    Button bInstallImport = (Button) findViewById(R.id.bInstallImport);
	    bInstallImport.setOnClickListener(this);
	    
	};
	
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.bInstallDownload:
			intent=new Intent(this, LibraryGameDownloadActivity.class);
			intent.putExtra("selectedgame", getIntent().getStringExtra("selectedgame"));			
			startActivity(intent);
			finish();
			break;
		case R.id.bInstallImport:
			intent=new Intent(this, SelectFileActivity.class);
			intent.putExtra("selectedgame", getIntent().getStringExtra("selectedgame"));
			startActivity(intent);
			finish();
			break;
		};
	};
	

}
