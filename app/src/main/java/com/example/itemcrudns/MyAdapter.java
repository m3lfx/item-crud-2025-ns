package com.example.itemcrudns;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
    private Context mContext;
    private List<Item> list_data;


    private static final String urlString ="http://172.34.98.64:8000/storage/" ;
    public MyAdapter(Context mContext, List<Item> list_data) {
        this.list_data = list_data;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_data,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        final Item listData = this.list_data.get(position);
        Log.i("image",urlString + listData.getImage_url());
        if (listData.getImage_url().isEmpty()) {
            holder.imageView.setImageResource(R.drawable.ic_launcher_background);
        } else{
            Picasso.get().load(urlString + listData.getImage_url()).into(holder.imageView);
        }
        holder.txtdescription.setText(listData.getDescription());
        holder.txtsell_price.setText(listData.getSell_price());

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(mContext, DetailViewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("name",listData.getDescription());
                intent.putExtra("imageurl",urlString + listData.getImage_url());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list_data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtdescription, txtsell_price;
        private ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            txtdescription =  itemView.findViewById(R.id.txt_description);
            txtsell_price =   itemView.findViewById(R.id.txt_sell_price);
            imageView =  itemView.findViewById(R.id.imageView);
        }
    }
}
