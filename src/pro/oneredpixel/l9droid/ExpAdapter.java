//author: damager82
//полностью взято отсюда http://forum.startandroid.ru/download/file.php?id=42
//переименовано из MyExpAdapter
//сообщение с файлом: http://forum.startandroid.ru/viewtopic.php?f=3&t=99&start=40#p5977
//Я склеил SimpleExpandableListAdapter и SimpleAdapter, чтобы результат умел работать не только с TextView, но и с ImageView. Странно, что этого не сделали по-дефолту. Посмотрите Урок 48 про SimpleAdapter, чтобы понять механизм.
//Файлы в архиве:
//MyExpAdapter.java - получившийся адаптер
//MainAct.java - пример из Урока 45, заточенный под новый адаптер. Сейчас везде вставляется одна и та же картинка. Меняйте ее на свои. Работает как указание id (R.drawable.icon), так и указание имени файла из assets ("icon.png").
//item_group.xml - layout для группы. Обратите внимание на атрибут android:paddingLeft. Он нужен, чтобы был отступ.
//item_child.xml - layout для элемента
//layout-файлы меняйте, как вам нужно.
//Делал поверхностно, возможны баги. Потестируйте.

package pro.oneredpixel.l9droid;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpAdapter extends BaseExpandableListAdapter {
	private List<? extends Map<String, ?>> mGroupData;
	private int mExpandedGroupLayout;
	private int mCollapsedGroupLayout;
	private String[] mGroupFrom;
	private int[] mGroupTo;

	private List<? extends List<? extends Map<String, ?>>> mChildData;
	private int mChildLayout;
	private int mLastChildLayout;
	private String[] mChildFrom;
	private int[] mChildTo;

	Context ctx;

	private LayoutInflater mInflater;

	public ExpAdapter(Context context,
			List<? extends Map<String, ?>> groupData, int groupLayout,
			String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData,
			int childLayout, String[] childFrom, int[] childTo) {
		this(context, groupData, groupLayout, groupLayout, groupFrom, groupTo,
				childData, childLayout, childLayout, childFrom, childTo);
	}

	public ExpAdapter(Context context,
			List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
			int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData,
			int childLayout, String[] childFrom, int[] childTo) {
		this(context, groupData, expandedGroupLayout, collapsedGroupLayout,
				groupFrom, groupTo, childData, childLayout, childLayout,
				childFrom, childTo);
	}

	public ExpAdapter(Context context,
			List<? extends Map<String, ?>> groupData, int expandedGroupLayout,
			int collapsedGroupLayout, String[] groupFrom, int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData,
			int childLayout, int lastChildLayout, String[] childFrom,
			int[] childTo) {
		ctx = context;

		mGroupData = groupData;
		mExpandedGroupLayout = expandedGroupLayout;
		mCollapsedGroupLayout = collapsedGroupLayout;
		mGroupFrom = groupFrom;
		mGroupTo = groupTo;

		mChildData = childData;
		mChildLayout = childLayout;
		mLastChildLayout = lastChildLayout;
		mChildFrom = childFrom;
		mChildTo = childTo;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return mChildData.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newChildView(isLastChild, parent);
		} else {
			v = convertView;
		}
		bindView(v, mChildData.get(groupPosition).get(childPosition),
				mChildFrom, mChildTo);
		return v;
	}

	public View newChildView(boolean isLastChild, ViewGroup parent) {
		return mInflater.inflate((isLastChild) ? mLastChildLayout
				: mChildLayout, parent, false);
	}

	private void bindView(View view, Map<String, ?> dataSet, String[] from,
			int[] to) {
		int len = to.length;

		for (int i = 0; i < len; i++) {
			final View v = view.findViewById(to[i]);
			if (v != null) {
				final Object data = dataSet.get(from[i]);
				String text = data == null ? "" : data.toString();
				if (text == null) {
					text = "";
				}

				if (v instanceof Checkable) {
					if (data instanceof Boolean) {
						((Checkable) v).setChecked((Boolean) data);
					} else if (v instanceof TextView) {
						setViewText((TextView) v, text);
					} else {
						throw new IllegalStateException(v.getClass().getName()
								+ " should be bound to a Boolean, not a "
								+ (data == null ? "<unknown type>"
										: data.getClass()));
					}
				} else if (v instanceof TextView) {
					setViewText((TextView) v, text);
				} else if (v instanceof ImageView) {
					if (data instanceof Integer) {
						setViewImage((ImageView) v, (Integer) data);
					} else {
						setViewImage((ImageView) v, text);
					}
				} else {
					throw new IllegalStateException(v.getClass().getName()
							+ " is not a "
							+ " view that can be bounds by this SimpleAdapter");
				}
			}

		}
	}

	public void setViewText(TextView v, String text) {
		v.setText(text);
	}

	public void setViewImage(ImageView v, int value) {
		v.setImageResource(value);
	}

	public void setViewImage(ImageView v, String value) {
		try {
			v.setImageResource(Integer.parseInt(value));
		} catch (NumberFormatException nfe) {
			InputStream bitmap = null;
			try {
				bitmap = ctx.getAssets().open(value);
				Bitmap bit = BitmapFactory.decodeStream(bitmap);
				v.setImageBitmap(bit);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					bitmap.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public int getChildrenCount(int groupPosition) {
		return mChildData.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		return mGroupData.get(groupPosition);
	}

	public int getGroupCount() {
		return mGroupData.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newGroupView(isExpanded, parent);
		} else {
			v = convertView;
		}
		bindView(v, mGroupData.get(groupPosition), mGroupFrom, mGroupTo);
		return v;
	}

	public View newGroupView(boolean isExpanded, ViewGroup parent) {
		return mInflater.inflate((isExpanded) ? mExpandedGroupLayout
				: mCollapsedGroupLayout, parent, false);
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

}
