package com.bones.firebaselogin;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by lenovo ip on 19/06/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    private Context context;
    private List<ImageModel> list;
    private ImageView imageView;

    public RecyclerAdapter(List<ImageModel> list,Context context){
        this.context = context;
        this.list = list;

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_item);
        }
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(list.get(position).getImage_url() != null){
            Glide.with(context).asBitmap().load(list.get(position).getImage_url()).into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.user);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



}
