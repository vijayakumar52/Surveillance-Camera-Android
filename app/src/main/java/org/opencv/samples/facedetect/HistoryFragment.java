package org.opencv.samples.facedetect;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adsonik.surveillancecamera.R;
import com.vijay.androidutils.ActivityHolder;

import java.util.List;

/**
 * Created by vijay-3593 on 31/12/17.
 */

public class HistoryFragment extends Fragment {
    int position;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_viewpager, null, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager pager = view.findViewById(R.id.historyViewPager);
        new Loader(pager).execute();}


    class Loader extends AsyncTask<String, Integer, List<History>> {
        ViewPager viewPager;

        public Loader(ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        protected List<History> doInBackground(String... strings) {
            List<History> allDatas = ((MainActivity) ActivityHolder.getInstance().getActivity()).getDatabase().getHistoryDao().getAll();
            return allDatas;
        }

        @Override
        protected void onPostExecute(List<History> result) {
            super.onPostExecute(result);
            MainActivity activity = (MainActivity) getActivity();
            HistoryViewPagerAdapter adapter = new HistoryViewPagerAdapter(getActivity(), result);
            this.viewPager.setAdapter(adapter);
            this.viewPager.setCurrentItem(position);
        }
    }
}
