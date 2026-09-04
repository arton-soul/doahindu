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
import androidx.recyclerview.widget.RecyclerView;

import com.dearyoti.doahindu.R;
import com.dearyoti.doahindu.activity.TopicActivity;
import com.dearyoti.doahindu.model.CategoryModel;

import java.util.ArrayList;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

    private ArrayList<CategoryModel> categoryList;
    private Context context;
    
    public HomeCategoryAdapter(Context context, ArrayList<CategoryModel> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public void updateList(ArrayList<CategoryModel> list) {
        categoryList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel categoryModel = categoryList.get(position);
        byte[] res = categoryModel.getCat_image();
        Bitmap bitmap = BitmapFactory.decodeByteArray(res, 0, res.length);
        holder.imgCategoryImage.setImageBitmap(bitmap);
        holder.imgCategoryImage.invalidate();

        holder.txtCategoryName.setText("" + categoryModel.getCat_name());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TopicActivity.class);
                intent.putExtra("cat_id", categoryModel.getCat_id());
                intent.putExtra("cat_name", categoryModel.getCat_name());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView imgCategoryImage;
        private TextView txtCategoryName;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardView);
            imgCategoryImage = view.findViewById(R.id.img_category);
            txtCategoryName = view.findViewById(R.id.txt_category_name);
        }
    }
}
