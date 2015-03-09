package com.example.cs65project.habitme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * This is the Home fragment in main activity.
 * Display the habit list of the user
 */
public class HomeFragment extends Fragment {

    private Activity mContext;
    public static EntryDataSource dataSource;

    //display contents with cardveiw
    public static List<HabitItem> mEntryList;
    private static HomeListAdapter ca;
    private static RecyclerView recList;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recList = (RecyclerView) v.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        recList.setLayoutManager(llm);
        ca = new HomeListAdapter(mEntryList);
        recList.setAdapter(ca);

        //when user clicks on a certain habit, go to the check habit activity to check details of this habit
        //if there is a habit, which is the provided one, is going on, other provided habits on the list will be blocked.
        recList.addOnItemTouchListener(
                new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(mContext, CheckHabitActivity.class);
                        HabitItem mEntry = mEntryList.get(position);

                        //check whether you have unfinished habit
                        if(mEntry.getCreateType() == Globals.INPUTTYPE_CHOOSE){
                            if(Globals.UNFINISHED_HABIT) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setMessage("You cannot check in another habit. Finish your current one first!");
                                builder.setTitle("Fail to check");
                                builder.setIcon(R.drawable.sunshineicon);
                                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.create().show();
                            }else{
                                intent.putExtra("ID", mEntry.getId());
                                intent.putExtra("Title", mEntry.getHabitTitle());
                                startActivity(intent);
                            }
                        }else{
                            intent.putExtra("ID", mEntry.getId());
                            intent.putExtra("Title", mEntry.getHabitTitle());
                            startActivity(intent);
                        }

                    }

                })
        );
        return v;
    }

    /**
     * update the habit list displayed on the screen
     */
    public static void updateEntries() {
        mEntryList = dataSource.fetchEntries();
        ca = new HomeListAdapter(mEntryList);
        recList.setAdapter(ca);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment NotificationFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (savedInstanceState != null) {
            dataSource = new EntryDataSource(mContext);
            dataSource.open();
            updateEntries();
        }
        //open databases
        else{
            dataSource = new EntryDataSource(mContext);
            dataSource.open();
            //show items from databases
            mEntryList = dataSource.fetchEntries();
        }
    }

}