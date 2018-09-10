![效果图](http://upload-images.jianshu.io/upload_images/4952738-15b5375128c51bbd.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

已经看到网上有大神用各种方式实现了，我这是属于简易版ADB命令式实现。

### 操作方法
1.光标移动到起始点，点击FORM
2.光标移动到目标点，点击TO
3.小人已经跳过去了

### 原理说明
安装APP，通过设置起点和目标点位置，获得弹跳的毫秒数，发送请求到连接手机的电脑中，电脑执行adb命令起跳。

### 具体实现
本人的测试设备是Mate9，android版本为7.0，由于在非Root环境下，普通安卓应用并不能通过Runtime.getRuntime().exec()来点击本应用外的区域，所以将手机直接通过USB调试模式连接到电脑，在点击TO按钮后，

    int a = Math.abs(mToX - mFromX);
    int b = Math.abs(mToY - mFromY);
    double c = Math.sqrt(a * a + b * b);

已知起点和终点的坐标，得到两条直角边长度，用勾股定理很容易就求出了斜边长度，经过测试，mate9每ms的弹跳距离是0.75像素，长度除0.75得到time的毫秒数，直接发起一次GET请求到电脑中发布的Servlet，然后电脑执行Runtime.getRuntime().exec("adb shell input swipe 100 100 100 100 " + time)来控制起跳，一次完美的起跳就完成了。

###源代码
源代码非常简单，就直接放在这里了

    //写在安卓APP中的起跳
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
---
    //标靶的布局文件
    <FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="150dp">

    <RelativeLayout
        android:layout_width="100dp"
        android:layout_height="wrap_content">

        <TextView
            android:id="@+id/tv_time"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerHorizontal="true"/>

        <RelativeLayout
            android:id="@+id/rl"
            android:layout_width="100dp"
            android:layout_height="100dp"
            android:layout_below="@id/tv_time">

            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:layout_centerVertical="true"
                android:background="@android:color/black"/>

            <View
                android:layout_width="1dp"
                android:layout_height="match_parent"
                android:layout_centerHorizontal="true"
                android:background="@android:color/black"/>

            <View
                android:id="@+id/indicator"
                android:layout_width="30dp"
                android:layout_height="30dp"
                android:layout_centerInParent="true"
                android:background="@drawable/mid"/>

        </RelativeLayout>

        <Button
            android:id="@+id/btnForm"
            android:layout_width="50dp"
            android:layout_height="wrap_content"
            android:layout_below="@id/rl"
            android:onClick="setForm"
            android:text="form"
            android:textSize="8sp"/>

        <Button
            android:id="@+id/btnTo"
            android:layout_width="50dp"
            android:layout_height="wrap_content"
            android:layout_alignParentRight="true"
            android:layout_below="@id/rl"
            android:onClick="setTo"
            android:text="to"
            android:textSize="8sp"/>
    </RelativeLayout>
    </FrameLayout>
---
    //Servlet文件
    public class Jump extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int time = Integer.parseInt(req.getParameter("time"));
        Runtime.getRuntime().exec("adb shell input swipe 100 100 100 100 " + time);
    }
    }

以上就是此Java版跳一跳辅助的核心内容，从此制霸排行榜不是梦φ(>ω<*)   
 ------------->(告诉一个秘密：跳太多分数会被直接删除的哟  ￣へ￣)
