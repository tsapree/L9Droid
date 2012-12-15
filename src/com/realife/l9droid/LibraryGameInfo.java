package com.realife.l9droid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LibraryGameInfo extends Activity implements OnClickListener {
	
	int selected_game_id;
	TextView tvGameName;
	TextView tvCategory;
	TextView tvAbout;
	TextView tvAuthors;
	
	Button bPlay;
	
	int gameinfo[][]={
		{R.id.bg11, R.string.game1_cat_name, R.string.game11_name, R.string.game11_about, R.string.game11_author,},
		{R.id.bg12, R.string.game1_cat_name, R.string.game12_name, R.string.game12_about, R.string.game12_author,},
		{R.id.bg13, R.string.game1_cat_name, R.string.game13_name, R.string.game13_about, R.string.game13_author,},
		{R.id.bg21, R.string.game2_cat_name, R.string.game21_name, R.string.game21_about, R.string.game21_author,},
		{R.id.bg22, R.string.game2_cat_name, R.string.game22_name, R.string.game22_about, R.string.game22_author,},
		{R.id.bg23, R.string.game2_cat_name, R.string.game23_name, R.string.game23_about, R.string.game23_author,},
		{R.id.bg31, R.string.game3_cat_name, R.string.game31_name, R.string.game31_about, R.string.game31_author,},
		{R.id.bg32, R.string.game3_cat_name, R.string.game32_name, R.string.game32_about, R.string.game32_author,},
		{R.id.bg33, R.string.game3_cat_name, R.string.game33_name, R.string.game33_about, R.string.game33_author,},
		{R.id.bg41, R.string.game4_cat_name, R.string.game41_name, R.string.game41_about, R.string.game41_author,},
		{R.id.bg42, R.string.game4_cat_name, R.string.game42_name, R.string.game42_about, R.string.game42_author,},
		{R.id.bg51, R.string.game5_cat_name, R.string.game51_name, R.string.game51_about, R.string.game51_author,},
		{R.id.bg52, R.string.game5_cat_name, R.string.game52_name, R.string.game52_about, R.string.game52_author,},
		{R.id.bg61, R.string.game6_cat_name, R.string.game61_name, R.string.game61_about, R.string.game61_author,},
		{R.id.bg62, R.string.game6_cat_name, R.string.game62_name, R.string.game62_about, R.string.game62_author,},
		{R.id.bg63, R.string.game6_cat_name, R.string.game63_name, R.string.game63_about, R.string.game63_author,},
		{R.id.bg64, R.string.game6_cat_name, R.string.game64_name, R.string.game64_about, R.string.game64_author,},
		{R.id.bg65, R.string.game6_cat_name, R.string.game65_name, R.string.game65_about, R.string.game65_author,},
		{R.id.bg66, R.string.game6_cat_name, R.string.game66_name, R.string.game66_about, R.string.game66_author,},
	};
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    selected_game_id = getIntent().getIntExtra("selectedgame", R.id.bg61);
	    setContentView(R.layout.library_game_info);
	    
		tvGameName = (TextView)findViewById(R.id.tvGameName);
		tvCategory = (TextView)findViewById(R.id.tvCategory);
		tvAbout = (TextView)findViewById(R.id.tvAbout);
		tvAuthors = (TextView)findViewById(R.id.tvAuthors);
		
		LinearLayout linLayout = (LinearLayout) findViewById(R.id.llInstalled);
	    LayoutInflater ltInflater = getLayoutInflater();
	    
		View item = ltInflater.inflate(R.layout.library_game_info_item, linLayout, false);
		TextView tvVersion = (TextView) item.findViewById(R.id.tvVersion);
		tvVersion.setText("Speccy");
		Button bProperties = (Button) item.findViewById(R.id.bProperties);
		Button bPlay = (Button) item.findViewById(R.id.bPlay);
		item.setTag("/mnt/sdcard/L9Droid/Snowball zx/v3_snowball.sna");
		bPlay.setOnClickListener(this);
		
		//"/mnt/sdcard/L9Droid/Emerald Isle Speccy/emerald.sna"
		
//		item.getLayoutParams().width = LayoutParams.MATCH_PARENT;
//		item.setBackgroundColor(colors[i % 2]);
		linLayout.addView(item);

		//bPlay = (Button) findViewById(R.id.bPlay);
		//bPlay.setOnClickListener(this);
		
		for (int i=0;i<gameinfo.length;i++) {
			if (gameinfo[i][0]==selected_game_id) {
				tvCategory.setText(gameinfo[i][1]);
				tvGameName.setText(gameinfo[i][2]);
				tvAbout.setText(gameinfo[i][3]);
				tvAuthors.setText(gameinfo[i][4]);
				break;
			};
		};
	    
	}

	public void onClick(View v) {
		View p=(View)v.getParent();
		if ((p!=null) && (p.getTag()!=null)) {
			Intent intent = new Intent();
	  	  	intent.putExtra("opengame", (String)p.getTag());
	  	  	setResult(RESULT_OK,intent);
	  	  	finish();
		};
		
	};
}
