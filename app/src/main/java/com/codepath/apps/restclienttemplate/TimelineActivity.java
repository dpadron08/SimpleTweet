package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {


    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    // for refreshing
    private SwipeRefreshLayout swipeContainer;

    // for the progress loading action item
    MenuItem miActionProgressItem;

    TwitterClient client;

    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_timeline); removed for VB
        ActivityTimelineBinding binding = ActivityTimelineBinding.inflate(getLayoutInflater()); // VB
        View view = binding.getRoot(); // VB
        setContentView(view); // VB

        client = TwitterApp.getRestClient(this);


        // Lookup the swipe container view
        //swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer); // removed for VB
        swipeContainer = binding.swipeContainer; // VB

        // find the recycler view
        //rvTweets = findViewById(R.id.rvTweets); // removed for VB
        rvTweets = binding.rvTweets;
        // initialize the list of tweets and adapter

        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // recycler view setup: layout manager and adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        populateHomeTimeline();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore " + page);
                loadMoreData();
            }
        };
        // Add the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

    }

    private void loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(tweets.get(tweets.size() - 1).id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "succeeded getting more data!" );
                // 2. Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    // 3. Append the new data objects to the existing sets of items inside the array of items
                    // 4. Notify the adapter of the new items made with 'notifyItemRangeInserted'
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "failed to get more data");
            }
        });

    }

    public void fetchTimelineAsync(int page) {
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // Remember to CLEAR OUT old items before appending in the new ones
                adapter.clear();

                Log.i(TAG, "success!" + json.toString());
                JSONArray jsonArray = json.jsonArray;

                try {
                    // ...the data has come back, add new items to your adapter...
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    if (miActionProgressItem != null) {
                        hideProgressBar();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                    e.printStackTrace();
                    if (miActionProgressItem != null) {
                        hideProgressBar();
                    }
                }

                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error ");
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
            }
            /*
            public void onSuccess(JSONArray json) {
                // Remember to CLEAR OUT old items before appending in the new ones
                adapter.clear();

                // ...the data has come back, add new items to your adapter...
                //adapter.addAll(...);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            public void onFailure(Throwable e) {
                Log.d("DEBUG", "Fetch timeline error: " + e.toString());
            }

             */
        });
    }

    // for toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
        //return super.onCreateOptionsMenu(menu);
    }

    // for toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            // compose icon has been selected
            //Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
            // navigate to a new compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            intent.putExtra("replyingToId", 0L);
            intent.putExtra("replyingToTweetOwner", "empty");
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // for progress bar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        Log.i(TAG, "Get here?");

        if (miActionProgressItem != null) {
            showProgressBar();
        }
        // return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    // for getting data back to parent activity after child activity finished
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // get data from intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // update the recycler view with new tweet
            // Modify data source of the tweets
            tweets.add(0, tweet);
            // update the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        } else if (requestCode == 40 && resultCode == RESULT_OK) {
            /*
                Added for ability to not need to refresh upon sending a reply
             */
            Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            intent.putExtra("replyingToId", tweet.id);
            intent.putExtra("replyingToTweetOwner", tweet.user.screen_name);
            startActivity(intent);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        if (miActionProgressItem != null) {
            showProgressBar();
        }
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "success!" + json.toString());
                JSONArray jsonArray = json.jsonArray;

                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                    if (miActionProgressItem != null) {
                        hideProgressBar();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                    e.printStackTrace();
                    if (miActionProgressItem != null) {
                        hideProgressBar();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "failure! Response: " + response, throwable);
                if (miActionProgressItem != null) {
                    hideProgressBar();
                }
            }
        });
    }

    // making the progress bar visible and invisible
    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

}