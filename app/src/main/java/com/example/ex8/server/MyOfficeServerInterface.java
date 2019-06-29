package com.example.ex8.server;

import com.example.ex8.data.AvailableImages;
import com.example.ex8.data.SetUserProfileImageRequest;
import com.example.ex8.data.SetUserPrettyNameRequest;
import com.example.ex8.data.TokenResponse;
import com.example.ex8.data.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyOfficeServerInterface {

    @GET("/users/{username}/token/")
    Call<TokenResponse> getToken(@Path("username") String username);

    @GET("/user/")
    Call <UserResponse> getUserInfo(@Header("Authorization") String token);

    @Headers("Content-Type: application/json")
    @POST("/user/edit/")
    Call<UserResponse> updateUserInfo(@Header("Authorization") String token,
                                      @Body SetUserPrettyNameRequest request);

    @Headers("Content-Type: application/json")
    @POST("/user/edit/")
    Call<UserResponse> updateUserInfo(@Header("Authorization") String token,
                                      @Body SetUserProfileImageRequest request);

    @GET("/images/all/")
    Call<AvailableImages> getAvailableImages();
}
