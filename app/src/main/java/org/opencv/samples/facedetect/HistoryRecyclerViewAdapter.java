package org.opencv.samples.facedetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adsonik.surveillancecamera.R;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.vijay.androidutils.ActivityHolder;
import com.vijay.androidutils.BitmapCache;
import com.vijay.androidutils.BitmapUtils;
import com.vijay.androidutils.DateUtils;
import com.vijay.androidutils.DialogUtils;

import java.io.File;
import java.util.List;

/**
 * Created by vijay-3593 on 30/12/17.
 */

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {
    private MainActivity activity;
    private List<History> allDatas;
    private BitmapCache bitmapCache;

    public HistoryRecyclerViewAdapter(MainActivity activity, List<History> history) {
        this.activity = activity;
        this.allDatas = history;
        this.bitmapCache = new BitmapCache();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_component, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        holder.textView.setText(getTime(position));
        holder.imageView.setImageBitmap(getImage(position));
    }

    private History getItem(int position) {
        return allDatas.get(position);
    }


    private Bitmap getImage(int position) {
        History item = getItem(position);
        long id = item.getCreatedTime();

        Bitmap bitmap = bitmapCache.getBitmap(id + "");
        if (bitmap == null) {
            File imageFile = new File(getHistoryPath(activity), id + ".jpeg");
            if (imageFile.isFile()) {
                bitmap = BitmapUtils.getDownScaledImage(imageFile.getPath(), 300, 300);
                bitmapCache.putBitmap(id + "", bitmap);
            }
        }
        return bitmap;
    }

    private String getTime(int position) {
        History item = getItem(position);
        return DateUtils.getRelativeTime(item.getCreatedTime());
    }

    public static String getHistoryPath(Context context) {
        File file = new File(context.getFilesDir(), MainActivity.Companion.getHISTORY());
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath();
    }

    public void notifyDataChanged(List<History> newData){
        this.allDatas = newData;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return allDatas.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imageView;
        TextView textView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.history_image);
            textView = itemView.findViewById(R.id.history_text);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            AppCompatActivity activity = (AppCompatActivity) ActivityHolder.getInstance().getActivity();
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            Fragment fragment = new HistoryFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", getAdapterPosition());
            fragment.setArguments(bundle);
            ft.replace(android.R.id.content, fragment, "pagerFragment");
            ft.addToBackStack("historyView");
            ft.commit();
        }

        @Override
        public boolean onLongClick(View v) {
            String tile = activity.getResources().getString(R.string.dialog_title_delete);
            String content = activity.getResources().getString(R.string.dialog_content_delete);
            String posBtn = activity.getResources().getString(R.string.dialot_ok);
            String negBtn = activity.getResources().getString(R.string.dialog_cancel);
            DialogUtils.getInstance().twoButtonDialog(activity, tile, content, posBtn, negBtn, true, new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (which == DialogAction.POSITIVE) {
                        History currentItem = getItem(getAdapterPosition());
                        activity.getViewModel().deleteItem(currentItem);
                    }
                }
            }, null);
            return false;
        }
    }
}
