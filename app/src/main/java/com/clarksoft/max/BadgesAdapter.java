package com.clarksoft.max;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BadgesAdapter extends RecyclerView.Adapter<BadgesAdapter.MyViewHolder> {

    private ArrayList<Uri> BadgeUri;
    private ArrayList<String> BadgeTitle;
    private ArrayList<String> BadgeDescription;
    private ArrayList<Boolean> BadgeEnabled;
    private Context context;

    public BadgesAdapter(ArrayList<Uri> BadgeUri, ArrayList<String> BadgeTitle, ArrayList<String> BadgeDescription, ArrayList<Boolean> BadgeEnabled, Context context) {

        this.BadgeUri = BadgeUri;
        this.BadgeTitle = BadgeTitle;
        this.BadgeDescription = BadgeDescription;
        this.BadgeEnabled = BadgeEnabled;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.badges_view, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.BadgeImageView.setImageURI(BadgeUri.get(position));
        holder.BadgeTitleView.setText(BadgeTitle.get(position));
        holder.BadgeDescriptionView.setText(BadgeDescription.get(position));

        if(BadgeEnabled.get(position)){
            holder.BadgeTitleView.setTextColor(context.getColor(R.color.colorPrimary));
            holder.BadgeDescriptionView.setTextColor(context.getColor(R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return BadgeUri.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView BadgeImageView;
        TextView BadgeTitleView;
        TextView BadgeDescriptionView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            BadgeImageView = itemView.findViewById(R.id.badge_view);
            BadgeTitleView = itemView.findViewById(R.id.badge_title);
            BadgeDescriptionView = itemView.findViewById(R.id.badge_description);
        }
    }
}
