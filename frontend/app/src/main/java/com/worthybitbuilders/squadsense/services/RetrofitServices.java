package com.worthybitbuilders.squadsense.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitServices {
    private static UserService userService = null;
    private static FriendService friendService = null;
    private final static String BASE_URL = "http://192.168.2.26:3000/";

    private static Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

    public static UserService getUserService() {
        if (userService == null) userService = retrofit.create(UserService.class);
        return userService;
    }

    public static FriendService getFriendService() {
        if (friendService == null) friendService = retrofit.create(FriendService.class);
        return friendService;
    }
}
