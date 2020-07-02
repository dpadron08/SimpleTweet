package com.codepath.apps.restclienttemplate.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TweetDao {

    @Query("SELECT Tweet.body AS tweet_body, Tweet.created_at AS tweet_created_at, Tweet.id AS tweet_id, " +
            "Tweet.isFavorited as tweet_isFavorited, Tweet.isRetweeted as tweet_isFavorited, User.*" +
            " FROM Tweet INNER JOIN User ON Tweet.userId = User.id ORDER BY Tweet.created_at DESC LIMIT 5")
    List<TweetWithUser> recentItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(Tweet... tweets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertModel(User... users);
}
