/**
 * 
 */
package com.nqmobile.livesdk.modules.newyear;

import java.util.Arrays;

import org.apache.thrift.TApplicationException;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TRewardPointsResp;
import com.nq.interfaces.userinfo.TPointsChangeInfo;
import com.nqmobile.live.R;
import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.theme.ThemeConstants;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 将Activity切换到积分结果界面的工具类
 * 
 * @author nq
 *
 */
public class GetPointViewUtil {
	private static final ILogger NqLog = LoggerFactory.getLogger("newyear");

	public static void setPointResultView(Activity chouJiangActivity) {
	    PriorityExecutor.getExecutor().submit(new GetLotteryPointProtocol(chouJiangActivity));
	}
	private static class GetLotteryPointProtocol extends AbsLauncherProtocol implements View.OnClickListener{
       private final Activity chouJiangActivity;
       
		public GetLotteryPointProtocol(Activity chouJiangActivity) {
			this.chouJiangActivity = chouJiangActivity;
		}

		@Override
		protected int getProtocolId() {
			return 0xee77;
		}
		
		@Override
		protected void process() {
			try {
				TLauncherService.Iface client = TLauncherServiceClientFactory
						.getClient(getThriftProtocol());
				TPointsChangeInfo param = new TPointsChangeInfo();
				param.clientTime = SystemFacadeFactory.getSystem().currentTimeMillis();
				param.resourceId="RPspring2015";
				param.scene="2015";
				TRewardPointsResp resp = client.rewardPointsNew(getUserInfo(), Arrays.asList(param));
				int point = 0;
				if (resp.pointsInfo != null) {
					point = resp.pointsInfo.expirePoints;
				}
				final int POINT = point;
				changeView(POINT);
	        } catch (TApplicationException e) {//服务器端无数据
	            NqLog.d("fail to rewardPointsNew");
	            onError();
	        } catch (Throwable e) {
	        	NqLog.e(e);
	        	onError();
	        }
		}

		@Override
		protected void onError() {
			chouJiangActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ToastUtils.toast(ApplicationContext.getContext(), "nq_spring_choujiang_failed");		
				}
			});
			changeView(0);
		}
		
		private void changeView(final int point){
			chouJiangActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setPoint(chouJiangActivity,GetLotteryPointProtocol.this, point);
				}
			});
		}
        
		@Override
		public void onClick(View v) {
			NqLog.d("点击了兑换积分按钮");
			LauncherSDK.getInstance(chouJiangActivity).gotoStore(ThemeConstants.STORE_FRAGMENT_INDEX_THEME);
		}
		
	}
	private static void setPoint(Activity chouJiangActivity,View.OnClickListener btnOnclickListener,int point){
		View leave = chouJiangActivity.findViewById(R.id.leave);
		View textlayout = chouJiangActivity.findViewById(R.id.text_layout);
		View item_layout = chouJiangActivity.findViewById(R.id.item_layout);
		View shareBtn = chouJiangActivity.findViewById(R.id.share);
		leave.setVisibility(View.GONE);
		textlayout.setVisibility(View.GONE);
		item_layout.setVisibility(View.GONE);
		shareBtn.setBackgroundResource(R.drawable.nq_spring_exchange_button);
		shareBtn.setOnClickListener(btnOnclickListener);
		ImageView title = (ImageView) chouJiangActivity
				.findViewById(R.id.title);

		DisplayMetrics dm = chouJiangActivity.getResources()
				.getDisplayMetrics();
		{
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) title
					.getLayoutParams();
			layoutParams.topMargin =(int) (70*dm.density);
			title.setLayoutParams(layoutParams);
			title.setImageResource(R.drawable.nq_spring_con_title);
		}
		RelativeLayout lyt = (RelativeLayout) title.getParent();
		{
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layoutParams.topMargin = (int) (110 * dm.density);
			ImageView bgview = new ImageView(chouJiangActivity);
			bgview.setImageResource(R.drawable.nq_spring_wincredit);
			ScrollView sc = (ScrollView) lyt.getParent();
			sc.scrollTo(sc.getScrollX(), sc.getTop());
			lyt.addView(bgview,1, layoutParams);
		}
//		  <TextView
//          android:id="@+id/tmp_base"
//          android:layout_width="0dp"
//          android:layout_height="0dp"
//          android:layout_centerHorizontal="true"
//          android:layout_marginTop="225dp" />
		{
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					0, 0);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layoutParams.topMargin = (int) (225 * dm.density);
			TextView tmp = new TextView(chouJiangActivity);
			tmp.setId(R.id.tmp_base);
			lyt.addView(tmp, layoutParams);
		}

//        <TextView
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:layout_alignBottom="@+id/tmp_base"
//            android:layout_toLeftOf="@+id/tmp_base"
//            android:layout_toStartOf="@+id/tmp_base"
//            android:text="100"
//            android:textColor="@android:color/white"
//            android:textSize="38sp"
//            android:textStyle="bold" />
		{
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.tmp_base);
			layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.tmp_base);
			TextView textPoint = new TextView(chouJiangActivity);
			textPoint.setTextColor(chouJiangActivity.getResources().getColor(android.R.color.white));
			textPoint.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 38);
			textPoint.setTypeface(null, Typeface.BOLD);
			// textPoint
			textPoint.setText(""+point);
			lyt.addView(textPoint, layoutParams);
		}
	}
}
