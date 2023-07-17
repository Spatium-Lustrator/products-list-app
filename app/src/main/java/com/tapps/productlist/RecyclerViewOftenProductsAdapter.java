package com.tapps.productlist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewOftenProductsAdapter extends RecyclerView.Adapter<RecyclerViewOftenProductsAdapter.ViewHolder>{

    private Context context;
    private List<String> listOftenProducts;
    private LayoutInflater layoutInflater;

    String pressedItem;

    private onOftenProductClick mCallback;

    ArrayList<String> selectedItems = new ArrayList<String>();

    // data is passed into the constructor
    public RecyclerViewOftenProductsAdapter(Context context, List<String> products, onOftenProductClick listener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.listOftenProducts = products;
        this.context = context;
        this.mCallback = listener;

    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_view_often_products_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String product = listOftenProducts.get(position);
        holder.tvOftenProduct.setText(product);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return listOftenProducts.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvOftenProduct;

        ViewHolder(View itemView) {
            super(itemView);
            tvOftenProduct = itemView.findViewById(R.id.tvRecyclerViewOftenProducts);
            tvOftenProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pressedItem = String.valueOf(tvOftenProduct.getText());
                    mCallback.onClickOften(pressedItem);
                }
            });
        }

        @Override
        public void onClick(View view) {
            //if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
            Log.i("RECYCLER-VIEW", "ONCLICK");

        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return listOftenProducts.get(id);
    }

}
