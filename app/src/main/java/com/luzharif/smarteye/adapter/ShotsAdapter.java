package com.luzharif.smarteye.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luzharif.smarteye.R;
import com.luzharif.smarteye.model.Shots;

import java.io.File;
import java.util.List;

/**
 * Created by LuZharif on 23/04/2016.
 */
public class ShotsAdapter extends RecyclerView.Adapter<ShotsAdapter.MyViewHolder>{

    private List<Shots> shotsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name_shot, name_fruit, fruit_quality;
        public ImageView image_fruit;
        public MyViewHolder(View itemView) {
            super(itemView);
            name_shot = (TextView) itemView.findViewById(R.id.nameshot);
            name_fruit = (TextView) itemView.findViewById(R.id.namefruit);
            fruit_quality = (TextView) itemView.findViewById(R.id.fruitquality);
            image_fruit = (ImageView) itemView.findViewById(R.id.imagefruit);
        }
    }

    public ShotsAdapter(List<Shots> shotsList) {
        this.shotsList = shotsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shots_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Shots shots = shotsList.get(position);
        File img = new File(String.valueOf(shots.getImageFruit()));
        holder.name_shot.setText(String.valueOf(shots.getNameShot()));
        holder.name_fruit.setText(String.valueOf(shots.getNameFruit()));
        holder.fruit_quality.setText("Kualitas: " + String.valueOf(shots.getFruitQuality()));
        //TODO Buat fungsi baca thumbnail saja
//        if (img.exists()) {
//            Bitmap bm = BitmapFactory.decodeFile(img.getAbsolutePath());
//            holder.image_fruit.setImageBitmap(bm);
//        }
    }

    @Override
    public int getItemCount() {
        return shotsList.size();
    }

}
