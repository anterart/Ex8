package com.example.ex8.work;

import android.content.Context;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ex8.data.AvailableImages;
import com.example.ex8.data.TokenResponse;
import com.example.ex8.server.MyOfficeServerInterface;
import com.example.ex8.server.ServerHolder;
import com.google.gson.Gson;

import retrofit2.Response;

public class GetAvailableImagesWorker extends Worker {
    public GetAvailableImagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            MyOfficeServerInterface serverInterface = ServerHolder.getInstance().serverInterface;
            Response<AvailableImages> response = serverInterface.getAvailableImages().execute();
            AvailableImages availableImagesResponse = response.body();
            String availableImagesResponseAsJson = new Gson().toJson(availableImagesResponse);
            Data outputData = new Data.Builder()
                    .putString("allImages", availableImagesResponseAsJson)
                    .build();
            return Result.success(outputData);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
