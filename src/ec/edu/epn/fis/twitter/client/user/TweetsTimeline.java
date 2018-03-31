package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.Timeline;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDeviceListener;
import com.twitterapime.search.Tweet;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.twitter.client.util.ImageCache;
import ec.edu.epn.fis.twitter.client.util.Utilities;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.views.List;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Image;

public class TweetsTimeline extends List {

    public static final int TIMELINE_HOME = 0;
    public static final int TIMELINE_MENTIONS = 1;
    private Environment env;
    private TweetForME midlet;
    private Vector tweets;
    private int timelineType;

    public TweetsTimeline(TweetForME midlet, int timelineType) {
        super(getTimelineName(timelineType));

        if (timelineType == TIMELINE_MENTIONS) {
            loadDelay = 0;
        }

        this.midlet = midlet;
        this.env = Environment.getInstance();
        this.timelineType = timelineType;
    }

    private static String getTimelineName(int timelineType) {
        switch (timelineType) {
            case TIMELINE_HOME:
                return "Cronología";
            case TIMELINE_MENTIONS:
                return "Menciones";
        }
        return "Timeline";
    }

    public void initialize() {
        setLoadActionListener(new LoadTimelineActionListener());
        setItemSelectionActionListener(new ItemSelectionActionListener());
    }

    private class LoadTimelineActionListener implements ActionListener {

        public void execute() {
            final ProgressDialog pdTimeline = new ProgressDialog(getTimelineName(timelineType), "Cargando...");

            Thread tTimeline = new Thread(new Runnable() {

                public void run() {
                    Timeline tml = env.getTimeline();
                    Query q = QueryComposer.count(20);

                    tweets = new Vector();

                    TweetLoaderSearchDeviceListener tlsdl = new TweetLoaderSearchDeviceListener(pdTimeline);

                    switch (timelineType) {
                        case TIMELINE_HOME:
                            tml.startGetHomeTweets(q, tlsdl);
                            break;
                        case TIMELINE_MENTIONS:
                            tml.startGetMentions(q, tlsdl);
                            break;
                    }
                }
            });

            pdTimeline.setDismissActionListener(new ActionListener() {

                public void execute() {
                    clearControls();

                    Tweet t;
                    Date dt;
                    StringBuffer sbText;

                    for (int i = 0; i < tweets.size(); i++) {
                        Object[] tweetInfo = (Object[]) tweets.elementAt(i);
                        t = (Tweet) tweetInfo[0];

                        dt = new Date(Long.parseLong(t.getString(MetadataSet.TWEET_PUBLISH_DATE)));

                        sbText = new StringBuffer();
                        sbText.append(t.getString(MetadataSet.TWEET_CONTENT));
                        if (sbText.length() > 44) {
                            sbText.setLength(44);
                            sbText.append(" [más]");
                        }

                        addListItem(sbText.toString(), (Image) tweetInfo[1], Utilities.formatDate(dt), false, tweetInfo);
                    }
                }
            });

            showDialog(pdTimeline);
            tTimeline.start();
        }
    }

    private class ItemSelectionActionListener implements ActionListener {

        public void execute() {
            if (getSelectedListItem() == null) {
                return;
            }

            Object[] tweetInfo = (Object[]) getSelectedListItem().getValue();
            Tweet twt = (Tweet) tweetInfo[0];
            Image img = (Image) tweetInfo[1];

            TweetDetail td = new TweetDetail(midlet,
                    twt,
                    img,
                    "@" + twt.getUserAccount().getString(MetadataSet.USERACCOUNT_USER_NAME),
                    false);
            getController().addView(td, img);
        }
    }

    private class TweetLoaderSearchDeviceListener implements SearchDeviceListener {

        private ProgressDialog loadDialog;

        public TweetLoaderSearchDeviceListener(ProgressDialog loadDialog) {
            this.loadDialog = loadDialog;
        }

        public void tweetFound(Tweet tweet) {
            Image img = ImageCache.getImage(tweet.getUserAccount().getString(MetadataSet.USERACCOUNT_PICTURE_URI));
            tweets.addElement(new Object[]{tweet, img});
        }

        public void searchCompleted() {
            loadDialog.close();
        }

        public void searchFailed(Throwable cause) {
            cause.printStackTrace();
            loadDialog.close();
        }
    }
}
