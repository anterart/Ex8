package com.example.ex8;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.ex8.data.AvailableImages;
import com.example.ex8.data.SetUserPrettyNameRequest;
import com.example.ex8.data.TokenResponse;
import com.example.ex8.data.User;
import com.example.ex8.data.UserResponse;
import com.example.ex8.server.ServerHolder;
import com.example.ex8.work.GetAvailableImagesWorker;
import com.example.ex8.work.GetTokenWorker;
import com.example.ex8.work.GetUserInfoWorker;
import com.example.ex8.work.UpdateUserImageURLWorker;
import com.example.ex8.work.UpdateUserPrettyNameWorker;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private static Bundle currentSavedInstanceState;
    private static String token;
    private static String userName;
    private static String prettyName;
    private static String imageURL;
    private static ArrayList<String> availableImagesList;
    private TextView insertUserNameTextView;
    private EditText insertUserNameEditText;
    private Button userNameButton;
    private TextView insertPrettyNameTextView;
    private EditText insertPrettyNameEditText;
    private Button prettyNameButton;
    private TextView imageURLTextView;
    private Spinner spinnerImageURL;
    private Button imageButton;
    private ImageView userImageImageView;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        getViews();
        setListeners();
        loadAppData();
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("insertUserNameEditText", insertUserNameEditText.getText().toString());
        outState.putString("insertPrettyNameEditText", insertPrettyNameEditText.getText().toString());
        outState.putString("token", token);
        outState.putString("userName", userName);
        outState.putString("prettyName", prettyName);
        outState.putString("imageURL", imageURL);
        outState.putStringArrayList("availableImagesList", availableImagesList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToSharedPreferences();
    }

    private void saveToSharedPreferences(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    private void getViews(){
        insertUserNameTextView = findViewById(R.id.insertUserNameTextView);
        insertUserNameEditText = findViewById(R.id.insertUserNameEditText);
        userNameButton = findViewById(R.id.userNameButton);
        insertPrettyNameTextView = findViewById(R.id.insertPrettyNameTextView);
        insertPrettyNameEditText = findViewById(R.id.insertPrettyNameEditText);
        prettyNameButton = findViewById(R.id.prettyNameButton);
        imageURLTextView = findViewById(R.id.imageURLTextView);
        spinnerImageURL = findViewById(R.id.spinnerImageURL);
        imageButton = findViewById(R.id.imageButton);
        userImageImageView = findViewById(R.id.userImageImageView);
    }

    private void setListeners(){
        userNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (insertUserNameEditText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please insert a valid username!", Toast.LENGTH_LONG).show();
                }
                else{
                    progress = new ProgressDialog(MainActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Wait while loading...");
                    progress.setCancelable(false);
                    progress.show();
                    getImages();
                    getToken();
                    progress.dismiss();
                }
            }
        });
        prettyNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (insertPrettyNameEditText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please insert a valid pretty name!", Toast.LENGTH_LONG).show();
                }
                else{
                    progress = new ProgressDialog(MainActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Wait while loading...");
                    progress.setCancelable(false);
                    progress.show();
                    updatePrettyName();
                    progress.dismiss();
                }
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(MainActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.setCancelable(false);
                progress.show();
                updateImage();
                progress.dismiss();
            }
        });
    }

    private void loadAppData(){
        if (currentSavedInstanceState != null)
        {
            getSavedInstanceState();
        }
        else
        {
            new AsyncDataLoad(this).execute();
        }
    }

    private void getSavedInstanceState()
    {
        insertUserNameEditText.setText(currentSavedInstanceState.getString("insertUserNameEditText"));
        insertPrettyNameEditText.setText(currentSavedInstanceState.getString("insertPrettyNameEditText"));
        token = currentSavedInstanceState.getString("token");
        userName = currentSavedInstanceState.getString("userName");
        prettyName = currentSavedInstanceState.getString("prettyName");
        imageURL = currentSavedInstanceState.getString("imageURL");
        availableImagesList = currentSavedInstanceState.getStringArrayList("availableImagesList");
    }

    private static class AsyncDataLoad extends AsyncTask<Void, Void, Void>
    {
        private WeakReference<MainActivity> mainActivityWeakReference;

        AsyncDataLoad(MainActivity activity)
        {
            mainActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            MainActivity activity = mainActivityWeakReference.get();
            if (activity == null || activity.isFinishing())
            {
                return null;
            }
            final SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            token = sharedPreferences.getString("token", null);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // get a reference to the activity if it is still there
            MainActivity activity = mainActivityWeakReference.get();
            if (activity == null || activity.isFinishing()) return;
            if (token == null){
                activity.askUserToInsertUserName();
            }
            else{
                activity.getUserInfo();
            }
        }
    }

    public void askUserToInsertUserName(){
        insertUserNameTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        insertUserNameEditText.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        userNameButton.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public void getToken(){
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getTokenWork = new OneTimeWorkRequest.Builder(GetTokenWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("username", insertUserNameEditText.getText().toString()).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(getTokenWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String tokenAsJson = info.getOutputData().getString("token");
                if (tokenAsJson == null){
                    return;
                }
                TokenResponse tokenResponse = new Gson().fromJson(tokenAsJson, TokenResponse.class);
                Log.d(TAG, "got token: " + tokenResponse.data);
                token = tokenResponse.data;
                getUserInfo();
            }
        });
    }

    public void getUserInfo(){
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);
        progress.show();
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getUserInfoWork = new OneTimeWorkRequest.Builder(GetUserInfoWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("token", "token " + token).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(getUserInfoWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String userResponseAsJson = info.getOutputData().getString("user");
                if (userResponseAsJson == null){
                    return;
                }
                if (info.getState() == WorkInfo.State.FAILED)
                {
                    System.out.println("5");
                }
                UserResponse userResponse = new Gson().fromJson(userResponseAsJson, UserResponse.class);
                Log.d(TAG, "got user info: " + userResponse.data);
                User user = userResponse.data;
                prettyName = user.pretty_name;
                imageURL = user.image_url;
                getImages();
                hideInsertUserName();
                progress.dismiss();
                showUserInfo();
            }


        });
    }

    public void hideInsertUserName(){
        insertUserNameTextView.setVisibility(View.GONE);
        insertUserNameEditText.setVisibility(View.GONE);
        userNameButton.setVisibility(View.GONE);
    }

    public void showUserInfo(){
        insertPrettyNameTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        insertPrettyNameEditText.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        prettyNameButton.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (prettyName == null || prettyName.isEmpty()){
            insertPrettyNameTextView.setText("Your pretty name is not yet defined, please insert it below and tap the button below to update it!");
        }
        else{
            String textToDisplay = "Hello " + prettyName + "! If you'd like to change your pretty name, please insert a new pretty name below and push the button below to update it!";
            insertPrettyNameTextView.setText(textToDisplay);
        }
        imageURLTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        spinnerImageURL.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        imageButton.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        userImageImageView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        if (imageURL == null || imageURL.isEmpty()){
            imageURLTextView.setText("You didn't select profile image, please select it from the drop down list below and click on the button below to update it!");
        }
        else{
            String x = "Your current profile image is " + imageURL + ". You can change your profile picture by selecting it from drop down list menu below and click on the button below to update it!";
            imageURLTextView.setText(x);
            progress = new ProgressDialog(this);
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading...");
            progress.setCancelable(false);
            progress.show();
            Glide.with(this).load("https://hujipostpc2019.pythonanywhere.com/" + imageURL).into(userImageImageView);
            progress.dismiss();
        }
    }

    public void getImages(){
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getUserInfoWork = new OneTimeWorkRequest.Builder(GetAvailableImagesWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(getUserInfoWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String availableImagesResponseAsJson = info.getOutputData().getString("allImages");
                if (availableImagesResponseAsJson == null){
                    return;
                }
                AvailableImages availableImages = new Gson().fromJson(availableImagesResponseAsJson, AvailableImages.class);
                Log.d(TAG, "got all images: " + availableImages.data);
                availableImagesList = availableImages.data;
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, availableImagesList);
                spinnerImageURL.setAdapter(dataAdapter);
            }
        });
    }

    public void updatePrettyName(){
        UUID workTagUniqueId = UUID.randomUUID();
        OneTimeWorkRequest getUserInfoWork = new OneTimeWorkRequest.Builder(UpdateUserPrettyNameWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("token", "token " + token)
                        .putString("pretty_name", insertPrettyNameEditText.getText().toString()).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(getUserInfoWork);

        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;

                WorkInfo info = workInfos.get(0);

                // now we can use it
                String userResponseAsJson = info.getOutputData().getString("user");
                if (userResponseAsJson == null){
                    return;
                }
                UserResponse userResponse = new Gson().fromJson(userResponseAsJson, UserResponse.class);
                Log.d(TAG, "got user info: " + userResponse.data);
                User user = userResponse.data;
                prettyName = user.pretty_name;
                hideInsertUserName();
                showUserInfo();
            }
        });
    }

    public void updateImage(){
        UUID workTagUniqueId = UUID.randomUUID();
        final OneTimeWorkRequest getUserInfoWork = new OneTimeWorkRequest.Builder(UpdateUserImageURLWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(new Data.Builder().putString("token", "token " + token)
                        .putString("image_url", spinnerImageURL.getSelectedItem().toString()).build())
                .addTag(workTagUniqueId.toString())
                .build();

        WorkManager.getInstance().enqueue(getUserInfoWork);
        WorkManager.getInstance().getWorkInfosByTagLiveData(workTagUniqueId.toString()).observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // we know there will be only 1 work info in this list - the 1 work with that specific tag!
                // there might be some time until this worker is finished to work (in the mean team we will get an empty list
                // so check for that
                if (workInfos == null || workInfos.isEmpty())
                    return;
                WorkInfo info = workInfos.get(0);
                // now we can use it
                String userResponseAsJson = info.getOutputData().getString("user");
                if (userResponseAsJson == null){
                    return;
                }
                UserResponse userResponse = new Gson().fromJson(userResponseAsJson, UserResponse.class);
                Log.d(TAG, "got user info: " + userResponse.data);
                User user = userResponse.data;
                imageURL = user.image_url;
                hideInsertUserName();
                showUserInfo();
            }
        });
    }
}
