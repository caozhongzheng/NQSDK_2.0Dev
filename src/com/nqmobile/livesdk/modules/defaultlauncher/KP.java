package com.nqmobile.livesdk.modules.defaultlauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.nqmobile.live.R;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.utils.MResource;

/**
 * Created by Rainbow on 2015/1/23.
 */
public class KP {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    private static void a(Context context,ComponentName name,int i,int j){
        try{
            context.getPackageManager().setComponentEnabledSetting(name,i,j);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void a(Context context,String s,boolean b){
        PackageManager pm = context.getPackageManager();
        if(ajz.a()){
            akZ.a(context, R.string.clear_default_launcher_for_coolpad,1);
            nN.c(context,s);
        }else{
            if(Build.VERSION.SDK_INT >= 8){
                c(context,true);
                ComponentName name = new ComponentName(context,"com.lqsoft.launcher.MockHome");
                a(context,name,1,1);
                d(context,b);
                a(context,name,0,1);
            }else{
                pm.clearPackagePreferredActivities(s);
                d(context,b);
            }
        }
    }

    /**
     * 设置默认桌面入口
     * @param context
     * @param b
     */
    public static void a(Context context,boolean b){
        ResolveInfo info = nN.f(context);
        if(info != null){
            String packname = info.activityInfo.applicationInfo.packageName;
            if(packname.equals(ClientInfo.getPackageName())){
                Toast.makeText(context, MResource.getString(context,"string","set_default_launcher_success"),Toast.LENGTH_SHORT).show();
            }else{
                a(context,packname,b);
            }
        }else{
            d(context,b);
        }
    }

    public static boolean a(Context arg2) {
        ResolveInfo v0 = nN.f(arg2);
        if(v0 != null){
            return v0.activityInfo.applicationInfo.packageName.equals(ClientInfo.getPackageName());
        }

        return false;
    }

    public static void b(Context context){
        PackageManager pm = context.getPackageManager();
        pm.clearPackagePreferredActivities(ClientInfo.getPackageName());
    }

    public static void b(Context context,boolean b){
        Intent i = new Intent("android.intent.action.MAIN");
        i.addCategory("android.intent.category.HOME");
        i.putExtra("flag",11);
        if(!(context instanceof Activity)) {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (ajz.ak()) {
            ComponentName name = new ComponentName("android", "com.android.internal.app.ResolverActivity");
            i.setComponent(name);
        }

        if (b) {
            e(context);
        }
        context.startActivity(i);
    }

    public static void c(Context context){

    }

    public static void c(Context context,boolean b){
        ResolveInfo info = nN.f(context);
        if(info != null){
            String packName = info.activityInfo.applicationInfo.packageName;
            if(!packName.equals(ClientInfo.getPackageName())){
                if(b){
                    PackageManager pm = context.getPackageManager();
                    if(Build.VERSION.SDK_INT >= 8){
                        ComponentName name = new ComponentName(context.getPackageName(),"com.lqsoft.launcher.MockHome");
                        pm.setComponentEnabledSetting(name,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
                        Intent i = new Intent("android.intent.action.MAIN");
                        i.addCategory("android.intent.category.HOME");
                        i.addCategory("android.intent.category.DEFAULT");
                        pm.resolveActivity(i,PackageManager.GET_RESOLVED_FILTER);
                        pm.setComponentEnabledSetting(name,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                    }else{
                        pm.clearPackagePreferredActivities(packName);
                    }
                }
            }
        }
    }

    public static boolean d(Context context){
        return true;
    }

    private static void d(Context context,boolean b){
        if(ajz.ak() || ajz.d() || ajz.f()|| ajz.e() || ajz.R() || ajz.aF() || ajz.aq() || ajz.aG()){
            b(context,true);
        }else{
            Intent i = new Intent(context,CustomActionDispatchActivity.class);
            if(b){
                i.putExtra("isOnceMore",false);
            }
            i.setAction("action.select.launcher");
            if(!(context instanceof Activity)){
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(i);
        }
    }

    private static void e(Context context){
        Toast t = new Toast(context);
        PackageManager pm = context.getPackageManager();
        Resources res = null;
        Resources r = null;
        try{
            r = pm.getResourcesForActivity(new ComponentName("android","com.android.internal.app.ResolverActivity"));
        }catch (PackageManager.NameNotFoundException e){
        }

        res = r;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.launcher_select_toast, null, false);
        TextView first = (TextView) view.findViewById(R.id.first_toast_text);
        TextView second = (TextView) view.findViewById(R.id.second_toast_front_text);
        TextView third = (TextView) view.findViewById(R.id.third_toast_text);
        third.setVisibility(View.GONE);
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.launcher_select_first_step));
        sb.append(context.getString(R.string.launcher_select_guid_first_text,
                context.getString(R.string.launcher_select_guid_check_first_step_text)));
        first.setText(sb.toString());

        StringBuilder ss = new StringBuilder();
        ss.append(context.getString(R.string.launcher_select_second_step));
        ss.append(context.getString(R.string.launcher_select_guid_check_second_step_text));
        second.setText(ss.toString());
        t.setView(view);
        t.setGravity(0x31, 0, 0);
        t.setDuration(Toast.LENGTH_LONG);

        String str = null;
        String str2 = null;
        try {
            int id = res.getIdentifier("alwaysUse", "string", "android");
            if (id > 0) {
                str = context.getString(id);
                StringBuilder sss = new StringBuilder();
                sss.append(context.getString(R.string.launcher_select_first_step));
                sss.append(context.getString(R.string.launcher_select_guid_first_text,
                        akT.b(str) ? context.getString(R.string.launcher_select_guid_check_first_step_text) : str));

                first.setText(sb.toString());

                StringBuilder d = new StringBuilder();
                d.append(context.getString(R.string.launcher_select_second_step));
                d.append(context.getString(R.string.launcher_select_guid_check_second_step_text));
                second.setText(d.toString());

                first.setVisibility(View.VISIBLE);
                third.setVisibility(View.GONE);
            }
        } catch (Exception e) {
        }

        try {
            int id2 = res.getIdentifier("activity_resolver_use_always", "string", "android");
            if (id2 > 0) {
                str2 = context.getString(id2);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(context.getString(R.string.launcher_select_first_step));
                sb2.append(context.getString(R.string.launcher_select_guid_select_first_step_text));
                second.setText(sb2.toString());

                StringBuilder sb3 = new StringBuilder();
                sb3.append(context.getString(R.string.launcher_select_second_step));
                sb3.append(context.getString(R.string.launcher_select_guid_second_text,
                        akT.b(str2) ? context.getString(R.string.launcher_select_guid_second_text_for_null): str2));
                third.setText(sb3.toString());
                first.setVisibility(View.GONE);
                third.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){

        }

        if(ajz.b()){
            first.setVisibility(View.GONE);
            third.setVisibility(View.GONE);
            second.setText(context.getString(R.string.launcher_select_guid_second_text_for_one_step));
        }

        if (ajz.d() || ajz.e() || ajz.f()) {
            first.setVisibility(View.GONE);
            third.setVisibility(View.GONE);
            second.setText(context.getString(R.string.launcher_select_guid_second_text_for_one_step));
            t.setGravity(0x38, 0, 0x32);
        }

         if (ajz.R() || ajz.ak()) {
             first.setVisibility(View.VISIBLE);
             third.setVisibility(View.GONE);
             StringBuilder a = new StringBuilder();
             a.append(context.getString(R.string.launcher_select_first_step));
             a.append(context.getString(R.string.launcher_select_guid_first_text,
                     akT.b(str) ? context.getString(R.string.launcher_select_guid_check_first_step_text) : str));
             first.setText(a.toString());

             StringBuilder b = new StringBuilder();
             b.append(context.getString(R.string.launcher_select_second_step));
             b.append(context.getString(R.string.launcher_select_guid_check_second_step_text));
             second.setText(b.toString());
         }

        if(ajz.aq()) {
            StringBuilder c = new StringBuilder();
            c.append(context.getString(R.string.launcher_select_first_step));
            c.append(context.getString(R.string.launcher_select_guid_first_text,
                    context.getString(R.string.launcher_select_guid_check_first_step_text_R8007)));
            first.setText(c.toString());

            StringBuilder d = new StringBuilder();
            d.append(context.getString(R.string.launcher_select_second_step));
            d.append(context.getString(R.string.launcher_select_guid_check_second_step_text));
            second.setText(d.toString());

            first.setVisibility(View.VISIBLE);
            third.setVisibility(View.GONE);
        }

        t.show();
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
