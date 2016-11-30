package com.jenuine.infoWidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RemoteViews;

import java.util.Random;

/**
 * A widget that outputs a list of ip addresses together with their (network) interface name.
 */
public class InfoWidget extends AppWidgetProvider {

    private static final String TAG = "InfoWidget";
    private static final String REFRESH_CLICKED = "InfoWidget.REFRESH_CLICKED";
    private int appWidgetID = 0;
    private Context context;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // there may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        this.context = context;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.i(TAG, "Updating widget...");
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        // construct the RemoteViews object:  push the ip addresses into ipAddressesTextView

        int n = new Random().nextInt(10);
//        Log.v(TAG, String.valueOf(n));
        RemoteViews views = null;
        int[] layouts={R.layout.blue_widget,
                R.layout.green_widget,
                R.layout.red_widget,
                R.layout.orange_widget,
                R.layout.purple_widget,
                R.layout.teal_widget,
                R.layout.cyn_widget,
                R.layout.indigo_widget,
                R.layout.pink_widget,
                R.layout.yellow_widget
        };
            views = new RemoteViews(context.getPackageName(), layouts[n]);
        views.setTextViewText(R.id.ipAddressesTextView, getIpAddressesString(context));
            // refresh this widget when clicked
            Intent intent = new Intent(context, getClass());
            intent.setAction(REFRESH_CLICKED);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.ipAddressesTextView, pendingIntent);
            // instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // will be called when the user clicks on the widget
        if (REFRESH_CLICKED.equals(intent.getAction())) {
            Log.i(TAG, "Widget clicked");
//            Toast.makeText(context, context.getString(R.string.refreshing_ips), Toast.LENGTH_LONG).show();
            // forces the AppWidgetManager to refresh the widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName name = new ComponentName(context, InfoWidget.class);
            int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(name);
            onUpdate(context, appWidgetManager, widgetIDs);
        }
    }


    /**
     * Returns IP addresses, that this device has, as a formatted string.
     *
     * @param context
     * @return IP addresses string
     */
    private String getIpAddressesString(Context context) {
        String ipAddressesString;

        // get ip addresses and save them as a string in ipAddressesString
        try {
            ipAddressesString = getInfo();
        } catch (Exception e) {
            String message = context.getString(R.string.netcfg_error);
            Log.e(TAG, message, e);
            ipAddressesString = message + ":\n➥ " + e.getLocalizedMessage();
        }

        return ipAddressesString;
    }

    private String getInfo() {
        StringBuilder builder = new StringBuilder();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        String name = wifiInfo.getSSID();
        builder.append("WIFI: " + name);
        builder.append("\n");
        String ipAddressesString = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        builder.append("IP: " + ipAddressesString);
        builder.append("\n");
//        builder.append("Battery: " +batteryLevel());
//        builder.append("\n");
        builder.append("SERIAL: " + Build.SERIAL);
        builder.append("\n");
        builder.append("MODEL: " + Build.MODEL);
        builder.append("\n");
        builder.append("ID: " + Build.ID);
        builder.append("\n");
        builder.append("Manufacture: " + Build.MANUFACTURER);
        builder.append("\n");
        builder.append("brand: " + Build.BRAND);
        builder.append("\n");
        builder.append("type: " + Build.TYPE);
        builder.append("\n");
        builder.append("user: " + Build.USER);
        builder.append("\n");
        builder.append("BASE: " + Build.VERSION_CODES.BASE);
        builder.append("\n");
        builder.append("INCREMENTAL " + Build.VERSION.INCREMENTAL);
        builder.append("\n");
        builder.append("HOST " + Build.HOST);
        builder.append("\n");
        builder.append("Version Code: " + Build.VERSION.RELEASE);
        return builder.toString();
    }

    int level = 0;

    private String batteryLevel() {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }

            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        return "Battery Level Remaining: " + String.valueOf(level) + "%";
    }

}
