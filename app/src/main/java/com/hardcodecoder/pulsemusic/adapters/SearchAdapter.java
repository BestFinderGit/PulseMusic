package com.hardcodecoder.pulsemusic.adapters;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hardcodecoder.pulsemusic.GlideApp;
import com.hardcodecoder.pulsemusic.GlideConstantArtifacts;
import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.TaskRunner;
import com.hardcodecoder.pulsemusic.helper.DiffCb;
import com.hardcodecoder.pulsemusic.helper.MediaArtHelper;
import com.hardcodecoder.pulsemusic.interfaces.ItemClickListener;
import com.hardcodecoder.pulsemusic.model.MusicModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolderLibrary> {

    protected List<MusicModel> list = new ArrayList<>();
    private Deque<List<MusicModel>> pendingUpdates = new ArrayDeque<>();
    private ItemClickListener.Simple mListener;
    private LayoutInflater mInflater;
    private Handler mMainHandler = new Handler();

    public SearchAdapter(LayoutInflater inflater, ItemClickListener.Simple clickListener) {
        this.mListener = clickListener;
        this.mInflater = inflater;
    }

    @NonNull
    @Override
    public MyViewHolderLibrary onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolderLibrary(mInflater.inflate(R.layout.rv_search_item, parent, false), mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderLibrary holder, int position) {
        holder.setItemData(list.get(position));
    }

    @Override
    public int getItemCount() {
        if (list != null)
            return list.size();
        else
            return 0;
    }

    public void updateItems(final List<MusicModel> newItems) {
        pendingUpdates.push(newItems);
        if (pendingUpdates.size() > 1) {
            return;
        }
        updateItemsInternal(newItems);
    }


    private void updateItemsInternal(final List<MusicModel> newItems) {
        /*mBackgroundHandler.post(() -> {
            final List<MusicModel> oldItems = new ArrayList<>(this.list);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCb(oldItems, newItems));
            mMainHandler.post(() -> applyDiffResult(newItems, diffResult));
        });*/
        TaskRunner.executeAsync(() -> {
            final List<MusicModel> oldItems = new ArrayList<>(this.list);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCb(oldItems, newItems));
            mMainHandler.post(() -> applyDiffResult(newItems, diffResult));
        });
    }

    private void applyDiffResult(List<MusicModel> newItems, DiffUtil.DiffResult diffResult) {
        pendingUpdates.remove(newItems);
        dispatchUpdates(newItems, diffResult);
        if (pendingUpdates.size() > 0) {
            List<MusicModel> latest = pendingUpdates.pop();
            pendingUpdates.clear();
            updateItemsInternal(latest);
        }
    }

    private void dispatchUpdates(List<MusicModel> newItems, DiffUtil.DiffResult diffResult) {
        diffResult.dispatchUpdatesTo(this);
        list.clear();
        list.addAll(newItems);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        pendingUpdates.clear();
        list.clear();
    }

    /*
     * Custom View holder class
     */
    static class MyViewHolderLibrary extends RecyclerView.ViewHolder {

        private TextView songName;
        private ImageView albumArt;

        MyViewHolderLibrary(View itemView, ItemClickListener.Simple listener) {
            super(itemView);
            songName = itemView.findViewById(R.id.search_item_title);
            albumArt = itemView.findViewById(R.id.search_item_art);
            itemView.setOnClickListener(v -> listener.onItemClick(getAdapterPosition()));
            itemView.findViewById(R.id.search_item_options_btn)
                    .setOnClickListener(v -> listener.onOptionsClick(v, getAdapterPosition()));
        }

        void setItemData(MusicModel md) {
            songName.setText(md.getSongName());
            GlideApp.with(itemView.getContext())
                    .load(md.getAlbumArtUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            MediaArtHelper.getMediaArtDrawableAsync(itemView.getContext(), md.getAlbumId(),
                                    MediaArtHelper.RoundingRadius.RADIUS_4dp,
                                    drawable -> albumArt.setImageDrawable(drawable));
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .transform(GlideConstantArtifacts.getRadius8dp())
                    .into(albumArt);
        }
    }
}

