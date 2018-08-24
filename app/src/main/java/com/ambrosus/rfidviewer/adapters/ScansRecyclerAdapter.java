package com.ambrosus.rfidviewer.adapters;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ambrosus.rfidviewer.R;
import com.ambrosus.rfidviewer.TSLBluetoothDeviceApplication;
import com.ambrosus.rfidviewer.models.ScanEntry;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import commons.Info;
import model.AMBAsset;
import model.AMBEvent;
import model.AmbrosusSDK;
import utils.Optional;


public class ScansRecyclerAdapter extends RecyclerView.Adapter<ScanViewHolder> {

    private final Activity context;
    private final List<ScanEntry> ids;
    private final Set<String> epcSet;
    private RecyclerView recyclerView;
    private AmbrosusSDK sdk;

    //TODO:
    /*
        wrap EPC code in entry manager
        manager starts download task with AMBSdk to get asset IDs
        when task completes, update recycler view

        > need manager in this class. When add entry to the entry list,
        register a task in the manager.
        Then, when a download task completes, manager calls back the adapter to notify data changed on position of entry
     */

    public ScansRecyclerAdapter(final Activity context) {
        this.context = context;
        this.ids = new ArrayList<>();
        this.epcSet = new HashSet<>();
        sdk = ((TSLBluetoothDeviceApplication) context.getApplication()).ambrosusSDK();

    }

    public void addEntry(String epcCode) {

        System.out.print("On main thread: ");
        System.out.println(Looper.myLooper() == Looper.getMainLooper());

        if (!epcSet.contains(epcCode)) {
            epcSet.add(epcCode);

            int insertionIndex = ids.size();
            ids.add(new ScanEntry(epcCode));

            notifyItemInserted(insertionIndex);
            getEntryAssetId(insertionIndex);
            recyclerView.scrollToPosition(insertionIndex);
        }
    }

    private void getEntryAssetId(int entryIndex) {

        Map<String, String> params = new HashMap<>();
        params.put("identifier[RFID]", ids.get(entryIndex).getEpc());

        sdk.findAssets(params, qResponse -> {
            if (qResponse.hasBody() && qResponse.body().getResultCount() > 0) {
                AMBAsset firstAsset = qResponse.body().getResults().get(0);
                ids.get(entryIndex).setAssetId(firstAsset.getAssetId());
                getEntryName(entryIndex, firstAsset.getAssetId());
            }
            notifyItemChanged(entryIndex);
        });
    }

    private void getEntryName(int entryIndex, String assetId) {

        Map<String, String> params = new HashMap<>();
        params.put("assetId", assetId);
        params.put("data[type]", "ambrosus.asset.info");

        sdk.findEvents(params, eventQResponse -> {
            if (eventQResponse.hasBody() && eventQResponse.body().getResultCount() > 0) {
                AMBEvent firstInfoEvent = eventQResponse.body().getResults().get(0);

                List<Info> infoData = firstInfoEvent.eventDataWithType(Info.class);

                if (infoData.size() > 0) {
                    Info info = infoData.get(0);
                    Optional<JsonElement> name = info.getElementWithPath("name");
                    if (name.isPresent()) {
                        ids.get(entryIndex).setAssetName(name.get().getAsString());
                    }
                }
            }
            notifyItemChanged(entryIndex);
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            default:
                return new ScanViewHolder(
                        context,
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_entry, parent, false)
                );
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {

        holder.setEntry(ids.get(position));

    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }
}

