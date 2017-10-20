package com.wlanjie.ffmpeg.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wlanjie.ffmpeg.FFmpeg;
import com.wlanjie.ffmpeg.library.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wlanjie on 2017/8/31.
 */
public class VideoFrameActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_frame);
    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    final VideoFrameAdapter adapter = new VideoFrameAdapter();
    recyclerView.setAdapter(adapter);
    Observable.create(new ObservableOnSubscribe<List<Bitmap>>() {
      @Override
      public void subscribe(ObservableEmitter<List<Bitmap>> e) throws Exception {
        int result = FFmpeg.getInstance().openInput("/sdcard/a.mp4");
        if (result != 0) {
          e.onError(new RuntimeException());
          return;
        }
        List<Bitmap> videoFrames = FFmpeg.getInstance().getVideoFrame();
        e.onNext(videoFrames);
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<Bitmap>>() {
          @Override
          public void accept(List<Bitmap> bitmaps) throws Exception {
            adapter.setBitmaps(bitmaps);
            FFmpeg.getInstance().release();
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            throwable.printStackTrace();
            FFmpeg.getInstance().release();
          }
        });
  }

  class VideoFrameAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<Bitmap> mBitmaps = new ArrayList<>();

    void setBitmaps(List<Bitmap> bitmaps) {
      if (bitmaps == null || bitmaps.isEmpty()) {
        return;
      }
      mBitmaps.clear();
      mBitmaps.addAll(bitmaps);
      notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.item_video_frame, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      Bitmap bitmap = mBitmaps.get(position);
      holder.imageView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
      return mBitmaps.size();
    }
  }

  class ViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    ViewHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView;
    }
  }
}
