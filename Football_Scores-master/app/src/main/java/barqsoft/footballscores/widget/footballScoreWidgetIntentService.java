package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class footballScoreWidgetIntentService extends IntentService {
    private static final String ACTION_FOOTBALL_SCORE_UPDATE = "barqsoft.footballscores.widget.action.FOOTBALL_SCORE_UPDATE";
    private static final String[] FOOTBALLSCORE_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };
    // these indices must match the projection
    private static final int INDEX_MATCH_ID = 0;
    private static final int INDEX_DATE = 1;
    private static final int INDEX_TIME = 2;
    private static final int INDEX_HOME = 3;
    private static final int INDEX_AWAY = 4;
    private static final int INDEX_HOME_GOALS = 5;
    private static final int INDEX_AWAY_GOALS = 6;


    /**
     * Starts this service to perform action football score update . If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFootballScoreUpdate(Context context) {
        Intent intent = new Intent(context, footballScoreWidgetIntentService.class);
        intent.setAction(ACTION_FOOTBALL_SCORE_UPDATE);
        context.startService(intent);
    }


    public footballScoreWidgetIntentService() {
        super("footballScoreWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOOTBALL_SCORE_UPDATE.equals(action)) {
                handleActionFootballScoreUpdate();
            }
        }
    }

    /**
     * Handle action Football score update in the provided background thread
     */
    private void handleActionFootballScoreUpdate() {

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                footballScoreWidget.class));

        String todayDate = Utilies.getFormattedDate(System.currentTimeMillis());
        Uri FootballScoreUri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(FootballScoreUri, FOOTBALLSCORE_COLUMNS, null,
                new String[] { todayDate }, DatabaseContract.scores_table.HOME_GOALS_COL + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // extracting  data from the Cursor
        String homeTeam = data.getString(INDEX_HOME);
        String awayTeam = data.getString(INDEX_AWAY);
        int homeGoals = data.getInt(INDEX_HOME_GOALS);
        int awayGoals = data.getInt(INDEX_AWAY_GOALS);
        String currentTime = data.getString(INDEX_TIME);
        data.close();

        // Perform this loop procedure for each Score widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.football_score_widget;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setTextViewText(R.id.home_name, homeTeam);
            views.setTextViewText(R.id.away_name, awayTeam);
            views.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awayGoals));
            views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(homeTeam));
            views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(awayTeam));
            views.setTextViewText(R.id.score_textview, Utilies.getScores(
                    homeGoals, awayGoals));
            views.setTextViewText(R.id.time_textview, currentTime);




            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, R.id.home_crest, homeTeam);
                setRemoteContentDescription(views, R.id.away_crest, awayTeam);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.score_textview, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views,int viewId, String description) {
        views.setContentDescription(viewId, description);
    }


}
