package cn.pro47.wechatjump;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Pro47x on 2018/1/3.
 */

public class Jump {
    private static final String TAG = "Jump";

    private Context mContext;
    private int mFromX = 0;
    private int mFromY = 0;
    private int mToX = 0;
    private int mToY = 0;

    /**
     * 每毫秒的距离
     */
    private static final double MS_DISTANCE = 0.75;

    private WindowManager wm;
    private View mIndicatorView;
    private View mIndicator;
    private WindowManager.LayoutParams params;
    private TextView mTv_time;

    public Jump(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        mIndicatorView = LayoutInflater.from(mContext).inflate(R.layout.indicator, null, false);
        mIndicatorView.measure(0, 0);
        mIndicator = mIndicatorView.findViewById(R.id.indicator);
        mTv_time = mIndicatorView.findViewById(R.id.tv_time);
        mIndicatorView.findViewById(R.id.btnForm)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setForm();
                    }
                });
        mIndicatorView.findViewById(R.id.btnTo)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTo();
                    }
                });


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


    public void setForm() {
        int[] mLocation = new int[2];
        mIndicator.getLocationOnScreen(mLocation);
        int wOffset = mIndicator.getMeasuredWidth() / 2;
        int hOffset = mIndicator.getMeasuredHeight() / 2;
        mFromX = mLocation[0] + wOffset;
        mFromY = mLocation[1] + hOffset;
    }

    public void setTo() {
        int[] mLocation = new int[2];
        mIndicator.getLocationOnScreen(mLocation);
        int wOffset = mIndicator.getMeasuredWidth() / 2;
        int hOffset = mIndicator.getMeasuredHeight() / 2;
        mToX = mLocation[0] + wOffset;
        mToY = mLocation[1] + hOffset;

        int a = Math.abs(mToX - mFromX);
        int b = Math.abs(mToY - mFromY);
        double c = Math.sqrt(a * a + b * b);
        final int time = (int) (c / MS_DISTANCE);
        mTv_time.setText(String.valueOf(time));

        mFromX = 0;
        mFromY = 0;
        mToX = 0;
        mToY = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                requestGet(time);
            }
        }).start();
    }

    private void requestGet(int time) {
        try {
            StringBuilder requestUrl = new StringBuilder("http://192.168.1.140:8080/jump/JumpTime").append("?").append("time=").append(time);
            // 新建一个URL对象
            URL url = new URL(requestUrl.toString());
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接主机超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // 设置是否使用缓存  默认是true
            urlConn.setUseCaches(true);
            // 设置为Post请求
            urlConn.setRequestMethod("GET");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
            urlConn.setRequestProperty("Content-Type", "application/json");
            //设置客户端与服务连接类型
            urlConn.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            urlConn.connect();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                Log.e(TAG, "Get方式请求成功，result--->");
            } else {
                Log.e(TAG, "Get方式请求失败");
                Log.e(TAG, urlConn.getResponseMessage());
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
