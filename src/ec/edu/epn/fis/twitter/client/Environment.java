package ec.edu.epn.fis.twitter.client;

import com.twitterapime.rest.FriendshipManager;
import com.twitterapime.rest.Timeline;
import com.twitterapime.rest.TweetER;
import com.twitterapime.rest.UserAccount;
import com.twitterapime.rest.UserAccountManager;

public class Environment {

    private static Environment instance;
    
    private TweetER twitter;
    private UserAccountManager userAccountManager;
    private UserAccount account;

    private Environment() {
    }

    public UserAccountManager getUserAccountManager() {
        return userAccountManager;
    }

    public void setUserAccountManager(UserAccountManager aUserAccountManager) {
        userAccountManager = aUserAccountManager;
    }

    public TweetER getTwitter() {
        return twitter;
    }

    public void setTwitter(TweetER aTwitter) {
        twitter = aTwitter;
    }

    public UserAccount getAccount() {
        return account;
    }

    public FriendshipManager getFriendsManager() {
        return FriendshipManager.getInstance(userAccountManager);
    }

    public Timeline getTimeline() {
        return Timeline.getInstance(userAccountManager);
    }

    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }

        return instance;
    }
}
