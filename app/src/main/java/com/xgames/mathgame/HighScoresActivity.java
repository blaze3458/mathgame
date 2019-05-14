package com.xgames.mathgame;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HighScoresActivity extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    HighScore easyScores,mediumScores,hardScores;
    ArrayList<HighScore> highScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        getScores();
    }


    private void getScores(){
        easyScores = new HighScore("HighScoreEasy",this);
        mediumScores = new HighScore("HighScoreMedium",this);
        hardScores = new HighScore("HighScoreHard",this);

        highScores = new ArrayList<>();
        highScores.add(easyScores);
        highScores.add(mediumScores);
        highScores.add(hardScores);
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String SCORE_TABLE = "scoreTable";

        private scoreListAdapter adapter;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber,HighScore scoreTable) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            HighScoreData  data = scoreTable.getHighScoreData();
            args.putParcelable(SCORE_TABLE,data);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_high_scores, container, false);
            ListView list = rootView.findViewById(R.id.scoreList);
            HighScoreData scoreTable = (HighScoreData)getArguments().getParcelable(SCORE_TABLE);
            adapter = new scoreListAdapter(getContext(),scoreTable.getUserNames(),scoreTable.getScores());
            list.setAdapter(adapter);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1,highScores.get(position));
        }

        @Override
        public int getCount() { return 3;}
    }

    private static class scoreListAdapter extends BaseAdapter{

        private String userNames[] = new String[10];
        private int scores[] = new int[10];
        Context cont;

        public scoreListAdapter(Context cont,String userNames[],int scores[]){
            this.userNames = userNames;
            this.scores = scores;
            this.cont = cont;
        }

        @Override
        public int getCount() {return 10;}

        @Override
        public Object getItem(int position) {return null;}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(cont).inflate(R.layout.score_list_element, null);

            TextView userName = v.findViewById(R.id.userName);
            TextView score = v.findViewById(R.id.score);

            userName.setText(userNames[position]);
            score.setText(String.valueOf(scores[position]));
            return v;
        }
    }
}
