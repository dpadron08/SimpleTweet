package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    Context context;
    List<Tweet> tweets;
    TwitterClient client;
    private final int REQUEST_CODE = 40;

    // Pass in context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        this.client = TwitterApp.getRestClient(context);
    }

    // for each row, inflate the layout for the tweet
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }


    // bind values based on the position
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data at position
        Tweet tweet = tweets.get(position);
        // bind the tweet with the viewholder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }


    // define a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvName;
        TextView tvRelativeTimestamp;
        ImageView ivMedia;

        Button btnReply;
        Button btnRetweet;
        Button btnFavorite;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvRelativeTimestamp = itemView.findViewById(R.id.tvRelativeTimestamp);
            ivMedia = itemView.findViewById(R.id.ivMedia);

            btnReply = itemView.findViewById(R.id.btnReply);
            btnRetweet = itemView.findViewById(R.id.btnRetweet);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);

            setMyOnClickListeners(itemView);
        }

        private void setMyOnClickListeners(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ABC", "Clicking tweet itself");
                    onClickTweetAction(view);
                }
            });

            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ABC", "Clicking reply button");
                    onClickReplyAction(view);
                }
            });
            
            btnRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ABC", "Clicking Retweet button");
                    onClickRetweetAction(view);
                }
            });
            
            btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ABC", "Clicking favorite button");
                    onClickFavoriteAction(view);
                }
            });
        }

        private void onClickFavoriteAction(View view) {
            Tweet tweet = tweets.get(getAdapterPosition());
            if (tweet.isFavorited) {
                // unfavorite the tweet

                client.destroyFavorite(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Toast.makeText(context, "Unfavorited!", Toast.LENGTH_SHORT).show();
                        toggleFavoriteSetting(getAdapterPosition());
                        return;
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Toast.makeText(context, "Unfavoriting failed", Toast.LENGTH_SHORT).show();

                        return;

                    }
                });
                return;

            } else {
                // favorite the tweet
                client.createFavorite(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Toast.makeText(context, "Favorited!", Toast.LENGTH_SHORT).show();
                        toggleFavoriteSetting(getAdapterPosition());
                        return;

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Toast.makeText(context, "Favoriting failed", Toast.LENGTH_SHORT).show();
                        return;

                    }
                });

            }
        }

        private void onClickRetweetAction(View view) {
            Tweet tweet = tweets.get(getAdapterPosition());
            if (tweet.isRetweeted) {
                // unfavorite the tweet

                client.destroyRetweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Toast.makeText(context, "Un-retweeted!", Toast.LENGTH_SHORT).show();
                        toggleRetweetSetting(getAdapterPosition());
                        return;
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Toast.makeText(context, "Un-retweet failed", Toast.LENGTH_SHORT).show();

                        return;

                    }
                });
                return;

            } else {
                // favorite the tweet
                client.createRetweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Toast.makeText(context, "Retweeted!", Toast.LENGTH_SHORT).show();
                        toggleRetweetSetting(getAdapterPosition());
                        return;

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Toast.makeText(context, "Retweeting failed", Toast.LENGTH_SHORT).show();
                        return;

                    }
                });

            }
        }

        private void onClickReplyAction(View view) {
            // launch new compose intent and send the username that is going to be replied to
            Intent intent = new Intent(context, ComposeActivity.class);
            Tweet tweet = tweets.get(getAdapterPosition());
            intent.putExtra("replyingToId", tweet.id);
            intent.putExtra("replyingToTweetOwner", tweet.user.screen_name);
            intent.putExtra("tweet", Parcels.wrap(tweet));

            (  (TimelineActivity) context ).onActivityResult(REQUEST_CODE, Activity.RESULT_OK, intent);
            //context.startActivity(intent); // before adding ability to not need to refresh to view reply
        }

        private void onClickTweetAction(View view) {
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);

            String screenName = "@" + tweet.user.screen_name;
            tvScreenName.setText(screenName);
            tvName.setText(tweet.user.name);

            tvRelativeTimestamp.setText(getRelativeTimeAgo(tweet.created_at));
            Glide.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
            Glide.with(context).load(tweet.mediaUrl).into(ivMedia);

        }

        // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
        @RequiresApi(api = Build.VERSION_CODES.N)
        public String getRelativeTimeAgo(String rawJsonDate) {
            String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
            sf.setLenient(true);

            String relativeDate = "";
            try {
                long dateMillis = sf.parse(rawJsonDate).getTime();
                relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            } catch (ParseException | java.text.ParseException e) {
                e.printStackTrace();
            }

            return relativeDate;
        }

    }

    private void toggleFavoriteSetting(int position) {
        tweets.get(position).isFavorited =  !tweets.get(position).isFavorited;
    }

    private void toggleRetweetSetting(int position) {
        tweets.get(position).isRetweeted =  !tweets.get(position).isRetweeted;
    }
}
