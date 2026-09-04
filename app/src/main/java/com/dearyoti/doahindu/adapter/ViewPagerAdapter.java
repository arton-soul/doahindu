package com.dearyoti.doahindu.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.StoriesActivity;
import com.dearyoti.doahindu.model.LatestStoryModel;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private View imageLayout;
    private LayoutInflater inflater;
    private Context context;
    private ArrayList<LatestStoryModel> latestList;

    public ViewPagerAdapter(Context context, ArrayList<LatestStoryModel> latestList) {
        this.context = context;
        this.latestList = latestList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return latestList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        imageLayout = inflater.inflate(R.layout.view_pager_items, view, false);

        assert imageLayout != null;
        final ImageView imageView = imageLayout
                .findViewById(R.id.img_view_item);
        TextView textView = imageLayout.findViewById(R.id.txt_banner_topic_desc);

        LatestStoryModel topicsModel = latestList.get(position);
        if (latestList.get(position).getTopic_image() != null) {
            byte[] res = topicsModel.getTopic_image();
            Bitmap bitmap = BitmapFactory.decodeByteArray(res, 0, res.length);
            imageView.setImageBitmap(bitmap);
        }
        textView.setText(topicsModel.getTopic_name());
        view.addView(imageLayout, 0);

        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, StoriesActivity.class);
                intent.putExtra("topic_id", topicsModel.getTopic_id());
                intent.putExtra("topic_name", topicsModel.getTopic_name());
                intent.putExtra("topic_story", topicsModel.getTopic_story());
                intent.putExtra("topic_image", topicsModel.getTopic_image());
                intent.putExtra("flag", "from_latest");
                context.startActivity(intent);
            }
        });
        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
