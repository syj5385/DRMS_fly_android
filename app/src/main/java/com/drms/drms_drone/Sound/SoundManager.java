package com.drms.drms_drone.Sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * Created by jjunj on 2017-09-20.
 */

public class SoundManager {
    private SoundPool mSoundPool;
    private Context mContext;
    private HashMap<Integer,Integer> map;

    public SoundManager(Context mContext) {
        super();

        this.mContext = mContext;
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
        map = new HashMap<Integer,Integer>();
    }

    public void addSound(int index, int resid){
        int id = mSoundPool.load(mContext,resid,1);
        map.put(index,id);
    }

    public void play(int index){
        mSoundPool.play(map.get(index),1,1,1,0,1);
    }

    public void stopSound(int index){
        mSoundPool.stop(map.get(index));
    }

}
