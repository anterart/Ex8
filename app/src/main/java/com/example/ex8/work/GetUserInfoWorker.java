package com.example.ex8.work;

import android.content.Context;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex8.data.UserResponse;
import com.example.ex8.server.MyOfficeServerInterface;
import com.example.ex8.server.ServerHolder;
import com.google.gson.Gson;

import retrofit2.Response;

public class GetUserInfoWorker extends Worker {
    public GetUserInfoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
            String token = getInputData().getString("token");
            Response<UserResponse> response = serverInterface.getUserInfo(token).execute();
            UserResponse userResponse = response.body();
            String userResponseAsJson = new Gson().toJson(userResponse);
            Data outputData = new Data.Builder()
                    .putString("user", userResponseAsJson)
                    .build();
            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
