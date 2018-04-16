package com.example.aml.shushme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.PlaceBuffer;

/**
 * Created by aml on 15/04/18.
 */

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.plassViewHolder> {
    Context context ;
    private PlaceBuffer places ;

    public PlaceListAdapter(Context context , PlaceBuffer places) {
        this.context = context;
        this.places = places;
    }

    @Override
    public plassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.plac_card_item , parent ,false) ;
        return new  plassViewHolder (view);
    }

    @Override
    public void onBindViewHolder(plassViewHolder holder, int position) {
        String placeName = places.get(position).getName().toString();
        String addressName = places.get(position).getAddress().toString();
        holder.name_text_view.setText(placeName);
        holder.address_text_view.setText(addressName);
    }

    @Override
    public int getItemCount() {
        if (places == null) return  0 ;
        return places.getCount();
    }

    public void swapPlaces(PlaceBuffer newPlaces) {
        places  = newPlaces ;

        if (places != null){
            this.notifyDataSetChanged();
        }

    }

    public class plassViewHolder extends RecyclerView.ViewHolder {
           TextView name_text_view ;
        TextView  address_text_view ;
        public plassViewHolder(View itemView) {
            super(itemView);
            name_text_view = (TextView) itemView.findViewById(R.id.name_text_view);
            address_text_view = (TextView) itemView.findViewById(R.id.address_text_view);
        }
    }
}
