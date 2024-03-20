package com.radiantfoundation.myapplication;

import static com.radiantfoundation.myapplication.AlbumDetailsAdapter.albumFiles;
import static com.radiantfoundation.myapplication.ApplicationClass.ACTION_NEXT;
import static com.radiantfoundation.myapplication.ApplicationClass.ACTION_PLAY;
import static com.radiantfoundation.myapplication.ApplicationClass.ACTION_PREVIOUS;
import static com.radiantfoundation.myapplication.ApplicationClass.CHANNEL_ID_2;
import static com.radiantfoundation.myapplication.MainActivity.musicFiles;
import static com.radiantfoundation.myapplication.MainActivity.repeateBoolean;
import static com.radiantfoundation.myapplication.MainActivity.shuffleBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements
ActionPlaying, ServiceConnection {
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, back_btn, shuffleBtn, repeateBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    public static Uri uri;
    //static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFulScreen();
        setContentView(R.layout.activity_player);
//        getSupportActionBar().hide()/-;

        intViews();
        getIntenMethod();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(musicService!= null && b){
                    musicService.seekTo(i*1000);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 100);
            }

        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean)
                {
                    shuffleBoolean=false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffleoff);

                }
                else
                {
                    shuffleBoolean= true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeateBoolean)
                {
                    repeateBoolean=false;
                    repeateBtn.setImageResource(R.drawable.ic_repeat_off);

                }
                else{
                    repeateBoolean=true;
                    repeateBtn.setImageResource(R.drawable.ic_repeat_on);

                }
            }
        });

    }

    private void setFulScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPostResume() {
        Intent intent= new Intent(this, MusicService.class);
        bindService(intent,this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this );
    }

    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeateBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeateBoolean) {
                position =((position-1)< 0 ?  (listSongs.size()-1) :(position-1));
            }
            uri= Uri.parse(listSongs.get(position).getPath()) ;
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this, 100);
                }

            });
            musicService.onCompleted();
//            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();

        }
        else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeateBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeateBoolean) {
                position =((position-1)< 0 ?  (listSongs.size()-1) :(position-1));
            }
            uri= Uri.parse(listSongs.get(position).getPath()) ;
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this, 100);
                }

            });
            musicService.onCompleted();
//            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);

        }

    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();

    }

    public void nextBtnClicked() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeateBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeateBoolean) {
                position = ((position+1)% listSongs.size());
            }
            uri= Uri.parse(listSongs.get(position).getPath()) ;
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this, 100);
                }

            });
            musicService.onCompleted();
//            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();

        }
        else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && repeateBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeateBoolean) {
                position = ((position+1)% listSongs.size());
            }
            uri= Uri.parse(listSongs.get(position).getPath()) ;
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this, 100);
                }

            });
            musicService.onCompleted();
//            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);

        }
    }

    private int getRandom(int i) {
        Random random= new Random();

        return random.nextInt(i+1);
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if(musicService.isPlaying())
        {
            playPauseBtn.setImageResource(R.drawable.ic_play);
//            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this, 100);
                }

            });


        }
        else
        {
//            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this, 100);
                }

            });
        }

    }

    private String  formattedTime(int mCurrentPosition) {
        String totalout= "";
        String totalNew="";
        String seconds= String.valueOf(mCurrentPosition%60);
        String minutes= String.valueOf(mCurrentPosition/60);
        totalout= minutes+":"+seconds;
        totalNew =minutes+":"+"0"+seconds;
        if (seconds.length() ==1)
        {
            return totalNew;
        }
        else{
            return totalout;
        }

    }

    private void getIntenMethod() {
        position= getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if(sender !=null && sender.equals("albumDetails"))
        {
            listSongs = albumFiles;
        }
        else{
            listSongs = musicFiles;

        }

        if(listSongs != null){
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent= new Intent(this, MusicService.class);
        intent.putExtra("serviceposition", position);
        startService(intent);




    }

    private void intViews() {
        song_name = findViewById(R.id.song_name);
        artist_name= findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn= findViewById(R.id.id_prev);
        back_btn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle_on);
        repeateBtn = findViewById(R.id.id_repeate);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar= findViewById(R.id.seekBar);





    }
    private void metaData(Uri uri){
        MediaMetadataRetriever retriever =new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration())/1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art != null)
        {

            bitmap = BitmapFactory.decodeByteArray(art, 0,art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch= palette.getDominantSwatch();
                    if(swatch != null){
                        ImageView gredient = findViewById(R.id.imageViewGradiant);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.gradiant_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP
                        , new int[] {swatch.getRgb(),0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP
                                , new int[] {swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                    }
                    else{
                        ImageView gredient = findViewById(R.id.imageViewGradiant);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.gradiant_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP
                                , new int[] {0x000000,0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP
                                , new int[] {0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                    }
                }
            });
        }
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.logo)
                    .into(cover_art);
            ImageView gredient = findViewById(R.id.imageViewGradiant);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gredient.setBackgroundResource(R.drawable.gradiant_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
        }
    }
    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }


    @Override
    public void onServiceConnected(ComponentName Name, IBinder service) {
        MusicService.MyBinder myBinder =(MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        Toast.makeText(this,"deeptimaan"+musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
//        musicService.showNotification(R.drawable.ic_pause);




    }

    @Override
    public void onServiceDisconnected(ComponentName Name) {
        musicService =null;

    }



}