package com.nqmobile.livesdk.modules.defaultlauncher;

import android.os.Build;
import android.os.SystemProperties;

/**
 * Created by Rainbow on 2015/1/23.
 */
public class ajz {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private static final String a;
    private static final String b;
    private static final String c;
    private static final String d;

    static{
        a = Build.PRODUCT.toLowerCase();
        b = Build.MODEL.toLowerCase();
        c = Build.MANUFACTURER.toLowerCase();
        d = Build.DISPLAY.toLowerCase();
    }

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

    public static boolean A() {
        return ajz.b.startsWith("me526");
    }

    public static boolean B() {
        return ajz.b.startsWith("me860");
    }

    public static boolean C() {
        return ajz.b.startsWith("me865");
    }

    public static boolean D() {
        return ajz.b.startsWith("xt882");
    }

    public static boolean E() {
        return ajz.c.equalsIgnoreCase("yulong");
    }

    public static boolean F() {
        return ajz.b.contains("kindle fire");
    }

    public static boolean G() {
        return ajz.b.startsWith("lg-p970");
    }

    public static boolean H() {
        return ajz.b.startsWith("u8800");
    }

    public static boolean I() {
        return ajz.b.startsWith("u9200");
    }

    public static boolean J() {
        return ajz.a.equalsIgnoreCase("c8650e") && ajz.c.equalsIgnoreCase("huawei");
    }

    public static boolean K() {
        return ajz.b.startsWith("lt29i");
    }

    public static boolean L() {
        return ajz.b.equalsIgnoreCase("deovo v5");
    }

    public static boolean M() {
        return ajz.b.equalsIgnoreCase("Lenovo A390t");
    }

    public static boolean N() {
        return ajz.b.equalsIgnoreCase("lenovo k860i");
    }

    public static boolean O() {
        return ajz.b.contains("lenovo a850");
    }

    public static boolean P() {
        return ajz.b.equalsIgnoreCase("lenovo p770");
    }

    public static boolean Q() {
        return ajz.b.equalsIgnoreCase("z8050") && ajz.c.equalsIgnoreCase("cellon");
    }

    public static boolean R() {
        return ajz.b.equalsIgnoreCase("vivo x3t");
    }

    public static boolean S() {
        return b.equalsIgnoreCase("a001") && c.equalsIgnoreCase("oneplus");
    }

    public static boolean T() {
        return mh.a >= 16;
    }

    public static boolean U() {
        return mh.a >= 14;
    }

    public static boolean V() {
        return Build.VERSION.SDK_INT < 11;
    }

    public static boolean W() {
        return mh.a == 8;
    }

    public static boolean X() {
        return mh.a > 8;
    }

    public static boolean Y() {
        return mh.a == 7;
    }

    public static boolean Z() {
        return mh.a > 7;
    }

    public static boolean a() {
        return ajz.c.equals("samsung") && ajz.b.equals("sch-w2013");
    }

    public static boolean aA() {
        return mh.a >= 18;
    }

    public static boolean aB() {
        return mh.a >= 19;
    }

    public static boolean aC() {
        return b.equalsIgnoreCase("amoi n826") || b.equalsIgnoreCase("amoi n821") || b.equalsIgnoreCase("amoi n820");
    }

    public static boolean aD() {
        return ajz.c.equals("yulong") && ajz.a.contains("5950");
    }

    public static boolean aE() {
        return ajz.c.equals("coolpad") && ajz.a.contains("7298a");
    }

    public static boolean aF() {
        return ajz.c.equals("coolpad") && ajz.a.contains("8297");
    }

    public static boolean aG() {
        return ajz.c.equals("yulong") && ajz.a.contains("8675");
    }

    public static boolean aH() {
        return ajz.c.contains("samsung") && ajz.b.equalsIgnoreCase("Galaxy Nexus");
    }

    public static String aI() {
        return "PRODUCT: " + ajz.a + " MODEL: " + ajz.b + " MANUFACTURER: " + ajz.c + " DISPLAY: " +
                ajz.d;
    }

    public static final boolean aa() {
        return Build.VERSION.SDK_INT >= 16;
    }

    public static boolean ab() {
        return mh.a < 12;
    }

    public static boolean ac() {
        return !ajz.F();
    }

    public static boolean ad() {
        return ajz.c.equals("samsung") && ajz.a.equals("sch-i909");
    }

    public static boolean ae() {
        return ajz.c.equals("samsung") && ajz.b.equals("gt-i9300");
    }

    public static boolean af() {
        return ajz.c.equals("samsung") && ajz.b.indexOf("sm-g950") > 0;
    }

    public static boolean ag() {
        return ajz.c.equals("samsung") && ajz.b.indexOf("sm-g900") > 0;
    }

    public static boolean ah() {
        return ajz.c.equals("samsung") && ajz.b.indexOf("gt-n7000") > 0;
    }

