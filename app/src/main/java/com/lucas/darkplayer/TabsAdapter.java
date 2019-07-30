package com.lucas.darkplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by tutlane on 19-12-2017.
 */

public class TabsAdapter extends FragmentPagerAdapter {
    int numberOfTabs;
    public TabsAdapter(FragmentManager fm, int NTabs){
        super(fm);
        this.numberOfTabs = NTabs;
    }
    @Override
    public int getCount() {
        return numberOfTabs;
    }
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                SongFragment songs = new SongFragment();
                return songs;
            case 1:
                PlaylistFragment playlist = new PlaylistFragment();
                return playlist;
            case 2:
                SettingsFragment contact = new SettingsFragment();
                return contact;
            default:
                return null;
        }
    }
}
