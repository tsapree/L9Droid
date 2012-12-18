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
import android.widget.SimpleExpandableListAdapter;

public class LibraryGamesActivity extends Activity implements OnClickListener {

	// названия компаний (групп)
	String[] groups = new String[] {"1.The Middle Earth - Jewels of Darkness - Trilogy",
			"2.The Silicon Dreams Trilogy", "6.Individual games"};
	
	// названия телефонов (элементов)
	String[] grp1 = new String[] {"1. Colossal Adventure", "2. Adventure Quest", "3. Dungeon Adventure"};
	String[] grp2 = new String[] {"1. Snowball", "2. Return to Eden", "3. Worm in Paradise"};
	String[] grp3 = new String[] {"1. Emerald Isle", "2. The Saga of Erik the Viking"};
	
	// коллекция для групп
	ArrayList<Map<String, String>> groupData;
	
	// коллекция для элементов одной группы
	ArrayList<Map<String, String>> childDataItem;
	
	// общая коллекция для коллекций элементов
	ArrayList<ArrayList<Map<String, String>>> childData;
	// в итоге получится childData = ArrayList<childDataItem>
	
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
		
		// заполняем коллекцию групп из массива с названиями групп
		groupData = new ArrayList<Map<String, String>>();
		for (String group : groups) {
			// заполняем список аттрибутов для каждой группы
			m = new HashMap<String, String>();
			m.put("groupName", group); // имя компании
			groupData.add(m);
		}
		
		// список аттрибутов групп для чтения
		String groupFrom[] = new String[] {"groupName"};
		// список ID view-элементов, в которые будет помещены аттрибуты групп
		int groupTo[] = new int[] {android.R.id.text1};
  
		// создаем коллекцию для коллекций элементов 
		childData = new ArrayList<ArrayList<Map<String, String>>>(); 
  
		// создаем коллекцию элементов для первой группы
		childDataItem = new ArrayList<Map<String, String>>();
		// заполняем список аттрибутов для каждого элемента
		for (String phone : grp1) {
			m = new HashMap<String, String>();
			m.put("phoneName", phone); // название телефона
			childDataItem.add(m);  
		}
		// добавляем в коллекцию коллекций
		childData.add(childDataItem);

		// создаем коллекцию элементов для второй группы        
		childDataItem = new ArrayList<Map<String, String>>();
		for (String phone : grp2) {
			m = new HashMap<String, String>();
			m.put("phoneName", phone);
			childDataItem.add(m);  
		}
		childData.add(childDataItem);

		// создаем коллекцию элементов для третьей группы        
		childDataItem = new ArrayList<Map<String, String>>();
		for (String phone : grp3) {
			m = new HashMap<String, String>();
			m.put("phoneName", phone);
			childDataItem.add(m);  
		}
		childData.add(childDataItem);

		// список аттрибутов элементов для чтения
		String childFrom[] = new String[] {"phoneName"};
		// список ID view-элементов, в которые будет помещены аттрибуты элементов
		int childTo[] = new int[] {android.R.id.text1};
  
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
			this,
			groupData,
			android.R.layout.simple_expandable_list_item_1,
			groupFrom,
			groupTo,
			childData,
			android.R.layout.simple_list_item_1,
			childFrom,
			childTo);
      
		elGames = (ExpandableListView) findViewById(R.id.elGames);
		elGames.setAdapter(adapter);
        
        
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
