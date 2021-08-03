package com.example.e_commerceproject.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_commerceproject.R;
import com.example.e_commerceproject.itemCard;
import com.example.e_commerceproject.itemClick;
import com.example.e_commerceproject.model.productItem;

import java.util.List;

public class productInfoAdapter extends RecyclerView.Adapter<productInfoAdapter.ProductViewHolder>{
    Context context;
    List<productItem> productsList;
    public itemClick click;
    public productInfoAdapter(Context context, List<productItem> productsList,itemClick click) {
        this.context = context;
        this.productsList = productsList;
        this.click=click;
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_info,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.img.setImageResource(productsList.get(position).getImage());
        holder.name.setText(productsList.get(position).getName());
        holder.price.setText(String.valueOf(productsList.get(position).getPrice()));
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public final class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, price;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.ProductimageView);
            name=itemView.findViewById(R.id.ProductName);
            price=itemView.findViewById(R.id.ProductPrice);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click.onClickItem(getAdapterPosition());
                }

            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    click.onLongClickItem(getAdapterPosition());
                    return true;
                }
            });

        }
    }
}
