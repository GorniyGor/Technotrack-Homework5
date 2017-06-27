package com.example.gor.myhomies5;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    static final String ACTION_SET_BUTTON = "0";
    static final String ACTION_PLUS_BUTTON = "+1";
    static final String ACTION_MINUS_BUTTON = "-1";
    private static final String ACTION = "Nothing";

    public static final String WIDGET_PREF = "widget_pref";
    public static final String WIDGET_COUNT = "widget_count" ;

    final String LOG_TAG = "myLogs";


    AlarmManager alarmManager = null;
    NotificationManager notificationManager;


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Read counter value
        SharedPreferences sharPref = context.
                getSharedPreferences(AppWidget.WIDGET_PREF, Context.MODE_PRIVATE);
        String countStr = String.valueOf(sharPref.getInt(AppWidget.WIDGET_COUNT + appWidgetId, 10));

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        // Place data (counter) into the text field
        views.setTextViewText(R.id.nameId_Counter, countStr);

        //------------------------------------------------------------------------------------------
        // Prepare Intent for Broadcast
        //      For decrease timer-counter (have been pressed button "-")
        //      use method getPendingIntentActionMinus() because we need it in onReceive()

        //      For raise timer-counter (have been pressed button "+")
        Intent intentActionPlus = new Intent(context, AppWidget.class);
        intentActionPlus.setAction(ACTION_PLUS_BUTTON);
        intentActionPlus.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //      For star timer (have been pressed button "Set")
        Intent intentActionSet = new Intent(context, AppWidget.class);
        intentActionSet.setAction(ACTION_SET_BUTTON);
        intentActionSet.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        // Create our event
        PendingIntent pendingIntentActionMinus = getPendingIntentActionMinus(context, appWidgetId);
        PendingIntent pendingIntentActionPlus = PendingIntent
                .getBroadcast(context, appWidgetId, intentActionPlus, 0);
        PendingIntent pendingIntentActionSet = PendingIntent
                .getBroadcast(context, appWidgetId, intentActionSet, 0);

        // Register our event
        views.setOnClickPendingIntent(R.id.nameId_Minus, pendingIntentActionMinus);
        views.setOnClickPendingIntent(R.id.nameId_Plus, pendingIntentActionPlus);
        views.setOnClickPendingIntent(R.id.nameId_Set, pendingIntentActionSet);


        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static PendingIntent getPendingIntentActionMinus (Context context, int appWidgetId){
        //      For decrease timer-counter (have been pressed button "-")
        Intent intentActionMinus = new Intent(context, AppWidget.class);
        intentActionMinus.setAction(ACTION_MINUS_BUTTON);
        intentActionMinus.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intentActionMinus, 0);
    }

    @Override
    public void onReceive (Context context, Intent intent){
        super.onReceive(context, intent);

        if(intent.getAction().equals(ACTION_MINUS_BUTTON) ||
                intent.getAction().equals(ACTION_PLUS_BUTTON)||
                intent.getAction().equals(ACTION_SET_BUTTON)) {

            // Extract ID
            int mAppWidgetId =
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);

            // Read counter value
            SharedPreferences sharPref = context.
                    getSharedPreferences(AppWidget.WIDGET_PREF, Context.MODE_PRIVATE);
            int counter = sharPref.getInt(AppWidget.WIDGET_COUNT + mAppWidgetId, 10);

            Log.d(LOG_TAG, "onReceive");

            if (intent.getAction().equals(ACTION_MINUS_BUTTON)) {
                Log.d(LOG_TAG, "equals(ACTION_MINUS_BUTTON)");
                if(counter > 0 ) {
                    sharPref.edit().
                            putInt(AppWidget.WIDGET_COUNT + mAppWidgetId, --counter).apply();
                    Log.d(LOG_TAG, "ACTION_MINUS_BUTTON");
                }
                else {
                    // Cancel the alarm, which was created by button "Set"
                    AlarmManager alarmManagerCancled = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManagerCancled.cancel(getPendingIntentActionMinus(context, mAppWidgetId));
                    // Departure notification of cancel the alarm
                    notificationManager = (NotificationManager) context.
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, getNotification(context, mAppWidgetId));
                }

            }
            if (intent.getAction().equals(ACTION_PLUS_BUTTON)) {
                Log.d(LOG_TAG, "equals(ACTION_PLUS_BUTTON)");
                if(counter < 99 ){
                    sharPref.edit().
                            putInt(AppWidget.WIDGET_COUNT + mAppWidgetId, ++counter).apply();
                    Log.d(LOG_TAG, "ACTION_PLUS_BUTTON");
                }
            }
            if (intent.getAction().equals(ACTION_SET_BUTTON)) {
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000,
                        getPendingIntentActionMinus(context, mAppWidgetId));

            }
            updateAppWidget(context, AppWidgetManager.getInstance(context),mAppWidgetId);
        }

    }

    private Notification getNotification(Context context, int appWidgetId ) {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(ACTION);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);

        Notification notification  = new Notification.Builder(context)
                .setContentIntent(pIntent)
                .setTicker("Alarm")
                .setSmallIcon(R.drawable.example_appwidget_preview)
                .setContentTitle("Alarm " + appWidgetId)
                .setContentText("Timer has worked")
                .setAutoCancel(true)
                .build();

        return notification;
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Enter relevant functionality for when the last widget is deleted
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                AppWidget.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(AppWidget.WIDGET_COUNT + widgetID);
        }
        editor.apply();
    }

}

