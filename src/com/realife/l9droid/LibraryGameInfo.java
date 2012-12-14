package com.realife.l9droid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class LibraryGameInfo extends Activity {
	
	int selected_game_id;
	TextView tvGameName;
	TextView tvCategory;
	TextView tvAbout;
	TextView tvAuthors;
	
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
		
		for (int i=0;i<gameinfo.length;i++) {
			if (gameinfo[i][0]==selected_game_id) {
				tvCategory.setText(gameinfo[i][1]);
				tvGameName.setText(gameinfo[i][2]);
				tvAbout.setText(gameinfo[i][3]);
				tvAuthors.setText(gameinfo[i][4]);
				break;
			};
		};
	    
	};
}
