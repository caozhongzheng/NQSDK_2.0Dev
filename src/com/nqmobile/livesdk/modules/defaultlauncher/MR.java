package com.nqmobile.livesdk.modules.defaultlauncher;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.nqmobile.live.R;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Rainbow on 2015/1/23.
 */
public class MR {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private int A;
    private int B;
    private int C;
    private int D;

    private boolean E;
    private BroadcastReceiver F;

    private ActivityManager a;
    private Context b;
    private Handler c;
    private WindowManager d;
    private View e;
    private WindowManager.LayoutParams f;
    private AlphaAnimation g;
    private AlphaAnimation h;
    private ImageView i; //select top
    private ImageView j; //select bottom
    private ImageView k; //step first
    private ImageView l; //step second

    private TextView m; //first text
    private TextView n; //second text

    private boolean o;
    private boolean p;

    private String q;

    private float r;
    private float s;
    private float t;

    private int u;
    private int v;
    private int[] w;
    private int[] x;
    private int y;
    private int z;

    // ===========================================================
    // Constructors
    // ===========================================================

    public MR(Context context){
        o = false;
        p = false;
        r = 26f;
        s = 26f;
        t = 31f;
        u = 5;
        v = 5;
        w = new int[2];
        x = new int[2];
        y = 0;
        z = 0;
        A = 0;
        B = 0;
        C = 120;
        D = 0;
        E = false;
        c = new MS();
        F = new MT();
    }

    private void b(){
        try{
            b.unregisterReceiver(F);
        }catch (Exception e){

        }
    }

    public void a(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nqmobile.action.FORCE_HIDE");
        b.registerReceiver(F,filter);
    }

    private void a(View view ,int i,int j){
        RelativeLayout.LayoutParams pa = (RelativeLayout.LayoutParams) view.getLayoutParams();
        pa.topMargin = j;
        pa.leftMargin = i;
        view.setLayoutParams(pa);
    }

