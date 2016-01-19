package barqsoft.footballscores;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import barqsoft.footballscores.service.myFetchService;

/**
 * Created by ephrem.shiferaw on 1/11/16.
 */

public class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {


    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;

    private int mCount = 0;
    private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
    private List<String> mWidgetHeaders = new ArrayList<String>();
    private Context mContext;
    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {

        mWidgetItems.clear();
        mWidgetHeaders.clear();
        mCount = 0;
        update_scores();
        refresh();
    }

    private void refresh() {
        try {
            ScoresProvider a = new ScoresProvider();
            a.SetContext(mContext);
            // Queries the user dictionary and returns results
            Cursor cursor = a.getScores();
            cursor.moveToFirst();
            mWidgetItems.clear();
            mWidgetHeaders.clear();

            String matchDay = "";
            int headerId = -1;
            while (!cursor.isAfterLast()) {


                WidgetItem w = new WidgetItem();
                w.matchDay = cursor.getString(COL_DATE);
                w.isFirstForDay = false;
                if (!w.matchDay.equals(matchDay)) {

                    if (headerId >= 5)
                        break;
                    headerId++;
                    w.isFirstForDay = true;
                    matchDay = w.matchDay;

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(matchDay);
                    System.out.println(date.getTime());
                    w.matchDayName = Utilies.getDayName(mContext, date.getTime());


                    mWidgetHeaders.add(w.matchDayName);

                }
                w.home_name = cursor.getString(COL_HOME);
                w.away_name = cursor.getString(COL_AWAY);
                w.date = cursor.getString(COL_MATCHTIME);
                w.score = Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS));

                if (headerId == 0)
                    w.isFirst = true;
                mWidgetItems.add(w);


                cursor.moveToNext();

            }
            mCount = mWidgetItems.size();

            // You can do heaving lifting in here, synchronously. For example, if you need to
            // process an image, fetch something from the network, etc., it is ok to do it here,
            // synchronously. A loading view will show up in lieu of the actual contents in the
            // interim.}


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        WidgetItem w = mWidgetItems.get(position);
        RemoteViews rv = null;
        if (!w.isFirstForDay) {
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);
            rv.setTextViewText(R.id.home_name_textview, w.home_name);
            rv.setTextViewText(R.id.away_name_textview, w.away_name);
            rv.setTextViewText(R.id.score_textview, w.score);
            rv.setTextViewText(R.id.match_day_textview, w.date);


        } else {//header
            rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_header);
            rv.setTextViewText(R.id.home_name_textview, w.matchDayName);
            rv.setViewVisibility(R.id.refresh, View.GONE);
            if (w.isFirst) {
                rv.setViewVisibility(R.id.refresh, View.VISIBLE);
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Intent.EXTRA_TEXT, position);
                rv.setOnClickFillInIntent(R.id.refresh, fillInIntent);
            }
        }

        return rv;
    }


    private void update_scores() {
        Intent service_start = new Intent(mContext, myFetchService.class);
        mContext.startService(service_start);
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }


}

class WidgetItem {
    public String home_name;
    public String away_name;
    public String date;
    public String score;
    public String matchDay;
    public String matchDayName;
    public boolean isFirstForDay;
    public boolean isFirst;
}

