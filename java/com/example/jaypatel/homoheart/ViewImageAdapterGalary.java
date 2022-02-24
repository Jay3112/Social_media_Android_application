package com.example.jaypatel.homoheart;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewImageAdapterGalary extends PagerAdapter {

    private Context context;
    private ArrayList<String> imageUris;

    ViewImageAdapterGalary(Context context, ArrayList<String> imageUris){
        this.context = context;
        this.imageUris = imageUris;
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final PhotoView imageView = new PhotoView(context);
        imageView.setBackgroundResource(R.drawable.custom_dialog_shape);
        imageView.setPadding(6,6,6,6);
        Picasso.get().load(imageUris.get(position)).into(imageView);
        container.setClipChildren(false);
        container.addView(imageView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                PopupMenu pm = new PopupMenu(context,imageView);
                pm.getMenuInflater().inflate(R.menu.image_crop_del,pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.crop:
                                Toast.makeText(context, "crop"+position, Toast.LENGTH_SHORT).show();
                                break;

                            case R.id.remove:
                                imageUris.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "remove"+position, Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });
                pm.show();
                return true;
            }
        });
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
