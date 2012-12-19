package com.realife.l9droid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;

public class LibraryGamesActivity extends Activity implements OnChildClickListener {

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
		
		Library lib=new Library();
		
		categories = new ArrayList<Map<String, String>>();
		// создаем коллекцию для коллекций элементов 
		games = new ArrayList<ArrayList<Map<String, String>>>(); 
		
		ArrayList<GameInfo> gameList=lib.getGameList(this);
		String prevCategory=null;
		
		String categoryFrom[] = new String[] {"category"};
		int categoryTo[] = new int[] {R.id.text1/*android.R.id.text1*/};

		String gameFrom[] = new String[] {"game"};
		int gameTo[] = new int[] {R.id.text1/*android.R.id.text1*/};
		
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
			m.put("id", gi.getId());
			gameItems.add(m);
		};
		if (prevCategory!=null) games.add(gameItems);
	
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				this,
				categories,
				R.layout.library_games_category_item,
				//android.R.layout.simple_expandable_list_item_1,
				categoryFrom,
				categoryTo,
				games,
				R.layout.library_games_game_item,
				//android.R.layout.simple_list_item_1,
				gameFrom,
				gameTo);
      
		elGames = (ExpandableListView) findViewById(R.id.elGames);
		elGames.setAdapter(adapter);
		elGames.setOnChildClickListener(this);
	};
	
	public boolean onChildClick(ExpandableListView parent,
			View view, int groupPosition,
			int childPosition, long id) {
		
			@SuppressWarnings("unchecked")
			String selectedGame=((Map<String,String>)(parent.getExpandableListAdapter().getChild(groupPosition, childPosition))).get("id");
			
			Intent intent=new Intent(this, LibraryGameInfoActivity.class);
			intent.putExtra("selectedgame", selectedGame);
			startActivityForResult(intent, 1);

			return true;
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
