package com.worthybitbuilders.squadsense.viewmodels;


import android.util.Patterns;

import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.worthybitbuilders.squadsense.models.ErrorResponse;
import com.worthybitbuilders.squadsense.models.UserModel;
import com.worthybitbuilders.squadsense.services.RetrofitServices;
import com.worthybitbuilders.squadsense.services.UserService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserViewModel extends ViewModel {
    private final UserService userService = RetrofitServices.getUserService();

    public UserViewModel() {}

    public boolean IsValidEmail(String email)
    {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void getUserByEmail (String email, UserCallback callback) {
        Call<UserModel> result = userService.getUserByEmail(email);
        result.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if (response.isSuccessful()) {
                    UserModel user = response.body();
                    callback.onSuccess(user);
                }
                else {
                    ErrorResponse err = null;
                    try {
                        err = new Gson().fromJson(response.errorBody().string(), ErrorResponse.class);
                    } catch (IOException e) {
                        callback.onFailure("Something has gone wrong!");
                    }
                    callback.onFailure(err.getMessage());
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }




    public interface UserCallback {
        public void onSuccess(UserModel user);
        public void onFailure(String message);
    }
}
