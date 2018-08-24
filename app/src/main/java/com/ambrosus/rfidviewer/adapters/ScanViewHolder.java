package com.ambrosus.rfidviewer.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ambrosus.rfidviewer.R;
import com.ambrosus.rfidviewer.models.ScanEntry;

public class ScanViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final Resources res;
    private CardView card;
    private TextView EPC;
    private TextView assetAddress;
    private TextView assetName;

    ScanViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        this.res = context.getResources();
        card = itemView.findViewById(R.id.scanCard);
        EPC = itemView.findViewById(R.id.scanEPC);
        assetAddress = itemView.findViewById(R.id.scanHexId);
        assetName = itemView.findViewById(R.id.scanAssetName);
        assetName.setVisibility(View.GONE);
    }

    public void setEntry(ScanEntry entry) {
        String epc = entry.getEpc();
        String id = entry.getAssetId();
        String name = entry.getAssetName();

        EPC.setText(String.format(res.getString(R.string.EPC_code), epc));

        if(id != null) {
            card.setOnClickListener(view -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dashboard.ambrosus" +
                        ".com/assets/"+id));
                context.startActivity(browserIntent);
            });
            assetAddress.setText(id);
            assetAddress.setTextColor(res.getColorStateList(R.color.greenAccent700));
        } else {
            card.setOnClickListener(null);
            assetAddress.setText(res.getString(R.string.asset_not_found));
            assetAddress.setTextColor(res.getColorStateList(R.color.redAccent700));
        }

        if(name != null){
            assetName.setVisibility(View.VISIBLE);
            assetName.setText(name);
        } else {
            assetName.setVisibility(View.GONE);
        }
    }

    public void setAssetAddress(String address){
        assetAddress.setText(address);
    }

    public void setAssetName(String name){
        assetName.setText(name);
    }
}
