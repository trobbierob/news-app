package com.example.android.newsapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>{

    private EditText mSearchEditText;
    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private SearchView mSearchView;
    private static final int NEWS_SEARCH_LOADER = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ProgressBar mLoadingIndicator;
    private String webTitle;
    private TextView mEmptyView;
    public String jsonString;
    public URL guardianQueryUrl;
    private ListView listView;
    private ArrayList<HashMap<String, String>> newsList;
    private static final String SEARCH_QUERY = "searchQuery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchEditText = (EditText) findViewById(R.id.editText_search);
        mUrlDisplayTextView = (TextView) findViewById(R.id.url_display_text);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.progress_bar);
        newsList = new ArrayList<>();
        mEmptyView = (TextView) findViewById(R.id.empty);
        listView = (ListView) findViewById(R.id.list);
        getSupportLoaderManager().initLoader(NEWS_SEARCH_LOADER, null, this);
    }

    private void Activate() {

        String userQuery = mSearchEditText.getText().toString();

        if (userQuery.equals(null) || userQuery.equals("")){
            mEmptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);

        } else {
            mEmptyView.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            guardianQueryUrl = NetworkUtils.buildURL(userQuery);
        }

        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY, guardianQueryUrl.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> searchLoader = loaderManager.getLoader(NEWS_SEARCH_LOADER);

        if (searchLoader == null) {
            loaderManager.initLoader(NEWS_SEARCH_LOADER, bundle, MainActivity.this);

        } else {
            loaderManager.restartLoader(NEWS_SEARCH_LOADER, bundle, MainActivity.this);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                if (guardianQueryUrl != null) {
                    try {
                        jsonString = NetworkUtils.getResponseFromHttpUrl(guardianQueryUrl);
                        JSONObject jsonNewsRootObject = new JSONObject(jsonString);
                        Log.v(LOG_TAG, "jsonrootObject is: " + jsonNewsRootObject);
                        JSONObject responseObject = jsonNewsRootObject.optJSONObject("response");
                        Log.v(LOG_TAG, "Response Object is: " + responseObject);
                        JSONArray resultsArray = responseObject.optJSONArray("results");
                        Log.v(LOG_TAG, "Results Array is: " + resultsArray);

                        for (int i = 0; i < resultsArray.length(); i++) {
                            JSONObject jsonObject = resultsArray.getJSONObject(i);
                            Log.v(LOG_TAG, "jsonObject is: " + jsonObject);

                            if (jsonObject.has("webTitle")){
                                webTitle = jsonObject.optString("webTitle");
                            } else {
                                webTitle = getString(R.string.title_missing);
                            }

                            Log.v(LOG_TAG, "webTitle is: " + webTitle);

                            HashMap<String, String> newsArticle = new HashMap<>();

                            newsArticle.put(getString(R.string.webTitle), webTitle);
                            newsList.add(newsArticle);
                        }
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "IOException at " + e);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "JSONException at " + e);
                    }

                } else {
                    Log.e(LOG_TAG, "JSON Server Error");
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (newsList != null) {
            Log.v(LOG_TAG, "newsList is: " + newsList);
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, newsList,
                    R.layout.list_item, new String[]{getString(R.string.webTitle)},
                    new int[]{R.id.webTitle});
            listView.setAdapter(adapter);
        } else {
            listView.setEmptyView(findViewById(R.id.empty));
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemSelected = item.getItemId();
        if (menuItemSelected == R.id.action_bar_search) {
            ConnectivityManager cm = (ConnectivityManager)
                    MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                DisplayURLText();
                newsList.clear();
                Activate();
            } else { // not connected to the internet
                Toast.makeText(getBaseContext(), "Check Connection",
                        Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        Temporary method to display the queried URL
     */
    private void DisplayURLText() {
        String githubQuery = mSearchEditText.getText().toString();
        URL githubSearchUrl = NetworkUtils.buildURL(githubQuery);
        mUrlDisplayTextView.setText(githubSearchUrl.toString());
    }
}