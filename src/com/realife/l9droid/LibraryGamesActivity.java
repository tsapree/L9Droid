package com.realife.l9droid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class LibraryGamesActivity extends Activity implements OnClickListener {
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.library_games);
	    
        findViewById(R.id.bg11).setOnClickListener(this);
        findViewById(R.id.bg12).setOnClickListener(this);
        findViewById(R.id.bg13).setOnClickListener(this);
        findViewById(R.id.bg21).setOnClickListener(this);
        findViewById(R.id.bg22).setOnClickListener(this);
        findViewById(R.id.bg23).setOnClickListener(this);
        findViewById(R.id.bg31).setOnClickListener(this);
        findViewById(R.id.bg32).setOnClickListener(this);
        findViewById(R.id.bg33).setOnClickListener(this);
        findViewById(R.id.bg41).setOnClickListener(this);
        findViewById(R.id.bg42).setOnClickListener(this);
        findViewById(R.id.bg51).setOnClickListener(this);
        findViewById(R.id.bg52).setOnClickListener(this);
        findViewById(R.id.bg61).setOnClickListener(this);
        findViewById(R.id.bg62).setOnClickListener(this);
        findViewById(R.id.bg63).setOnClickListener(this);
        findViewById(R.id.bg64).setOnClickListener(this);
        findViewById(R.id.bg65).setOnClickListener(this);
        findViewById(R.id.bg66).setOnClickListener(this);
	};
	
	public void onClick(View v) {
		Intent intent=new Intent(this, LibraryGameInfoActivity.class);
		//intent.putExtra("selectedgame", v.getId());
		intent.putExtra("selectedgame", (String)v.getTag());
		startActivityForResult(intent, 1);
		//finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
		  	  	setResult(RESULT_OK,data);
		  	  	finish();
				break;
			}
		} else {
		}
	}

}
