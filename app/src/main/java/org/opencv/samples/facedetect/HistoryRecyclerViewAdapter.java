package org.opencv.samples.facedetect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adsonik.surveillancecamera.R;
import com.vijay.androidutils.ActivityHolder;
import com.vijay.androidutils.BitmapUtils;
import com.vijay.androidutils.DateUtils;

import java.io.File;
import java.util.List;

/**
 * Created by vijay-3593 on 30/12/17.
 */

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {
    Context context;
    List<History> allDatas;

    public HistoryRecyclerViewAdapter(Context context, List<History> history) {
        this.context = context;
        this.allDatas = history;
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
        File file = new File(getHistoryPath(context));
        if (!file.exists()) {
            file.mkdir();
        }
        File imageFile = new File(file.getPath(), id + ".jpeg");
        if (imageFile.isFile()) {
            Bitmap image = BitmapUtils.getDownScaledImage(imageFile.getPath(), 100, 100);
            return image;
        }
        return null;
    }

    private String getTime(int position) {
        History item = getItem(position);
        return DateUtils.getRelativeTime(item.getCreatedTime());
    }

    public String getHistoryPath(Context context) {
        return context.getFilesDir().toString() + File.separator + MainActivity.Companion.getHISTORY();
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.history_image);
            textView = itemView.findViewById(R.id.history_text);
        }
    }
}
