package cn.pro47.wechatjump;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * x:335 y:1130
 * 10ms -> x:348 y:1122
 * a = 8, b = 13, c =
 */
public class MainActivity extends AppCompatActivity {
    private int mFromX = 0;
    private int mFromY = 0;
    private int mToX = 0;
    private int mToY = 0;

    private static final int ORIGIN_X = 335;
    private static final int ORIGIN_Y = 1130;
    /**
     * 每毫秒的距离
     */
    private static final double MS_DISTANCE = 0.75;

    private WindowManager wm;
    private View mIndicatorView;
    private View mIndicator;
    private WindowManager.LayoutParams params;
    private TextView mTv_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        mIndicatorView = LayoutInflater.from(this).inflate(R.layout.indicator, (ViewGroup) getWindow().getDecorView(), false);
        mIndicatorView.measure(0, 0);
        mIndicator = mIndicatorView.findViewById(R.id.indicator);
        mTv_time = mIndicatorView.findViewById(R.id.tv_time);
        mIndicatorView.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int newX = (int) event.getRawX();
                        int newY = (int) event.getRawY();
                        int dx = newX - startX;
                        int dy = newY - startY;
                        params.x += dx;
                        params.y += dy;
                        wm.updateViewLayout(mIndicatorView, params);
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP + Gravity.LEFT;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        wm.addView(mIndicatorView, params);
    }


    public void setForm(View view) {
        int[] mLocation = new int[2];
        mIndicator.getLocationOnScreen(mLocation);
        int wOffset = mIndicator.getMeasuredWidth() / 2;
        int hOffset = mIndicator.getMeasuredHeight() / 2;
        mFromX = mLocation[0] + wOffset;
        mFromY = mLocation[1] + hOffset;
    }

    public void setTo(View view) {
        int[] mLocation = new int[2];
        mIndicator.getLocationOnScreen(mLocation);
        int wOffset = mIndicator.getMeasuredWidth() / 2;
        int hOffset = mIndicator.getMeasuredHeight() / 2;
        mToX = mLocation[0] + wOffset;
        mToY = mLocation[1] + hOffset;

        int a = Math.abs(mToX - mFromX);
        int b = Math.abs(mToY - mFromY);
        double c = Math.sqrt(a * a + b * b);
        int time = (int) (c / MS_DISTANCE);
        mTv_time.setText(String.valueOf(time));
        Toast.makeText(getWindow().getContext(), String.valueOf(time), Toast.LENGTH_SHORT).show();

        mFromX = 0;
        mFromY = 0;
        mToX = 0;
        mToY = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getTopActivityInfo();
        }
        try {
            Runtime.getRuntime().exec("input swipe 100 100 300 300 " + time);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getTopActivityInfo() {
        String TAG = "1234";
        UsageStatsManager m = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (m != null) {
            long now = System.currentTimeMillis();
            //获取60秒之内的应用数据
            List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);
            Log.i(TAG, "Running app number in last 60 seconds : " + stats.size());
            String topActivity = "";
            //取得最近运行的一个app，即当前运行的app
            if ((stats != null) && (!stats.isEmpty())) {
                int j = 0;
                for (int i = 0; i < stats.size(); i++) {
                    if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                        j = i;
                    }
                }
                topActivity = stats.get(j).getPackageName();
            }
            Log.i(TAG, "top running app is : " + topActivity);
        }
    }

    private void start() {
        startActivityForResult(
                new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                1);
    }
}
