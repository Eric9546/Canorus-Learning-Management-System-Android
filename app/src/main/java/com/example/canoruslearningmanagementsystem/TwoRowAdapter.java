package com.example.canoruslearningmanagementsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TwoRowAdapter extends RecyclerView.Adapter<TwoRowAdapter.ViewHolder>
{

    private List<String> mRow1;
    private List<String> mRow2;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    TwoRowAdapter(Context context, List<String> row1, List<String> row2) {
        this.mInflater = LayoutInflater.from(context);
        this.mRow1 = row1;
        this.mRow2 = row2;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_two_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String row1 = mRow1.get(position);
        String row2 = mRow2.get(position);
        holder.myTextView.setText(row1);
        holder.myTextView2.setText(row2);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mRow1.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        TextView myTextView2;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.recyclerTwoRowItem1);
            myTextView2 = itemView.findViewById(R.id.recyclerTwoRowItem2);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mRow1.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
