package com.bignerdranch.android.photogallery;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiEndpointInterface {

    @GET("services/rest/?method=flickr.photos.getRecent&extras=url_s&format=json&nojsoncallback=1")
    Call<GalleryApiResponse> getGalleryItems(@Query("api_key")String apikey,
                                             @Query("page") int page);

    @GET("services/rest/?method=flickr.photos.search&extras=url_s&format=json&nojsoncallback=1")
    Call<GalleryApiResponse> getSearch(@Query("api_key")String apikey,
                                       @Query("text") String searchtext);
}
