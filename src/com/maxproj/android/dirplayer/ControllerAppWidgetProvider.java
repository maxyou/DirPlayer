package com.maxproj.android.dirplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class ControllerAppWidgetProvider extends AppWidgetProvider {
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			
			Log.d(LocalConst.DTAG, "ControllerAppWidgetProvider.onUpdate " + i);
			
			int appWidgetId = appWidgetIds[i];

			Intent intent = new Intent(context, DirPlayerActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.notification);
			views.setOnClickPendingIntent(R.id.notification_icon, pendingIntent);
//			views.setViewVisibility(R.id.notification_infor, View.INVISIBLE);

			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
