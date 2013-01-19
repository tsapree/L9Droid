package com.realife.l9droid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class RestoreGameActivity extends Activity {
	
	// имена атрибутов для Map
	final String ATTRIBUTE_NAME_NAME = "name";
	final String ATTRIBUTE_NAME_DATE = "date";
	final String ATTRIBUTE_NAME_IMAGE = "image";
	
	ListView lvStates;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restore_game);
        
     // массивы данных
        String[] texts = { "sometext 1", "sometext 2", "sometext 3",
            "sometext 4", "sometext 5" };
        String[] dates = { "01.01.01",
        		"sometext 2",
        		"sometext 3",
                "sometext 4",
                "sometext 5" };
        int img = R.drawable.ic_launcher;

        // упаковываем данные в понятную для адаптера структуру
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;
        for (int i = 0; i < texts.length; i++) {
          m = new HashMap<String, Object>();
          m.put(ATTRIBUTE_NAME_NAME, texts[i]);
          m.put(ATTRIBUTE_NAME_DATE, dates[i]);
          m.put(ATTRIBUTE_NAME_IMAGE, img);
          data.add(m);
        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { ATTRIBUTE_NAME_NAME, ATTRIBUTE_NAME_DATE,
            ATTRIBUTE_NAME_IMAGE };
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.tvName, R.id.tvDate, R.id.ivPic };

        // создаем адаптер
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.restore_game_item,
            from, to);

        // определяем список и присваиваем ему адаптер
        lvStates = (ListView) findViewById(R.id.lvStates);
        lvStates.setAdapter(sAdapter);
        
        
        
	};
}
