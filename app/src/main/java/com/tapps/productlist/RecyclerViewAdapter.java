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

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private Context context;
    private List<String> listProducts;
    private LayoutInflater layoutInflater;

    private onCheckBoxClick mCallback;

    ArrayList<String> selectedItems = new ArrayList<String>();

    // data is passed into the constructor
    public RecyclerViewAdapter(Context context, List<String> products, onCheckBoxClick listener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.listProducts = products;
        this.context = context;
        this.mCallback = listener;

    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recycler_view_products_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String product = listProducts.get(position);
        holder.tvProduct.setText(product);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return listProducts.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvProduct;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            tvProduct = itemView.findViewById(R.id.tvRecyclerViewItemProductName);
            checkBox = itemView.findViewById(R.id.chbRecyclerViewItemProduct);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox.isChecked()) {
                        selectedItems.add(String.valueOf(tvProduct.getText()));
                    } else {
                        selectedItems.remove(String.valueOf(tvProduct.getText()));
                    }
                    //Log.i("DEVELOP-DELETING", String.valueOf(selectedItems));
                    mCallback.onClick(selectedItems);
                }
            });
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
            Log.i("RECYCLER-VIEW", "ONCLICK");

        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return listProducts.get(id);
    }

}