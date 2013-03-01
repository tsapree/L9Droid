package pro.oneredpixel.l9droid;

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
import android.widget.ImageView;

public class LibraryGamesActivity extends Activity implements OnChildClickListener, OnClickListener {

	ExpandableListView elGames;
	Library lib;
	String lastSelectedGame;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.library_games);
		
		lastSelectedGame=null;
		
	    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
	    ivBack.setOnClickListener(this);
	    
		elGames = (ExpandableListView) findViewById(R.id.elGames);
		elGames.setOnChildClickListener(this);

		lib=Library.getInstance();
		
	    fillInfo();
	    
	    if (lib.getGamePath()!=null) {
	    	GameInfo gi=lib.getGameInfo(this,lib.getFileNameWithoutPath(lib.getFolder(lib.getGamePath())));
	    	Intent intent=new Intent(this, LibraryGameInfoActivity.class);
			intent.putExtra("selectedgame", gi.getId());
			lastSelectedGame = gi.getId();
			startActivityForResult(intent, 1);
	    }
	};
	
	public void fillInfo() {

		// коллекция для категорий
		ArrayList<Map<String, Object>> categories;
		// коллекция для элементов одной группы игр
		ArrayList<Map<String, Object>> gameItems=null;
		// общая коллекция для коллекций элементов
		ArrayList<ArrayList<Map<String, Object>>> games;
		// список аттрибутов группы или элемента
		Map<String, Object> m;
		
		int group = -1;
		int child = -1;
		
		categories = new ArrayList<Map<String, Object>>();
		// создаем коллекцию для коллекций элементов 
		games = new ArrayList<ArrayList<Map<String, Object>>>(); 
		
		ArrayList<GameInfo> gameList=lib.getGameList(this);
		String prevCategory=null;
		
		String categoryFrom[] = new String[] {"category"};
		int categoryTo[] = new int[] {R.id.text1/*android.R.id.text1*/};

		String gameFrom[] = new String[] {"game","mark"};
		int gameTo[] = new int[] {R.id.text1,R.id.ivMark};
		
		for (GameInfo gi: gameList) {
			if (!gi.getCategory().equals(prevCategory)) {
				if (prevCategory!=null) games.add(gameItems);
				prevCategory=gi.getCategory();
				//добавить категорию в список
				m = new HashMap<String, Object>();
				m.put("category", prevCategory);
				categories.add(m);
				gameItems = new ArrayList<Map<String, Object>>();
			};
			m = new HashMap<String, Object>();
			m.put("game", gi.getTitle());
			m.put("id", gi.getId());
			int mark;
			mark = Library.MARK_INFO;
			if (!gi.getId().startsWith("info_")) 
				mark = gi.getHighestMark();
			m.put("mark", Library.MARK_PICTURES_RESID[mark]);
			gameItems.add(m);
			if ( (lastSelectedGame!=null) && (gi.getId().equalsIgnoreCase(lastSelectedGame)) ) {
				group=categories.size()-1;
				child=gameItems.size()-1;
			};
		};
		if (prevCategory!=null) games.add(gameItems);
	
		//SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
		ExpAdapter adapter = new ExpAdapter(
				this,
				categories,
				R.layout.library_games_category_item,
				categoryFrom,
				categoryTo,
				games,
				R.layout.library_games_game_item,
				gameFrom,
				gameTo);

		elGames.setAdapter(adapter);
		if ((group>=0) && (child>=0))  {
			elGames.expandGroup(group);
			elGames.setSelectedChild(group, child, true);
			
		}
			
	};
	
	public boolean onChildClick(ExpandableListView parent,
			View view, int groupPosition,
			int childPosition, long id) {
		
			@SuppressWarnings("unchecked")
			String selectedGame=((Map<String,String>)(parent.getExpandableListAdapter().getChild(groupPosition, childPosition))).get("id");
			lastSelectedGame = selectedGame;
			
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
			fillInfo();
		}
	}
	
	public void onClick(View v) {
		if (v.getId()==R.id.ivBack) {
			onBackPressed();
		};
	}

}
