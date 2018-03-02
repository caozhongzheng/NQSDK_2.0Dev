package com.nqmobile.livesdk.modules.banner;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.modules.theme.TopicManager;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.Tools;

/**
 * bannerAdapter
 * @author changxiaofei
 * @time 2013-11-20 下午3:34:48
 */
public class BannerAdapter extends PagerAdapter {
	private static final ILogger NqLog = LoggerFactory.getLogger(BannerModule.MODULE_NAME);
	
	private Context context;
	private List<Banner> banners;
	private int plate;
	private int mColumn;
	
	public int getPlate() {
		return plate;
	}

	public void setPlate(int plate, int column) {
		this.plate = plate;
		mColumn = column;
	}

	public BannerAdapter(Context context, List<Banner> bannerInfos) {
		super();
		this.context = context;
		this.banners = bannerInfos;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		NqLog.d("destroyItem: position=" + position);
		((ViewPager) container).removeView((View) object);
	}
	
	@Override
	public int getCount() {
		if (banners != null && banners.size() > 1) {
			return Integer.MAX_VALUE;
		} else {
			return 1;
		}
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	
	@Override
	public Object instantiateItem(View view, int position) {
		NqLog.d("instantiateItem: position=" + position);
		if (banners.size() > 1 && position < 100){//ViewPager设置Adapter时，自动会生成position 0和1的View
			return null;
		}

		final int index = position % banners.size();
		final Banner banner = banners.get(index);
		final View imageLayout = LayoutInflater.from(context).inflate(MResource.getIdByName(context, "layout", "nq_banner_viewpager_item"), null);
		final AsyncImageView imageView = (AsyncImageView) imageLayout.findViewById(MResource.getIdByName(context, "id", "image"));
		final ProgressBar pbLoading = (ProgressBar) imageLayout.findViewById(MResource.getIdByName(context, "id", "loading"));
		String imgUrl = banner.getStrImageUrl();
		NqLog.d("instantiateItem: banner.imgUrl=" + imgUrl);
		final int plat = banner.getNewPlate();
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NqLog.d("onClick: plat:" + plat + ", column:" + mColumn + ", " + banner);
				if(plat == 0 || plat == 4) {
					AppManager.getInstance(context).viewAppDetail(mColumn, banner.getApp());
				}else if (plat == 1) {
					ThemeManager.getInstance(context).goThemeDetail(banner.getTheme());
				}else if (plat == 2) {
					WallpaperManager.getInstance(context).goWallpaperDetail(banner.getWallpaper());
				}else if (plat == 3) {
					TopicManager.getInstance(context).goTopicDetail(banner.getTopic());
				}
                StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT, BannerActionConstants.ACTION_LOG_1402,
						banner.getStrId(), StatManager.ACTION_CLICK, getPlate() + "_" + index);
				
				if(banner.getIntClickActionType() == App.CLICK_ACTION_TYPE_DIRECT)
                    StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                    		AppActionConstants.ACTION_LOG_1106, banner.getStrId(), StatManager.ACTION_CLICK, null);
			}
		});
		if(Tools.isEmpty(imgUrl)){
			if(plat == 0 || plat == 4) {
				imgUrl = banner.getApp().getStrIconUrl();
				if(Tools.isEmpty(imgUrl)){
					imgUrl = banner.getApp().getStrImageUrl();
				}
			}else if (plat == 1) {
				imgUrl = banner.getTheme().getStrIconUrl();
			}else if (plat == 2) {
				imgUrl = banner.getWallpaper().getStrIconUrl();
			}else if (plat == 3) {
				imgUrl = banner.getTopic().getPicture();
			}
		}
		NqLog.d("instantiateItem: banner.imgUrl:" + imgUrl);
		imageView.loadImage(imgUrl, pbLoading,
				MResource.getIdByName(context, "drawable", "nq_banner_empty"));

		((ViewPager) view).addView(imageLayout, 0);
		return imageLayout;
	}
}