package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    private static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 140;

    EditText etCompose;
    Button btnTweet;

    TwitterClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_compose); removed for VB
        ActivityComposeBinding binding = ActivityComposeBinding.inflate(getLayoutInflater()); // VB
        View view = binding.getRoot(); // VB
        setContentView(view);

        client = TwitterApp.getRestClient(this);
        // etCompose = findViewById(R.id.etCompose); removed for VB
        etCompose = binding.etCompose; // VB
        // btnTweet = findViewById(R.id.btnTweet); removed for VB
        btnTweet = binding.btnTweet; // VB

        final long replyingToId = getIntent().getLongExtra("replyingToId", 0);
        final String replyingToTweetOwner = getIntent().getStringExtra("replyingToTweetOwner");
        if (replyingToId != 0) {
            String intro = "@" + replyingToTweetOwner;
            etCompose.setText(intro);
        }

        // set a click listener on the button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }

                // make api call to twitter to publish the tweet
                client.publishTweet(tweetContent, replyingToId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says " + tweet.body);
                            Intent intent = new Intent(ComposeActivity.this, TimelineActivity.class);
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent); // set result code and bundle data for response

                            // added so i dont need to refresh to see reply to tweet
                            startActivityForResult(intent, 20);
                            finish(); // closes the activity, pass data to parent
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet");
                    }
                });
            }
        });
        
    }
}