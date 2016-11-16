package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhotoGalleryFragment extends VisibleFragment {


    private RecyclerView mPhotoRecyclerView;
    private static final String API_KEY = "d4d9d41fa47c32e5d9a0eee404354498";
    public static final String BASE_URL = "https://api.flickr.com/";
    private static List<GalleryItem> mGalleryItems;
    private int pageNumber = 1;                                                                     // Challenge chapter 23 (2)
    private static final String TAG= PhotoGalleryFragment.class.getSimpleName() ;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setHasOptionsMenu(true);                                                                    // to show the menu (in the app) the menu onOptionsItemSelected (page 479)

//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);

//        PollService.setServiceAlarm(getActivity(), true);                                         // page 479
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:                                                              // From the book (page 479) but we wil not use it?!?
                QueryPreferences.setStoredQuery(getActivity(), null);
                new FlickrFetch().getData();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem toggleItem = menu. findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                new FlickrFetch().getSearchData(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));                       // Means 3 columns of photo's
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {                        // Challenge chapter 23 (2)

            @Override                                                                                       // Challenge chapter 23 (2)
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override                                                                                           // Challenge chapter 23 (2)
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(dy)) {
                    if (pageNumber < 10) {
                        pageNumber++;
                        new FlickrFetch().getData();
                    }
                }
            }
        });

        new FlickrFetch().getData();                                                                                  // FROM SLIDES
        return v;
    }

    public class FlickrFetch {                                                                                       // *START* FROM SLIDES + more (chapter 25)...retrofit

        private ApiEndpointInterface prepareRetrofit() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            return retrofit.create(ApiEndpointInterface.class);
        }

        private void setResponse(Response<GalleryApiResponse> response) {
            GalleryApiResponse mGalleryApiResponse = response.body();
            if (response.body() == null) {
                Log.e("Retrofit body null", String.valueOf(response.code()));
            }
            mGalleryItems = mGalleryApiResponse.getGalleryItems();
            Log.v("mGalleryItems", String.valueOf(response.body().getGalleryItems().size()));
            if (mPhotoRecyclerView != null) {
                mPhotoRecyclerView.setAdapter(new PhotoAdapter(mGalleryItems));
            }
        }

        public void getData() {

            ApiEndpointInterface apiResponse = prepareRetrofit();
            apiResponse.getGalleryItems(API_KEY, pageNumber).enqueue(new Callback<GalleryApiResponse>() {

                @Override
                public void onResponse(Call<GalleryApiResponse> call, Response<GalleryApiResponse> response) {

                    setResponse(response);
                }

                @Override
                public void onFailure(Call<GalleryApiResponse> call, Throwable t) {
                    Log.e("Retrofit error", t.getMessage());
                }
            });
        }

            public void getSearchData(String s){

                ApiEndpointInterface apiResponseSearch = prepareRetrofit();
                apiResponseSearch.getSearch(API_KEY, s).enqueue(new Callback<GalleryApiResponse>() {

                    @Override
                    public void onResponse(Call<GalleryApiResponse> call, Response<GalleryApiResponse> response) {

                        setResponse(response);
                    }

                    @Override
                    public void onFailure(Call<GalleryApiResponse> call, Throwable t) {
                        Log.e("Retrofit error", t.getMessage());
                    }
                });
        }
    }                                                                                                                 // *END* FROM SLIDES

    public static  List<GalleryItem> getGalleryItems() {
        return mGalleryItems;
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {                       // *START* FROM SLIDES     +     page 515 (implements ...)

        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.iv_photo_gallery_fragment);
            itemView.setOnClickListener(this);                                                              // firing implicit intent when item is pressed (page 515)
        }

        public void bindGalleryItem(GalleryItem item) {
            Glide.with(getActivity())
                    .load(item.getUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(mItemImageView);
                                                                                                                         // *END* FROM SLIDES
            mGalleryItem = item;                                                                           // firing implicit intent when item is pressed (page 515)
        }

        @Override                                                                                           // firing implicit intent when item is pressed (page 515)
        public void onClick(View v) {
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem mGalleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(mGalleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}