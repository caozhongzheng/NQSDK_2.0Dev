package com.nqmobile.livesdk.modules.defaultlauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.utils.MResource;

import java.util.*;

/**
 * Created by Rainbow on 2015/1/23.
 */
public class ResolverActivity extends AlertActivity {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private MX a;
    private PackageManager b;
    private boolean c;
    private boolean d;
    private AbsListView e;
    private Button f;
    private Button g;
    private int h;
    private static int i;
    private CheckBox j;
    private AlertController.AlertParams k;
    private static Resources l;
    private boolean m;
    private int n;
    private static int o = 0;
    private static int p = 0;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String v3 = "";
        Intent v2 = null;
        int id = 0;
        Intent[] v4 = null;
        try{
            l = getPackageManager().getResourcesForActivity(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
            if(l == null){
                super.onCreate(savedInstanceState);
                finish();
                return;
            } else {
                int v0_1 = l.getIdentifier("Theme.OPPO.Dialog.Alert", "style", "oppo");
                if (v0_1 != 0) {
                    m = true;
                    setTheme(v0_1);
                }

                if(("HTC".equals(Build.MANUFACTURER)) || Build.VERSION.SDK_INT == 15 || ("samsung".equals(
                        Build.MANUFACTURER))) {
                    setTheme(ResolverActivity.l.getIdentifier("Theme.DeviceDefault.Dialog.Alert", "style",
                            "android"));
                } else {
                    setTheme(ResolverActivity.l.getIdentifier("Theme.DeviceDefault.Light.Dialog.Alert",
                            "style", "android"));
                }

                v2 = makeMyIntent();
                Set<String> cate = v2.getCategories();

                if (("android.intent.action.MAIN").equals(v2.getAction()) && cate != null && cate.size() == 1 && cate.contains("android.intent.category.HOME")) {
                    id = l.getIdentifier("whichHomeApplication", "string", "android");
                } else {
                    id = l.getIdentifier("whichApplication", "string", "android");
                }

                if(Build.VERSION.SDK_INT == 15 || Build.MANUFACTURER.equals("samsung")){
                    id = l.getIdentifier("whichApplication", "string", "android");
                }

                if(id > 0){
                    v3 = getResources().getText(id).toString();
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
            super.onCreate(savedInstanceState);
            finish();
            return;
        }

        onCreate(savedInstanceState,v2,v3,null,null,true);
    }

    protected void onCreate(Bundle bundle,Intent intent,CharSequence c,Intent[] i2,List<ResolveInfo> list,boolean boo){
        super.onCreate(bundle);
        try {
            this.b = this.getPackageManager();
            this.c = boo;
            this.k = this.mAlertParams;
            this.k.mTitle = c;
            ActivityManager am = (ActivityManager) getSystemService("activity");
            int[] v1 = new int[2];
            OK.a(am,v1);
            h = v1[0];
            i = v1[1];
            int id = l.getIdentifier("resolver_grid","layout","android");
            if(id > 0){
                k.mView = getLayoutInflater().inflate(id, null);
                e = (AbsListView) k.mView.findViewById(l.getIdentifier("resolver_grid", "id", "android"));
            }else {
                id = l.getIdentifier("resolver_list", "layout", "android");
                if (id > 0) {
                    k.mView = getLayoutInflater().inflate(id, null);
                    e = (AbsListView) k.mView.findViewById(l.getIdentifier("resolver_list", "id", "android"));
                }
            }

            if(!ajz.T()){
                setTheme(l.getIdentifier("Theme.DeviceDefault.Dialog.Alert", "style", "android"));
                a = new MX(this, intent, i2, list);
                k.mAdapter = a;
                int checkId = l.getIdentifier("always_use_checkbox","layout","android");
                k.mView = getLayoutInflater().inflate(l.getLayout(checkId), null);
                j = (CheckBox) k.mView.findViewById(l.getIdentifier("alwaysUse", "id", "android"));
                j.setText(l.getIdentifier("alwaysUse", "string", "android"));
                k.mView.setVisibility(View.INVISIBLE);
                getWindow().setBackgroundDrawable(new ColorDrawable(0));
                setupAlert();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)((FrameLayout )getWindow().getDecorView()).getChildAt(0).getLayoutParams();
                params.height = -0x2;
                params.gravity = 0x10;
                ((FrameLayout)getWindow().getDecorView()).getChildAt(0).setLayoutParams(params);
                k.mView.getViewTreeObserver().addOnGlobalLayoutListener(new MU());
            }else{
                setupAlert();
            }

            if(e != null) {
                a = new MX(this, intent, i2, list);
                if(ajz.o()) {
                    e.setPadding(0, 0, 0, 0);
                    ViewGroup.LayoutParams  pa = e.getLayoutParams();
                    if(pa == null) {
                        pa = new ViewGroup.LayoutParams(-1, ResolverActivity.i * 4);
                    } else {
                        pa.height = ResolverActivity.i * 4;
                    }
                }

                this.e.setAdapter(this.a);
            }

            if(boo){
                OK.a(e);
            }

            int v1_1 = a.getCount();
            if((e instanceof GridView)) {
                ((GridView)e).setNumColumns(Math.min(v1_1, getResources().getInteger(l.getIdentifier("config_maxResolverActivityColumns", "integer", "android"))));
                if(ajz.o()) {
                    ((GridView)e).setNumColumns(1);
                }
            }

            if((boo) && a != null) {
                ViewGroup v0_4 = (ViewGroup) findViewById(ResolverActivity.l.getIdentifier("button_bar", "id", "android"));
                if(v0_4 != null) {
                    v0_4.setVisibility(View.VISIBLE);
                    f = (Button)v0_4.findViewById(ResolverActivity.l.getIdentifier("button_always", "id", "android"));
                    g = (Button)v0_4.findViewById(ResolverActivity.l.getIdentifier("button_once", "id", "android"));
                }else {
                    this.c = false;
                }

                int v0_2 = a.b();
                if(v0_2 >= 0) {
                    OK.a(e, v0_2);
                }
            }

            ((FrameLayout)getWindow().getDecorView()).getChildAt(0).setVisibility(View.INVISIBLE);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        ResolverActivity.l = null;
        super.onDestroy();
    }

    protected void onRestart() {
        super.onRestart();
        this.a.a();
    }

    public static void handleActivityResult(Context context, int resultCode, Intent intent, boolean isOnceMore) {
        if(resultCode != Activity.RESULT_OK || intent == null) {
            KP.b(context, true);
        }else {
            MR mr = new MR(context);
            mr.a(context, intent.getIntExtra("width", 0), intent.getIntExtra("height", 0), intent.getIntArrayExtra(
                    "location"), intent.getIntExtra("secondWidth", 0), intent.getIntExtra("secondHeight",
                    0), intent.getIntArrayExtra("secondLocation"), intent.getIntExtra("statusBarHeight",
                    0), intent.getStringExtra("stepCheckText"), isOnceMore);
            mr.a();
            KP.b(context, false);
        }
    }

    private Intent makeMyIntent() {
        Intent i = new Intent(getIntent());
        i.setComponent(new ComponentName(ClientInfo.getPackageName(), "com.lqsoft.launcher.LiveLauncher"));
        i.setFlags(i.getFlags() & -0x800001);
        return i;
    }

    private static void onLocationLocated(Activity activity,View v1,View v2){
        Log.i("gqf","onLocationLocated !");
        String s1;
        String s2;
        try{
            Rect rec = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rec);
            int top = rec.top;
            int id = 0;
            if(ajz.T()){
                id = l.getIdentifier("activity_resolver_use_always", "string", "android");
            }else{
                id = l.getIdentifier("alwaysUse", "string", "android");
            }

            String str = null;
            if(id > 0){
                str = activity.getString(id);
            }

            if(str == null){
                str = MResource.getString(activity,"string","launcher_select_guid_second_text_for_null");
            }

            s1 = str;

            Intent intent = new Intent();
            int[] a = new int[2];
            v1.getLocationOnScreen(a);
            int[] b = new int[2];
            v2.getLocationOnScreen(b);

            int height = v2.getMeasuredHeight();
            if(ajz.n()){
                b[1] = p;
                a[1] = o;
                height = i;
            }

            intent.putExtra("width", v1.getMeasuredWidth());
            intent.putExtra("height", v1.getMeasuredHeight());
            intent.putExtra("location", a);
            intent.putExtra("secondWidth", v2.getMeasuredWidth());
            intent.putExtra("secondHeight", height);
            intent.putExtra("secondLocation", b);
            intent.putExtra("statusBarHeight", top);
            intent.putExtra("stepCheckText", s1);
            activity.setResult(Activity.RESULT_OK, intent);
        }catch (Throwable e){
            e.printStackTrace();
        }

        activity.finish();
    }

    public Drawable loadIconForResolveInfo(ResolveInfo arg4) {
        Drawable v0_1 = null;
        try {
            if (arg4.resolvePackageName != null && arg4.icon != 0) {
                v0_1 = this.getIcon(this.b.getResourcesForApplication(arg4.resolvePackageName), arg4.icon);
            }

            if (v0_1 == null) {
                int id = arg4.getIconResource();
                if (id > 0) {
                    v0_1 = this.getIcon(this.b.getResourcesForApplication(arg4.activityInfo.packageName), id);
                    if(v0_1 != null){
                        return v0_1;
                    }
                }
            }else{
                return v0_1;
            }
        } catch (PackageManager.NameNotFoundException v0) {
            v0.printStackTrace();
        }

        return arg4.loadIcon(b);
    }

    private Drawable getIcon(Resources res,int i){
        int j = h;
        return OK.a(res,i,j);
    }

    void startSelected(int arg1, boolean arg2) {
    }


    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public class MU implements ViewTreeObserver.OnGlobalLayoutListener{

        @Override
        public void onGlobalLayout() {
            Log.i("gqf","MU onGlobalLayout");
            k.mView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            if(ajz.n()) {
                Rect v0 = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(v0);
                int v0_1 = (960 - v0.top - (e.getLastVisiblePosition() - e.getFirstVisiblePosition()) * i + 100 + k.mView.getMeasuredHeight()) / 2 + 100;
                p = (n - e.getFirstVisiblePosition()) * i + v0_1;
                o = (v0_1 + e.getLastVisiblePosition() - e.getFirstVisiblePosition() + 1) * i;
            }

            onLocationLocated(ResolverActivity.this, k.mView, mAlert.getListView().getChildAt(n));
        }
    }

    public class MV{
        private ResolveInfo a;
        private CharSequence b;
        private Drawable c;
        private CharSequence d;

        public MV(ResolveInfo a,CharSequence b,CharSequence c){
            this.a = a;
            this.b = b;
            this.d = c;
        }
    }

    public class MZ{
        android.widget.TextView a;
        android.widget.TextView b;
        ImageView c;

        public MZ(View view){
            a = (android.widget.TextView) view.findViewById(l.getIdentifier("text1","id","android"));
            b = (android.widget.TextView) view.findViewById(l.getIdentifier("text2","id","android"));
            c = (ImageView) view.findViewById(l.getIdentifier("icon","id","android"));
        }
    }

    public class MW extends AsyncTask<MV,MV,MV>{

        @Override
        protected MV doInBackground(MV... mvs) {
            MV[] m = mvs;
            MV a = m[0];
            if(a.c == null){
                a.c = loadIconForResolveInfo(a.a);
            }
            return a;
        }

        @Override
        protected void onPostExecute(MV mv) {
            a.notifyDataSetChanged();
        }
    }

    public class Na{
        public CheckedTextView a;

        public Na(View v){
            a = (CheckedTextView) v.findViewById(ResolverActivity.l.getIdentifier("text1","id","android"));
        }
    }

    public class MX extends BaseAdapter{

        List<MV> aa;
        List bb;
        private final Intent[] dd;
        private final List ee;
        private ResolveInfo ff;
        private final LayoutInflater gg;
        private int hh;

        public MX(Context arg3, Intent arg4, Intent[] arg5, List arg6) {
            this.hh = -1;
            this.dd = arg5;
            this.ee = arg6;
            this.gg = (LayoutInflater)arg3.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.aa = new ArrayList<MV>();
            this.c();
        }

        private final void a(View view, MV mv) {
            if(!ajz.o()) {
                Object v0 = view.getTag();
                ((MZ)v0).a.setText(mv.b);
                if(d && !ajz.ai()) {
                    ((MZ)v0).b.setVisibility(View.VISIBLE);
                    ((MZ)v0).b.setText(mv.d);
                }
                else {
                    ((MZ)v0).b.setVisibility(View.GONE);
                }

                if(mv.c == null) {
                    new MW().execute(mv);
                }

                ((MZ)v0).c.setImageDrawable(mv.c);
            }
        }

        private void a(List<ResolveInfo> arg8, int arg9, int arg10, ResolveInfo arg11, CharSequence arg12) {
            int v0_1;
            if(arg10 - arg9 + 1 == 1) {
                if(this.ff != null && (this.ff.activityInfo.packageName.equals(arg11.activityInfo.packageName))
                        && (this.ff.activityInfo.name.equals(arg11.activityInfo.name))) {
                    this.hh = this.aa.size();
                }

                this.aa.add(new MV(arg11, arg12, null));
            }else{
                d = true;
                int v2 = 0;
                CharSequence v0 = arg11.activityInfo.applicationInfo.loadLabel(b);
                if(v0 == null) {
                    v2 = 1;
                }

                if(v2 == 0) {
                    HashSet v4 = new HashSet();
                    v4.add(v0);
                    for(int v3 = arg9 + 1;v3 <= arg10 ;v3++){
                        v0 = arg8.get(v3).activityInfo.applicationInfo.loadLabel(b);
                        if(v0 != null && !v4.contains(v0)){
                            v4.add(v0);
                        }
                    }

                    v2 = 1;
                    v4.clear();
                }

                for(;arg9 <= arg10;arg9 ++) {
                    ResolveInfo v0_2 = arg8.get(arg9);
                    if(this.ff != null && (
                            this.ff.activityInfo.packageName.equals((v0_2).activityInfo.packageName)) &&
                            (this.ff.activityInfo.name.equals((v0_2).activityInfo.name))) {
                        this.hh = this.aa.size();
                    }

                    if(v2 != 0) {
                        this.aa.add(new MV((v0_2), arg12, (v0_2).activityInfo.packageName));
                    }
                    else {
                        this.aa.add(new MV((v0_2), arg12, (v0_2).activityInfo.applicationInfo.loadLabel(b)));
                    }
                }
            }
        }

        public void a() {
            this.c();
            notifyDataSetChanged();
            if(getCount() == 0) {
                ResolverActivity.this.finish();
            }
        }

        public int b() {
            return this.hh;
        }

        private void c() {
            CharSequence v8_1 = null;
            String v5_2;
            int v2_1;
            Intent v0;
            List v1;
            List v10 = null;
            int v9 = 1;
            this.aa.clear();
            if(this.ee != null) {
                v1 = this.ee;
                this.bb = v10;
            } else {
                v0 = new Intent("android.intent.action.MAIN");
                v0.addCategory("android.intent.category.HOME");
                v1 = b.queryIntentActivities(v0, 0);
            }

            if(v1 != null) {
                int v7 = v1.size();
                if(v7 > 0) {
                    Collections.sort(v1, new ResolveInfo.DisplayNameComparator(getPackageManager()));
                    ResolveInfo v0_1 = (ResolveInfo) v1.get(0);
                    int v4 = 0;
                    while(v4 < v7) {
                        ResolveInfo v2 = (ResolveInfo)v1.get(v4);
                        Log.i("gqf","e="+e+" pack="+v2.activityInfo.applicationInfo.packageName);
                        if((v2).activityInfo.applicationInfo.packageName.equals(ClientInfo.getPackageName())) {
                            n = v4;
                        }

                        if(e != null && ((v2).activityInfo.applicationInfo.packageName.equals(ClientInfo.getPackageName()))) {
                            e.getViewTreeObserver().addOnGlobalLayoutListener(new MY(v4));
                        }

                        if((v0_1).priority != (v2).priority || (v0_1).isDefault != (v2).isDefault) {
                            for(v2_1 = v7; v2_1 > v4; v2_1--) {
                                if(this.bb == v1) {
                                    this.bb = new ArrayList(this.bb);
                                }

                                v1.remove(v4);
                            }
                        } else {
                            v2_1 = v7;
                        }

                        v4++;
                        v7 = v2_1;
                    }

                    if(v7 > 1) {
                        Collections.sort(v1, new ResolveInfo.DisplayNameComparator(b));
                    }

                    if(this.dd != null) {
                        for(v2_1 = 0; v2_1 < this.dd.length; v2_1++) {
                            v0 = this.dd[v2_1];
                            if(v0 != null) {
                                ActivityInfo v4_1 = v0.resolveActivityInfo(getPackageManager(), 0);
                                if(v4_1 != null) {
                                    ResolveInfo v5 = new ResolveInfo();
                                    v5.activityInfo = v4_1;
                                    if((v0 instanceof LabeledIntent)) {
                                        v5.resolvePackageName = ((LabeledIntent)v0).getSourcePackage();
                                        v5.labelRes = ((LabeledIntent)v0).getLabelResource();
                                        v5.nonLocalizedLabel = ((LabeledIntent)v0).getNonLocalizedLabel();
                                        v5.icon = ((LabeledIntent)v0).getIconResource();
                                    }

                                    this.aa.add(new MV(v5, v5.loadLabel(getPackageManager()), ((CharSequence)v10)));
                                }
                            }
                        }
                    }

                    v0_1 = (ResolveInfo)v1.get(0);
                    CharSequence v5_1 = (v0_1).loadLabel(b);
                    d = false;
                    v2_1 = 0;
                    ResolveInfo v4_2 = v0_1;
                    while(v9 < v7) {
                        if(v5_1 == null) {
                            v5_1 = (v4_2).activityInfo.packageName;
                        }

                        ResolveInfo v6 = (ResolveInfo) v1.get(v9);
                        CharSequence v0_2 = (v6).loadLabel(b);
                        CharSequence v8;
                        if(v0_2 == null) {
                            v8 = (v6).activityInfo.packageName;
                        } else {
                            v8 = v0_2;
                        }

                        if(!v8.equals(v5_1)) {
                            this.a(v1, v2_1, v9 - 1, (v4_2), v5_1);
                            v5_1 = ((String)v8);
                            v2_1 = v9;
                            v4_2 = v6;
                        }

                        v9++;
                    }

                    this.a(v1, v2_1, v7 - 1, (v4_2), v5_1);
                }
            }
        }

        @Override
        public int getCount() {
            return aa.size();
        }

        @Override
        public Object getItem(int i) {
            return aa.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            int v2_1;
            ViewGroup.LayoutParams v1_2;
            MZ v1_1;
            View v0_1 = null;
            if (view == null) {
                try {
                    if (ajz.o()) {
                        v0_1 = this.gg.inflate(l.getLayout(l.getIdentifier("simple_list_item_single_choice", "layout", "android")), null);
                        Na v1 = new Na(v0_1);
                        v0_1.setTag(v1);
                        ViewGroup.LayoutParams v2 = v1.a.getLayoutParams();
                        if (v2 == null) {
                            v1.a.setLayoutParams(new AbsListView.LayoutParams(-1, i));
                        } else {
                            v2.height = i;
                            v2.width = -1;
                        }

                        v1.a.setLayoutParams(v2);
                    } else {
                        v0_1 = this.gg.inflate(l.getLayout(l.getIdentifier("resolve_grid_item", "layout", "android")), null);
                        v1_1 = new MZ(v0_1);
                        v0_1.setTag(v1_1);
                        v1_2 = v1_1.c.getLayoutParams();
                        v2_1 = i;
                        v1_2.height = v2_1;
                        v1_2.width = v2_1;
                    }
                } catch (Throwable e) {
                    try {
                        v0_1 = this.gg.inflate(l.getIdentifier("resolve_list_item", "layout", "android"), viewGroup, false);
                        v1_1 = new MZ(v0_1);
                        v0_1.setTag(v1_1);
                        if(m) {
                            v0_1.setPadding(0, 0, 0, 0);
                        }

                        v1_2 = v1_1.c.getLayoutParams();
                        v2_1 = i;
                        v1_2.height = v2_1;
                        v1_2.width = v2_1;
                        if((ajz.as()) || (ajz.ar()) || ajz.ao()) {
                            v1_2.height += aiv.a(ResolverActivity.this,16f);
                        }
                    }
                    catch(Throwable ee) {
                        ee.printStackTrace();
                        finish();
                        view = new View(ResolverActivity.this);
                    }

                    view = v0_1;
                }

                view = v0_1;
            }

            MV mv = aa.get(position);
            a(view, mv);
            return view;
        }

        public class MY implements ViewTreeObserver.OnGlobalLayoutListener{

            private int a;

            public MY(int a){
                this.a = a;
            }

            @Override
            public void onGlobalLayout() {
                Log.i("gqf","MY onGlobalLayout");
                e.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View v = e.getChildAt(a - e.getFirstVisiblePosition());
                if(ajz.T()){
                    onLocationLocated(ResolverActivity.this,v,f);
                }
            }
        }
    }
}
