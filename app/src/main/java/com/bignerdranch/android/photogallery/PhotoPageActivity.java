package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class PhotoPageActivity extends SingleFragmentActivity {

    private PhotoPageFragment fragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        fragment = PhotoPageFragment.newInstance(getIntent().getData());
        return fragment;
    }

    /////////////////////
    //// CHALLENGE 1 ////
    /////////////////////

    @Override                                                                                       // challenge 1, page 524-525 (zie ook PhotoPageFragment)
    public void onBackPressed() {
        if (fragment.canGoBack()) {
            fragment.goBack();
            return;
        }else {

            // Otherwise defer to system default behavior.
            super.onBackPressed();
        }
    }
    //////// END /////////
}
