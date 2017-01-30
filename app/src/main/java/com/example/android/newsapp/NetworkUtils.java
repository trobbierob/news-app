package com.example.android.newsapp;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    final static String GUARDIAN_URL = "https://content.guardianapis.com/search";
    final static String PARAM_QUERY = "q";

    public static URL buildURL(String guardianSearchQuery) {

        Uri builtUri = Uri.parse(GUARDIAN_URL).buildUpon()
                .appendQueryParameter(PARAM_QUERY, guardianSearchQuery)
                .appendQueryParameter("api-key", "test")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = urlConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter(("\\A"));

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}