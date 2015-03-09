package com.example.cs65project.habitme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * View pager sliding tabs
 */
public class ActionTabsViewPagerAdapter extends FragmentPagerAdapter {
    //pre defined keys
    public static final int HOME = 0;
    public static final int FRIEND = 1;
    public static final int SETTING = 2;
    public static final String UI_TAB_HOME = "HOME";
    public static final String UI_TAB_FRIEND = "FRIEND";
    public static final String UI_TAB_SETTING = "SETTING";
    private ArrayList<Fragment> fragments;
    private FragmentManager mFragmentManager;

    /**
     * ActionTabsViewPagerAdapter constructor
     * @param fm
     * @param fragments
     */
    public ActionTabsViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments){
        super(fm);
        this.mFragmentManager = fm;
        this.fragments = fragments;
    }

    /**
     * Get item from position
     * @param pos
     * @return
     */
    public Fragment getItem(int pos){
        return fragments.get(pos);
    }

    /**
     * Get fragment items size
     * @return
     */
    public int getCount(){
        return fragments.size();
    }

    /**
     * Get page title with position
     * @param position
     * @return
     */
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case HOME:
                return UI_TAB_HOME;
            case FRIEND:
                return UI_TAB_FRIEND;
            case SETTING:
                return UI_TAB_SETTING;
            default:
                break;
        }
        return null;
    }
}
