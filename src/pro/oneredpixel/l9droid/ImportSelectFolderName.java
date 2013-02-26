package pro.oneredpixel.l9droid;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ImportSelectFolderName extends Activity implements OnClickListener {

	Library lib;
	EditText etFolder;
	String fileName;
	Button bImport;
	
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_choose_folder);
		
		
		lib=Library.getInstance();
		etFolder = (EditText) findViewById(R.id.importFolderName);
		bImport = (Button) findViewById(R.id.bImport);
		bImport.setOnClickListener(this);
		
		Intent intent = getIntent();
		
		fileName = intent.getStringExtra("selectedpath");
		
		File sdpath=new File(fileName);
		if (sdpath.isDirectory()) {
			etFolder.setText(sdpath.getName());
		} else {
			etFolder.setText(sdpath.getParentFile().getName());
		}
	};

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bImport: // кнопка ввода команды
			String to=etFolder.getText().toString();
			if (to.length()>0) {
				if (lib.importFile(fileName, to)) Toast.makeText(this, String.format("Imported: FROM=%s TO=%s",fileName,to), Toast.LENGTH_LONG).show();
				else Toast.makeText(this, String.format("Import fault: FROM=%s TO=%s",fileName,to), Toast.LENGTH_LONG).show();
				lib.invalidateInstalledVersions();
				finish();
			};
			break;
		}
	}
	
}
