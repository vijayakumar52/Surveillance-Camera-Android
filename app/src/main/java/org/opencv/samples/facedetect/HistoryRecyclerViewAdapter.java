package org.opencv.samples.facedetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adsonik.surveillancecamera.R;
import com.vijay.androidutils.BitmapCache;
import com.vijay.androidutils.BitmapUtils;
import com.vijay.androidutils.DateUtils;

import java.io.File;
import java.util.List;

/**
 * Created by vijay-3593 on 30/12/17.
 */

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {
    private Context context;
    private List<History> allDatas;
    private BitmapCache bitmapCache;

    public HistoryRecyclerViewAdapter(Context context, List<History> history) {
        this.context = context;
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
            File imageFile = new File(getHistoryPath(context), id + ".jpeg");
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

    @Override
    public int getItemCount() {
        return allDatas.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView textView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.history_image);
            textView = itemView.findViewById(R.id.history_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
