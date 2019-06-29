package com.example.ex8.work;

import android.content.Context;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex8.data.TokenResponse;
import com.example.ex8.server.MyOfficeServerInterface;
import com.example.ex8.server.ServerHolder;
import com.google.gson.Gson;

import retrofit2.Response;

public class GetTokenWorker extends Worker {
    public GetTokenWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
            String username = getInputData().getString("username");
            Response<TokenResponse> response = serverInterface.getToken(username).execute();
            TokenResponse tokenResponse = response.body();
            String tokenAsJson = new Gson().toJson(tokenResponse);
            Data outputData = new Data.Builder()
                    .putString("token", tokenAsJson)
                    .build();
            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
