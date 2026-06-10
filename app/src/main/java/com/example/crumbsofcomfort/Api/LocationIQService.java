package com.example.crumbsofcomfort.Api;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationIQService {
    @GET("v1/search.php")
    Call<List<LocationIQResponse>> getCoordinates(
            @Query("key") String apiKey,
            @Query("q") String address,
            @Query("format") String format
    );
}
