package com.bilibili.bbq.feedlib;

import android.databinding.BindingAdapter;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v7.content.res.AppCompatResources;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bilibili.lib.image.ImageLoader;

public class BindingAdapterUtil {

    @BindingAdapter("invisibleUnless")
    public static void invisibleUnless(View view, boolean visible){
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter("goneUnless")
    public static void goneUnless(View view, boolean visible){
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("spannableString")
    public static void spannableString(TextView textView, SpannableString string){
        if (!TextUtils.isEmpty(string)) {
            textView.setText(string);
        }
    }

    @BindingAdapter("clipToCircle")
    public static void clipToCircle(View view, boolean clip) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setClipToOutline(clip);
            view.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(
                            view.getPaddingLeft(),
                            view.getPaddingTop(),
                            view.getWidth() - view.getPaddingRight(),
                            view.getHeight() - view.getPaddingBottom()
                    );
                }
            });
        }
    }

    @BindingAdapter(value = {"imageUri", "placeholderImage"}, requireAll = false)
    public static void imageUri(ImageView imageView, String imageUri, @DrawableRes int placeholder) {
//        ImageLoader.getInstance().displayImageWithAnimations(imageUri, imageView, placeholder);
    }

}
