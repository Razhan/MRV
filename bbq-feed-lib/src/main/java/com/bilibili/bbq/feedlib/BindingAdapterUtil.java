package com.bilibili.bbq.feedlib;

import android.databinding.BindingAdapter;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

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

    @BindingAdapter(value = {"imageUri", "placeholder"}, requireAll = false)
    public static void imageUri(ImageView imageView, Uri imageUri, Drawable placeholder) {
        if (placeholder == null) {
            placeholder = AppCompatResources.getDrawable(imageView.getContext(), R.drawable.generic_placeholder);
        }

        //加载图片
//        if (imageUri == null) {
//            Glide.with(imageView)
//                    .load(placeholder)
//                    .into(imageView);
//        } else {
//            Glide.with(imageView)
//                    .load(imageUri)
//                    .apply(RequestOptions().placeholder(placeholderDrawable))
//                    .into(imageView);
//        }
    }

    @BindingAdapter(value = {"imageUri", "placeholder"}, requireAll = false)
    public static void imageUri(ImageView imageView, String imageUri, Drawable placeholder) {
        Uri uri = null;
        if (!TextUtils.isEmpty(imageUri)) {
            uri = Uri.parse(imageUri);
        }

        imageUri(imageView, uri, placeholder);
    }

}
