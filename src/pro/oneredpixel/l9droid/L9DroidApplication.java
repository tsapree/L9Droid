package pro.oneredpixel.l9droid;

import android.app.Application;
import android.content.Context;

public class L9DroidApplication extends Application {

	private static Context context;

	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		Library.initInstance();
	};

	public static Context getContext() {
		return context;
	}
}
