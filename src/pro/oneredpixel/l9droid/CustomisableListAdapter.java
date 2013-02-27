package pro.oneredpixel.l9droid;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomisableListAdapter<T> extends ArrayAdapter<T> {
	
	private Context mContext;
	private int id;
	private List <T>items ;
	
	public int textcolor;
	public int backgroundcolor;
	public int textsize;
	public Typeface texttypeface;
	public int textstyle;
	
	
	public CustomisableListAdapter(Context context, int textViewResourceId , List<T> list ) 
    {
        super(context, textViewResourceId, list);           
        mContext = context;
        id = textViewResourceId;
        items = list ;
        
        backgroundcolor = Color.WHITE;
        textcolor = Color.BLACK;
        textsize = 13;
        texttypeface = Typeface.MONOSPACE; 
    	textstyle = Typeface.NORMAL;
        
    }
	
	@Override
	public View getView(int position, View v, ViewGroup parent)
	{
		View mView = v ;
		if(mView == null) {
		    LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    mView = vi.inflate(id, null);
		}
		
		TextView text = (TextView) mView.findViewById(R.id.textView1);
		
		if(items.get(position) != null ) {
			text.setTextColor(textcolor);
			text.setBackgroundColor( backgroundcolor );
			text.setTypeface(texttypeface, textstyle);
			text.setTextSize(textsize);
			text.setText((CharSequence)items.get(position));
		}

        return mView;
    }
	
}