    public static boolean ai() {
        return ajz.c.equals("samsung") && ajz.b.indexOf("sm-n900") > 0;
    }

    public static boolean aj() {
        return ajz.d.contains("miui") && ajz.d.indexOf("mione") > 0;
    }

    public static boolean ak() {
        String v0_1;
        try {
            v0_1 = SystemProperties.get("ro.miui.ui.version.name");
        }
        catch(Exception v0) {
            v0_1 = "";
        }

        return v0_1.equalsIgnoreCase("V5") || (v0_1.equalsIgnoreCase("V6"));
    }

    public static boolean al() {
        return ajz.aI().contains("coolpad") || ajz.aI().contains("yulong") || ajz.a.matches("(?:coolpad|yulong)?5[0-9]+");
    }

    public static boolean am() {
        return ajz.b.contains("deovo v5");
    }

    public static boolean an() {
        return ajz.c.equals("bovo") && ajz.b.equals("s-f16");
    }

    public static boolean ao() {
        return ajz.c.equals("oppo") && b.contains("x909");
    }

    public static boolean ap() {
        return ajz.c.equals("oppo") && b.contains("x9007");
    }

    public static boolean aq() {
        return ajz.c.equals("oppo") && ajz.b.contains("r8007");
    }

    public static boolean ar() {
        return ajz.c.equalsIgnoreCase("huawei") && ajz.b.contains("p6-u06");
    }

    public static boolean as() {
        return ajz.c.equalsIgnoreCase("huawei") && ajz.b.contains("g700-t00");
    }

    public static boolean at() {
        return ajz.c.equalsIgnoreCase("huawei") && ajz.b.contains("c8500");
    }

    public static boolean au() {
        return ajz.c.equalsIgnoreCase("huawei") && ajz.b.contains("u9508");
    }

    public static boolean av() {
        return c.equalsIgnoreCase("huawei") || b.contains("h30-t10") || b.contains("h30-t00");
    }

    public static boolean aw() {
        return ajz.c.equalsIgnoreCase("huawei") && ajz.b.contains("c8813q");
    }

    public static boolean ax() {
        return ajz.c.equalsIgnoreCase("huawei") && ajz.b.contains("c8650");
    }

    public static boolean ay() {
        return ajz.b.contains("nexus 5");
    }

    public static boolean az() {
        return mh.a >= 17;
    }

    public static boolean b() {
        return ajz.a.contains("meizu_m9") && ajz.b.contains("m9");
    }

    public static boolean c() {
        return ajz.a.contains("meizu_mx");
    }

    public static boolean d() {
        return ajz.a.contains("meizu_mx2");
    }

    public static boolean e() {
        return ajz.a.contains("meizu_mx3");
    }

    public static boolean f() {
        return ajz.a.contains("meizu_mx4");
    }

    public static boolean g() {
        return b.contains("htc") || b.contains("desire");
    }

    public static boolean h() {
        return ajz.c.equals("zte") && ajz.b.contains("blade");
    }

    public static boolean i() {
        return ajz.c.equals("zte") && ajz.b.contains("zte-u v880");
    }

    public static boolean j() {
        return ajz.c.equals("zte") && ajz.b.contains("zte u985");
    }

    public static boolean k() {
        return ajz.c.equals("htc") && ajz.b.contains("hd2");
    }

    public static boolean l() {
        return ajz.c.equals("htc") && ajz.b.contains("htc one x");
    }

    public static boolean m() {
        return ajz.c.equals("htc") && !ajz.b.contains("htc 802w");
    }

    public static boolean n() {
        return ajz.c.equals("htc") && ajz.b.contains("velocity 4g x710s");
    }

    public static boolean o() {
        return ajz.c.equals("htc") && ajz.b.contains("htc 609d");
    }

    public static boolean p() {
        return c.equals("samsung") && b.equals("gt-i9100");
    }

    public static boolean q() {
        return ajz.b.startsWith("mi-one");
    }

    public static boolean r() {
        return ajz.b.startsWith("mi 2");
    }

    public static boolean s() {
        return ajz.b.startsWith("mi 3");
    }

    public static boolean t() {
        return c.equals("xiaomi") && Build.DEVICE.startsWith("HM");
    }

    public static boolean u() {
        return ajz.b.equalsIgnoreCase("gt-s5830");
    }

    public static boolean v() {
        return ajz.b.equalsIgnoreCase("gt-s5830i");
    }

    public static boolean w() {
        return ajz.b.equalsIgnoreCase("gt-p1000");
    }

    public static boolean x() {
        return ajz.b.startsWith("mb525");
    }

    public static boolean y() {
        return ajz.b.startsWith("me525");
    }

    public static boolean z() {
        return ajz.b.startsWith("mb526");
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
