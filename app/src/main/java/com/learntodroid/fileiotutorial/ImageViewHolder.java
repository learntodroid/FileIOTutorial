package com.learntodroid.fileiotutorial;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

public class ImageViewHolder extends RecyclerView.ViewHolder {
    private TextView title;
    private ImageView imageView;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.item_image_imageView);
        title = itemView.findViewById(R.id.item_image_title);
    }

    public void bind(Image image) {
        Glide.with(itemView).load(image.uri).into(imageView);
        title.setText(image.name);
    }
}
