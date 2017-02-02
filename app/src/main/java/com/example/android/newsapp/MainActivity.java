package com.example.android.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private EditText mSearchEditText;
    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private SearchView mSearchView;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ProgressBar mLoadingIndicator;
    private String webTitle;
    private TextView mEmptyView;
    public String jsonString;
    public URL guardianQueryUrl;
    private ListView listView;
    private ArrayList<HashMap<String, String>> newsList;

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
                //new GuardianQueryTask().execute();
                //LoaderManager loaderManager = getLoaderManager();
                //loaderManager.initLoader(0, null, this);
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

    public class GuardianQueryTaskLoader extends AsyncTaskLoader<Void>{

        public GuardianQueryTaskLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {

        }

        @Override
        public Void loadInBackground() {
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
    }

    public class GuardianQueryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            String userQuery = mSearchEditText.getText().toString();

            if (userQuery.equals(null) || userQuery.equals("")){
                mEmptyView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);

            } else {
                mEmptyView.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
                guardianQueryUrl = NetworkUtils.buildURL(userQuery);
            }
        }

        @Override
        protected Void doInBackground(Void... urls) {
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

        @Override
        protected void onPostExecute(Void result) {
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
    }
}