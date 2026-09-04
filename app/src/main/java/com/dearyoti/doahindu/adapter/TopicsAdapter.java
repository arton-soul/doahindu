package com.dearyoti.doahindu.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.StoriesActivity;
import com.dearyoti.doahindu.database.DatabaseHelper;
import com.dearyoti.doahindu.model.TopicsModel;
import com.dearyoti.doahindu.utils.MySharedPref;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

    private Context context;
    private View view;
    private ArrayList<TopicsModel> topicsList;
    private MySharedPref mySharedPref;
    private DatabaseHelper db;
    private itemInterface itemInter;
    private Integer selectedTopicId;
    private Boolean isFavorite;

    public TopicsAdapter(Context context, ArrayList<TopicsModel> topicsList, Boolean isFavorite, itemInterface itemInterface) {
        this.context = context;
        this.topicsList = topicsList;
        this.isFavorite = isFavorite;
        this.itemInter = itemInterface;
    }

    public void updateList(ArrayList<TopicsModel> list) {
        topicsList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_list_item, parent, false);
        db = new DatabaseHelper(context);
        mySharedPref = new MySharedPref();
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TopicsAdapter.ViewHolder holder, int position) {

        TopicsModel topicsModel = topicsList.get(position);

        if (topicsList.get(position).getTopic_image() != null) {
            byte[] res = topicsModel.getTopic_image();
            Bitmap bitmap = BitmapFactory.decodeByteArray(res, 0, res.length);
            holder.imgTopicImage.setImageBitmap(bitmap);
        }
        holder.txtTopicName.setText("" + topicsModel.getTopic_name());

        selectedTopicId = topicsModel.getTopic_id();
        isFavorite(holder);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, StoriesActivity.class);
                intent.putExtra("topic_id", topicsModel.getTopic_id());
                intent.putExtra("cat_id", topicsModel.getCat_id());
                intent.putExtra("topic_name", topicsModel.getTopic_name());
                intent.putExtra("topic_story", topicsModel.getTopic_story());
                intent.putExtra("flag", "from_topic");
                intent.putExtra("topic_image", topicsModel.getTopic_image());
                context.startActivity(intent);
            }
        });

        holder.imgTopicBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedTopicId = topicsModel.getTopic_id();
                if (db.isFavorite(selectedTopicId)) {
                    if (db.updateFavorite(selectedTopicId, 0)) {
                        Snackbar.make(view, "Story removed from favorite.", Snackbar.LENGTH_LONG).show();
                        if (isFavorite) {
                            itemInter.itemRemove();
                        }
                    }
                } else {
                    if (db.updateFavorite(selectedTopicId, 1)) {
                        Snackbar.make(view, "Story added to favorite.", Snackbar.LENGTH_LONG).show();
                    }
                }
                mySharedPref.setFavoriteTopicId(context, db);
                isFavorite(holder);
            }
        });
    }

    private void isFavorite(ViewHolder holder) {
        if (db.isFavorite(selectedTopicId)) {
            holder.imgTopicBookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.favorite_select));
        } else {
            holder.imgTopicBookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.favorite_unselect));
        }
    }

    @Override
    public int getItemCount() {
        return topicsList.size();
    }

    // Initializing the Views
    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgTopicImage;
        TextView txtTopicName;
        ImageView imgTopicBookmark, imgTopicArrow;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardView);
            imgTopicImage = view.findViewById(R.id.img_topic);
            txtTopicName = view.findViewById(R.id.txt_topic_name);
            imgTopicArrow = view.findViewById(R.id.img_item_arrow);
            imgTopicBookmark = view.findViewById(R.id.img_item_favorite);

            if (isFavorite) {
                imgTopicBookmark.setVisibility(View.VISIBLE);
                imgTopicArrow.setVisibility(View.INVISIBLE);
            } else {
                imgTopicArrow.setVisibility(View.VISIBLE);
                imgTopicBookmark.setVisibility(View.GONE);
            }
        }
    }

    public interface itemInterface {
        void itemRemove();
    }
}