    public void a(Context context,int width,int height,int[] location,int secondWidth,int secondHeight,int[] secondLocation,int statusBarHeight,String stepCheckText,boolean isOnceMore){
        Log.i("gqf","width="+width+" height="+height+" location[x]="+location[0]+" y="+location[1]+" secondWidth="+secondWidth
           +"secondHeight="+secondHeight+" secondLocation.x="+secondLocation[0]+" y="+secondLocation[1]+" statusBarHeight="+statusBarHeight+" stepCheckText="+stepCheckText);
        Log.i("gqf","isOnceMore="+isOnceMore);
        b = context.getApplicationContext();
        a = (ActivityManager) b.getSystemService(Context.ACTIVITY_SERVICE);
        d = (WindowManager) b.getSystemService(Context.WINDOW_SERVICE);
        E = isOnceMore;
        w = location;
        y = width;
        z = height;
        x = secondLocation;
        A = secondWidth;
        B = secondHeight;
        q = stepCheckText;
        DisplayMetrics displayMetrics = b.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        Log.i("gqf","density="+density);
        w[1] = w[1] - statusBarHeight;
        x[1] = x[1] - statusBarHeight;
        x[1] = (int)(x[1] - (t* density + 0.5f));
        w[1] = (int)(w[1] - (t* density + 0.5f));
        z = (int)(z + (t* density + 0.5f));
        B = (int)(B + (t * density + 0.5f));

        f = new WindowManager.LayoutParams();
        f.height = -1;
        f.width = -1;
        f.format = -3;
        f.type = 2003;
        f.flags = 1048;

        e = LayoutInflater.from(b).inflate(R.layout.launcher_select_layout,null);
        i = (ImageView) e.findViewById(R.id.launcher_select_top);
        ViewGroup.LayoutParams p1 = i.getLayoutParams();
        p1.height = z;
        p1.width = y;
        a(i,w[0],w[1]); //设置第一个框的位置

        g = new AlphaAnimation(0,1f);
        g.setStartOffset(500);
        g.setDuration(1000);

        j = (ImageView) e.findViewById(R.id.launcher_select_bottom);

        h = new AlphaAnimation(0f,1f);
        h.setStartOffset(1000);
        h.setDuration(1000);

        ViewGroup.LayoutParams p2 = j.getLayoutParams();
        p2.height = B;
        p2.width = A;
        a(j,x[0],x[1]); //设置第二个框的位置

        k = (ImageView)e.findViewById(R.id.guide_step_first);
        a(k,w[0],(int)(w[1] - (s * density  + 0.5f)));

        l = (ImageView)e.findViewById(R.id.guide_step_second);
        a(l,x[0],(int)(x[1] - (s * density  + 0.5f)));

        m = (TextView)e.findViewById(R.id.step_first_text);
        m.setWidth(y);
        m.setText(b.getString(R.string.launcher_select_guid_first_text,
                b.getString(R.string.app_name)));
        a(m,w[0] + (int)(u*density + 0.5f),w[1] + (int)(v*density + 0.5f));

        n = (TextView)e.findViewById(R.id.step_second_text);
        a(n,x[0] + (int)(u *density + 0.5f),x[1] + (int)(v*density + 0.5f));

        c.sendEmptyMessage(0);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public class MS extends Handler{
        @Override
        public void handleMessage(Message msg) {
            List<ActivityManager.RunningTaskInfo> info = a.getRunningTasks(1);
            switch (msg.what){
                case 0:
                    for(Iterator iter = info.iterator();iter.hasNext();){
                        ActivityManager.RunningTaskInfo inff = (ActivityManager.RunningTaskInfo) iter.next();
                        if (!p) {
                            if (!inff.topActivity.getClassName().equals("com.android.internal.app.ResolverActivity")) {
                                D++;
                                if (D < C) {
                                    c.sendEmptyMessageDelayed(0, 100);
                                }
                            }else{
                                try {
                                    d.removeView(e);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                d.addView(e, f);
                                o = true;
                                if (akT.b(q) && ajz.T()) {
                                    q = b.getString(R.string.launcher_select_guid_second_text_for_null);
                                }
                                if (E && ajz.aB()) {
                                    i.setVisibility(View.GONE);
                                    k.setVisibility(View.GONE);
                                    m.setVisibility(View.GONE);
                                    l.setVisibility(View.GONE);
                                    n.setText(b.getString(R.string.launcher_select_guid_second_text_for_api_nineteen, q));
                                } else {
                                    n.setText(b.getString(R.string.launcher_select_guid_second_text, q));
                                    i.startAnimation(g);
                                    k.startAnimation(g);
                                    m.startAnimation(g);
                                    l.startAnimation(h);
                                }

                                if (!ajz.T()) {
                                    n.setText(b.getString(R.string.launcher_select_guid_second_text, b.getString(R.string.app_name)));
                                    if (!akT.b(q)) {
                                        m.setText(b.getString(R.string.launcher_select_guid_first_text, q));
                                    } else {
                                        m.setText(b.getString(R.string.launcher_select_guid_first_text, b.getString(R.string.launcher_select_guid_check_first_step_text)));
                                    }
                                }

                                j.startAnimation(h);
                                n.startAnimation(h);
                                c.sendEmptyMessageDelayed(1, 500);
                            }
                        }
                    }
                    break;
                case 1:
                    for(Iterator iter = info.iterator();iter.hasNext();){
                        ActivityManager.RunningTaskInfo inff = (ActivityManager.RunningTaskInfo) iter.next();
                        if(o && !inff.topActivity.getClassName().equals("com.android.internal.app.ResolverActivity")){
                            try{
                                o = false;
                                p = true;
                                d.removeView(e);
                                b();
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                        if(!o){
                            continue;
                        }

                        c.sendEmptyMessageDelayed(1,100);
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    }

    public class MT extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(context != null && intent.getAction().equals("com.nqmobile.action.FORCE_HIDE")){
                p = true;
                if(o){
                    try{
                        p = false;
                        d.removeView(e);
                        b();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
