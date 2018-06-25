package com.ega.rssfeedreader.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ega.rssfeedreader.R;
import com.ega.rssfeedreader.model.RssItem;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder>{
    private Activity myContext;

    private ArrayList<RssItem> listRssItem;
    private int rowLayout;

    public ContentAdapter(Context context, int rowLayout, ArrayList<RssItem> list){
        this.myContext = (Activity) context;
        this.listRssItem = list;
        this.rowLayout = rowLayout;
    }

    public void clearData(){
        if(listRssItem != null){
            listRssItem.clear();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if(listRssItem == null) return;

        RssItem currentArticle = listRssItem.get(position);

        Locale.setDefault(Locale.getDefault());

        Date date = currentArticle.getPubDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");

        final String publishedDate = dateFormat.format(date);

        holder.labelTitle.setText(currentArticle.getTitle());
        holder.labelPubDate.setText(publishedDate);

        if(currentArticle.getImage() == null){
            holder.imageThumbnail.setImageResource(R.drawable.default_thumbnail);
        }
        else{
            Picasso.get()
                    .load(currentArticle.getImage())
                    .placeholder(R.drawable.default_thumbnail)
                    .into(holder.imageThumbnail);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(listRssItem.get(position).getLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                myContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listRssItem == null ? 0 : listRssItem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView labelTitle;
        TextView labelPubDate;
        ImageView imageThumbnail;

        public ViewHolder(View itemView){
            super(itemView);

            labelTitle = itemView.findViewById(R.id.label_title);
            labelPubDate = itemView.findViewById(R.id.label_published_date);
            imageThumbnail = itemView.findViewById(R.id.image_thumbnail);
        }
    }
}
