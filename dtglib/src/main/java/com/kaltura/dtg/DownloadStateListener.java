package com.kaltura.dtg;

/**
 * Created by Aviran Abady on 7/2/15.
 */
public interface DownloadStateListener {
    void onDownloadComplete(DownloadItem item);

    void onProgressChange(DownloadItem item, long downloadedBytes);

    void onDownloadStart(DownloadItem item);

    void onDownloadPause(DownloadItem item, DownloadStateReason reason, Exception error);

    void onDownloadFailure(DownloadItem item, Exception error);
    
    void onDownloadMetadata(DownloadItem item, Exception error);

    void onDownloadRemoved(String itemId);

    /**
     * Allow application to modify the default track selection.
     * @param item
     */
    void onTracksAvailable(DownloadItem item, DownloadItem.TrackSelector trackSelector);
}

