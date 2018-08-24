package com.ambrosus.rfidviewer.models;

/**
 * Wrapper class that holds an EPC code and starts fetching asset data in the background
 */
public final class ScanEntry {
    private final String epc;
    private String assetId;
    private String assetName;

    public ScanEntry(String epc){
        this.epc = epc;
    }

    public String getEpc() {
        return epc;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
