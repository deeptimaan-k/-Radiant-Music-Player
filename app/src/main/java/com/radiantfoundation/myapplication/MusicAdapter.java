package com.radiantfoundation.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.radiantfoundation.myapplication.MusicFiles;
import com.radiantfoundation.myapplication.R;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyVieHolder> {


    private Context mContext;
    private ArrayList<MusicFiles> mfiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mfiles)
    {
        this.mfiles= mfiles;
        this.mContext= mContext;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mfiles.get(position).getTitle());
        byte[] image =getAbumArt(mfiles.get(position).getPath());
        if(image != null)
        {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        }
        else {
            Glide.with(mContext)
                    .load(R.drawable.logo)
                    .into(holder.album_art);
        }
        holder.itemView.setOnClickListener((v) ->{
                Intent intent = new Intent(mContext,PlayerActivity.class);
                intent.putExtra("position" , position);
                mContext.startActivity(intent);
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu =new PopupMenu(mContext, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) ->{
                    switch (item.getItemId()){
                        case R.id.delete:
                            Toast.makeText(mContext, "Delete Clicked", Toast.LENGTH_SHORT).show();
                            deleteFile(position, v);
                            break;
                    }
                    return true;
                });

            }
        });


    }
    private void deleteFile(int  position, View v){
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mfiles.get(position).getId()));
        File file = new File(mfiles.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mfiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mfiles.size());
            Snackbar.make(v, "File Deleted :", Snackbar.LENGTH_LONG)
                    .show();
        }
        else{
            //may be in sd card
            Snackbar.make(v, "Can't be Deleted :", Snackbar.LENGTH_LONG)
                    .show();

        }
    }

    @Override
    public int getItemCount() {
        return mfiles.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder{
        TextView file_name ;
        ImageView album_art, menuMore;

        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name =itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }
    private byte[] getAbumArt(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art= retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
    void updateList(ArrayList<MusicFiles> musicFilesArrayList)
    {
        mfiles = new ArrayList<>();
        mfiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}

