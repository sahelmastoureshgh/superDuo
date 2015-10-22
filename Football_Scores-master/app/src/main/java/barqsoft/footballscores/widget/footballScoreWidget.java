package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import barqsoft.footballscores.service.MyFetchService;

/**
 * Implementation of App Widget functionality.
 */
public class FootballScoreWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        FootballScoreWidgetIntentService.startActionFootballScoreUpdate(context);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        FootballScoreWidgetIntentService.startActionFootballScoreUpdate(context);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (MyFetchService.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            FootballScoreWidgetIntentService.startActionFootballScoreUpdate(context);
        }
    }
}

