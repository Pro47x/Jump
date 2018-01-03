package cn.pro47.wechatjump;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

/**
 * x:335 y:1130
 * 10ms -> x:348 y:1122
 * a = 8, b = 13, c =
 */
public class MainActivity extends AppCompatActivity {
    Jump mJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mJump = new Jump(this);
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
