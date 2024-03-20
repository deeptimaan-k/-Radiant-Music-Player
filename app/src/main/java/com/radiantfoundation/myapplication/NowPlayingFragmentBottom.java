package com.radiantfoundation.myapplication;

import static com.radiantfoundation.myapplication.MainActivity.SHOW_MINI_PLAYER;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragmentBottom extends Fragment {
    ImageView nextBtn, albumArt;
    TextView artistName, songName;
    FloatingActionButton playPauseBtn;
    View view;

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_now_playing_bottom,
                container, false);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        artistName = view.findViewById(R.id.song_artist_mini);
        playPauseBtn = view.findViewById(R.id.paly_pause_miniPlayer);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        if(SHOW_MINI_PLAYER){

        }

        return view;
    }
}