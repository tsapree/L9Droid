package com.realife.l9droid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;

public class LibraryGamesActivity extends Activity implements OnClickListener {

	// коллекция для категорий
	ArrayList<Map<String, String>> categories;
	// коллекция для элементов одной группы игр
	ArrayList<Map<String, String>> gameItems;
	// общая коллекция для коллекций элементов
	ArrayList<ArrayList<Map<String, String>>> games;
	// список аттрибутов группы или элемента
	Map<String, String> m;
	ExpandableListView elGames;
	
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
		
		Library lib=new Library();
		
		categories = new ArrayList<Map<String, String>>();
		// создаем коллекцию для коллекций элементов 
		games = new ArrayList<ArrayList<Map<String, String>>>(); 
		
		ArrayList<GameInfo> gameList=lib.getGameList(this);
		String prevCategory=null;
		
		String categoryFrom[] = new String[] {"category"};
		int categoryTo[] = new int[] {android.R.id.text1};

		String gameFrom[] = new String[] {"game"};
		int gameTo[] = new int[] {android.R.id.text1};
		
		for (GameInfo gi: gameList) {
			if (!gi.getCategory().equals(prevCategory)) {
				if (prevCategory!=null) games.add(gameItems);
				prevCategory=gi.getCategory();
				//добавить категорию в список
				m = new HashMap<String, String>();
				m.put("category", prevCategory);
				categories.add(m);
				gameItems = new ArrayList<Map<String, String>>();
			};
			m = new HashMap<String, String>();
			m.put("game", gi.getTitle());
			gameItems.add(m);
		};
		if (prevCategory!=null) games.add(gameItems);
	
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				this,
				categories,
				android.R.layout.simple_expandable_list_item_1,
				categoryFrom,
				categoryTo,
				games,
				android.R.layout.simple_list_item_1,
				gameFrom,
				gameTo);
      
		elGames = (ExpandableListView) findViewById(R.id.elGames);
		elGames.setAdapter(adapter);
		elGames.setOnChildClickListener(new OnChildClickListener() {
			public boolean onChildClick(ExpandableListView parent, View view,
		          int groupPosition, int childPosition, long id) {
				return true;
				//TODO: доделать обработку нажатия
			}
		});
        
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
