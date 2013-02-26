package pro.oneredpixel.l9droid;

import android.app.Application;

public class L9DroidApplication extends Application {

	public void onCreate() {
		super.onCreate();
		Library.initInstance();
	};
}
