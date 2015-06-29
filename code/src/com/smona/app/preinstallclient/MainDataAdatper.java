package com.smona.app.preinstallclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.download.DownloadInfo;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.ContainerSpace;
import com.smona.app.preinstallclient.view.Element;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;

@SuppressLint({ "InflateParams", "ViewHolder" })
public class MainDataAdatper extends AbstractDataAdapter {

	private ArrayList<View> mVisibleViews = new ArrayList<View>();

	public MainDataAdatper(Context context, IDataSource datasource) {
		super(context, datasource);
	}

	@Override
	public View createView(int position, View convertView, ViewGroup parent) {
		ItemInfo info = (ItemInfo) mDataSource.getInfo(position);
		if (convertView == null) {
			convertView = createElement();
		}

		setConvertView(convertView, info);
		return convertView;
	}

	@SuppressLint("NewApi")
	private View createElement() {
		View convertView = mLayoutInflater.inflate(R.layout.item_main, null);
		mVisibleViews.add(convertView);
		return convertView;
	}

	private void setConvertView(View convertView, ItemInfo info) {
		((Element) convertView).initUI(info);
	}

	public int getNeedCount() {
		return mDataSource.getCount(false);
	}

	public void refreshUI(HashMap<String, DownloadInfo> values) {
		super.refreshUI(values);

		if (values.size() == 0) {
			return;
		}
		LogUtil.d("values", "values: " + values);
		ItemInfo tag;
		for (View view : mVisibleViews) {
			tag = (ItemInfo) view.getTag();
			if (tag != null) {
				DownloadInfo info = values.get(tag.appUrl);
				LogUtil.d("tag", "tag: " + tag + ",info: " + info);
				if (info == null) {
					continue;
				}
				((Element) view).onProgress(info.process);
				((Element) view).onStatusChange(info.status);
			}
		}
	}
}
