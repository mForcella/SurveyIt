package edu.newpaltz.surveyit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

public class ImageAdapter extends PagerAdapter {

    Context context;
    private String[] GalImages;

    public ImageAdapter(Context context, String[] images){
        this.context=context;
        GalImages = images;
    }

    @Override
    public int getCount() {
        return GalImages.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        //int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
        //imageView.setPadding(padding, padding, padding, padding);
        imageView.setPadding(15, 15, 15, 15);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        String jpg = GalImages[position];
        // set object image
        if (!jpg.equals("NULL")) {
            String path = Environment.getExternalStorageDirectory().getPath()+"/"+jpg;
            File imgFile = new File(path);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        }
        ((ViewPager) container).addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }
}