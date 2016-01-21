package com.etma.tacticalmedicine.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etma.tacticalmedicine.R;
import com.etma.tacticalmedicine.ZoomImageView;

public class ImageToZoomFragment extends Fragment{
    private static final String TAG = "ImageToZoomFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG,"in onCreateView");
        View infl = inflater.inflate(R.layout.fragment_image, container, false);
//        ZoomImageView myZoomView = new ZoomImageView(getActivity());
//        myZoomView.addView(infl);
        return infl;
    }
}
