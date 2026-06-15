package com.example.myapplication.network;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = "";

    public static synchronized ApiService getApiService(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", "http://10.0.2.2:3000");

        // Format URL correctly
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            serverUrl = "http://" + serverUrl;
        }
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        if (!serverUrl.endsWith("api/")) {
            serverUrl += "api/";
        }

        // Rebuild if retrofit is null or the base URL has changed
        if (retrofit == null || !currentBaseUrl.equals(serverUrl)) {
            currentBaseUrl = serverUrl;

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}
