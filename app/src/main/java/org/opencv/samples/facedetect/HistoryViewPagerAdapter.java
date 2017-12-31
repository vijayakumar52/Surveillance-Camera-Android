package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.adsonik.surveillancecamera.R;
import com.jsibbold.zoomage.ZoomageView;
import com.vijay.androidutils.BitmapCache;
import com.vijay.androidutils.BitmapUtils;
import com.vijay.androidutils.DisplayUtils;

import java.io.File;
import java.util.List;

import static org.opencv.samples.facedetect.HistoryRecyclerViewAdapter.getHistoryPath;

/**
 * Created by vijay-3593 on 31/12/17.
 */

public class HistoryViewPagerAdapter extends PagerAdapter{
    Context context;
    List<History> historyList;
    private BitmapCache bitmapCache;


    HistoryViewPagerAdapter(Context context, List<History> adapterData) {
        this.context = context;
        this.historyList = adapterData;
        this.bitmapCache = new BitmapCache();
    }

    @Override
    public int getCount() {
        return historyList.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        relativeLayoutParams.addRule(Gravity.CENTER);
        relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.md_grey_600));
        relativeLayout.getBackground().setAlpha(150);
        ZoomageView imageView = new ZoomageView(context);

        RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int padding = (int) DisplayUtils.convertDpToPixel(16, context);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageBitmap(getImage(position));
        imageView.setLayoutParams(imageViewParams);

        imageView.setAutoCenter(true);
        imageView.setTranslatable(true);
        imageView.setZoomable(true);

        relativeLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(imageView);
        ((ViewPager) container).addView(relativeLayout, 0);
        return relativeLayout;
    }


    private Bitmap getImage(int position) {
        History item = historyList.get(position);
        long id = item.getCreatedTime();

        int width = DisplayUtils.getScreenWidthinPx((Activity) context);
        Bitmap bitmap = bitmapCache.getBitmap(id + "");
        if (bitmap == null) {
            File imageFile = new File(getHistoryPath(context), id + ".jpeg");
            if (imageFile.isFile()) {
                //bitmap = BitmapUtils.getDownScaledImage(imageFile.getPath(), 300, 300);
                bitmap = BitmapUtils.getBitmap(imageFile.getPath());
                bitmapCache.putBitmap(id + "", bitmap);
            }
        }
        return bitmap;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }
}