package com.danielkim.soundrecorder.listeners;

/**
 * Created by Daniel on 1/3/2015.
 * Listen for add/rename items in database
 */
//TODO: fixe the ondatabaseentryFAVD so it works?
public interface OnDatabaseChangedListener{
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRenamed();
    //void onDatabaseEntryFavd();
}