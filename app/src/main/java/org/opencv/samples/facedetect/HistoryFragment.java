package org.opencv.samples.facedetect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adsonik.surveillancecamera.R;

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
        final ViewPager pager = view.findViewById(R.id.historyViewPager);

        MainActivity activity = (MainActivity) getActivity();
        activity.getViewModel().allItems.observe(getActivity(), new android.arch.lifecycle.Observer<List<History>>() {
            @Override
            public void onChanged(@Nullable List<History> histories) {
                HistoryViewPagerAdapter adapter = new HistoryViewPagerAdapter(getActivity(), histories);
                pager.setAdapter(adapter);
                pager.setCurrentItem(position);
            }
        });
    }
}
