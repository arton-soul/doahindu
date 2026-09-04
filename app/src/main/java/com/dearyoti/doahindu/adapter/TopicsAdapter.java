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
import com.dearyoti.doahindu.database.DatabaseExecutor;
import com.dearyoti.doahindu.model.TopicsModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

    private Context context;
    private View view;
    private ArrayList<TopicsModel> topicsList;
    private DatabaseHelper db;
    private itemInterface itemInter;
    private Boolean isFavorite;
    private long favoriteCollectionId = 1L;

    public TopicsAdapter(Context context, ArrayList<TopicsModel> topicsList, Boolean isFavorite, itemInterface itemInterface) {
        this.context = context;
        this.topicsList = topicsList;
        this.isFavorite = isFavorite;
        this.itemInter = itemInterface;
        this.db = new DatabaseHelper(context.getApplicationContext());
    }

    public TopicsAdapter(Context context, ArrayList<TopicsModel> topicsList, long collectionId,
                         itemInterface itemInterface) {
        this(context, topicsList, true, itemInterface);
        favoriteCollectionId = collectionId;
    }

    public void updateList(ArrayList<TopicsModel> list) {
        topicsList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TopicsAdapter.ViewHolder holder, int position) {

        TopicsModel topicsModel = topicsList.get(position);

        holder.imgTopicImage.setImageDrawable(null);
        if (topicsList.get(position).getTopic_image() != null) {
            byte[] res = topicsModel.getTopic_image();
            Bitmap bitmap = BitmapFactory.decodeByteArray(res, 0, res.length);
            holder.imgTopicImage.setImageBitmap(bitmap);
        }
        holder.txtTopicName.setText("" + topicsModel.getTopic_name());

        showFavorite(holder, Boolean.TRUE.equals(topicsModel.getIs_topic_fav()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, StoriesActivity.class);
                intent.putExtra("topic_id", topicsModel.getTopic_id());
                intent.putExtra("flag", "from_topic");
                context.startActivity(intent);
            }
        });

        holder.imgTopicBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int topicId = topicsModel.getTopic_id();
                boolean newFavoriteState = !Boolean.TRUE.equals(topicsModel.getIs_topic_fav());
                DatabaseExecutor.execute(() -> isFavorite
                        ? db.removeTopicFromCollection(favoriteCollectionId, topicId)
                        : db.updateFavorite(topicId, newFavoriteState ? 1 : 0), updated -> {
                    if (!updated) {
                        return;
                    }
                    topicsModel.setIs_topic_fav(isFavorite ? false : newFavoriteState);
                    showFavorite(holder, isFavorite ? false : newFavoriteState);
                    Snackbar.make(view, newFavoriteState
                            ? "Story added to favorite." : "Story removed from favorite.",
                            Snackbar.LENGTH_LONG).show();
                    if (isFavorite) {
                        itemInter.itemRemove();
                    }
                }, error -> Snackbar.make(view, "Unable to update favorite.",
                        Snackbar.LENGTH_LONG).show());
            }
        });
    }

    private void showFavorite(ViewHolder holder, boolean favorite) {
        if (favorite) {
            holder.imgTopicBookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.favorite_select));
            holder.imgTopicBookmark.setContentDescription(
                    context.getString(R.string.action_remove_favorite));
        } else {
            holder.imgTopicBookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.favorite_unselect));
            holder.imgTopicBookmark.setContentDescription(
                    context.getString(R.string.action_add_favorite));
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
