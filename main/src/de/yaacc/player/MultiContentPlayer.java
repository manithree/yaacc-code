/*
 * Copyright (C) 2013 www.yaacc.de 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.yaacc.player;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import de.yaacc.R;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.util.NotificationId;

/**
 * @author Tobias Schoene (openbit)
 * 
 */
@SuppressLint("ShowToast")
public class MultiContentPlayer extends AbstractPlayer {

	private int appPid;

	/**
	 * @param context
	 * @param name
	 *            playerName
	 * 
	 */
	public MultiContentPlayer(UpnpClient upnpClient, String name) {
		this(upnpClient);
		setName(name);
	}

	/**
	 * @param context
	 */
	public MultiContentPlayer(UpnpClient upnpClient) {
		super(upnpClient);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yaacc.player.AbstractPlayer#stopItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	protected void stopItem(PlayableItem playableItem) {
		if (appPid != 0) {
			Process.killProcess(appPid);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yaacc.player.AbstractPlayer#loadItem(de.yaacc.player.PlayableItem)
	 */
	@Override
	protected Object loadItem(PlayableItem playableItem) {
		// DO nothing special
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yaacc.player.AbstractPlayer#startItem(de.yaacc.player.PlayableItem,
	 * java.lang.Object)
	 */
	@Override
	protected void startItem(PlayableItem playableItem, Object loadedItem) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setDataAndType(playableItem.getUri(), playableItem.getMimeType());
		try {
			getContext().startActivity(intent);
		} catch (ActivityNotFoundException anfe) {
			Toast.makeText(getContext(),
					R.string.can_not_start_activity + anfe.getMessage(),
					Toast.LENGTH_LONG).show();

		}
		discoverStartedActivityPid();

	}

	private void discoverStartedActivityPid() {

		ActivityManager activityManager = (ActivityManager) getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager
				.getRunningTasks(Integer.MAX_VALUE);
		List<RunningAppProcessInfo> apps = activityManager
				.getRunningAppProcesses();
		String packageName = services.get(0).topActivity.getPackageName(); // fist
																			// Task
																			// is
																			// the
																			// last
																			// started
																			// task
		for (int i = 0; i < apps.size(); i++) {
			if (apps.get(i).processName.equals(packageName)) {
				appPid = apps.get(i).pid;
				Log.d(getClass().getName(),
						"Found activity process: " + apps.get(i).processName
								+ " PID: " + appPid);
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.AbstractPlayer#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (appPid != 0) {
			Process.killProcess(appPid);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.AbstractPlayer#getNotificationIntent()
	 */
	@Override
	protected PendingIntent getNotificationIntent() {
		Intent notificationIntent = new Intent(getContext(),
				MultiContentPlayerActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(),
				0, notificationIntent, 0);
		return contentIntent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yaacc.player.AbstractPlayer#getNotificationId()
	 */
	@Override
	protected int getNotificationId() {

		return NotificationId.MULTI_CONTENT_PLAYER.getId();
	}
}