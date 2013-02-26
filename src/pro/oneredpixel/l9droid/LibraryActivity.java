package pro.oneredpixel.l9droid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LibraryActivity extends Activity implements OnMenuItemClickListener {
	
	ListView lvMain;
	ArrayList<String> paths;
	Library lib; //TODO: временно - свой экземпл€р дл€ доступа к библиотеке
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.library);
	  }
	
    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem mi;
    	mi = menu.add(0,1,0,"Import");
    	mi.setOnMenuItemClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onMenuItemClick(MenuItem arg0) {
		switch (arg0.getItemId()) {
		case 1: //library TODO: переделать в id, возможно перенести меню в ресурсы
			Intent intent=new Intent(this, ImportActivity.class);
			startActivityForResult(intent, 1); //TODO: "1"-change it or kill ))
			//mi.setIntent(intent);
			break;
		};
		return false;
	}
    
    protected void onResume() {
    	super.onResume();
	    lib=Library.getInstance();
	    lib.prepareLibrary(this);
	    
	    //paths = getResources().getStringArray(R.array.lib_paths);
	    paths=lib.getPaths();
	    lvMain = (ListView) findViewById(R.id.lvMain);

	    //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	    //    this, R.array.lib_paths, android.R.layout.simple_list_item_1);
	    if (paths!=null) {
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		    		android.R.layout.simple_list_item_1, paths);
		    lvMain.setAdapter(adapter);
	
		    lvMain.setOnItemClickListener(new OnItemClickListener() {
		      public void onItemClick(AdapterView<?> parent, View view,
		          int position, long id) {
		    	  Intent intent = new Intent();
		    	  intent.putExtra("opengame",/*view.toString()*/ paths.get(position));
		    	  setResult(RESULT_OK,intent);
		    	  finish();
		      }
		    });
	    };
    }
    
}
