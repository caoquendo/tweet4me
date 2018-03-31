package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.UserAccount;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.Tweet;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.twitter.client.util.Utilities;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.views.List;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import java.util.Date;
import javax.microedition.lcdui.Image;

public class TweetsList extends List {

    public static final int MY_TWEETS = 0;
    public static final int THIRD_PARTY_TWEETS = 1;
    private TweetForME midlet;
    private UserAccount userAccount;
    private int listType;
    private Tweet[] tweets;
    private Image accountImage;

    public TweetsList(TweetForME midlet, UserAccount userAccount, Image accountImage, int listType) {
        super(getTitle(listType, userAccount));

        loadDelay = 0;

        this.midlet = midlet;
        this.userAccount = userAccount;
        this.listType = listType;
        this.accountImage = accountImage;
    }

    private static String getTitle(int listType, UserAccount userAccount) {
        switch (listType) {
            case MY_TWEETS:
                return "Mis Tweets";
            case THIRD_PARTY_TWEETS:
                return "Tweets de @" + userAccount.getString(MetadataSet.USERACCOUNT_USER_NAME);
        }
        return "Tweets";
    }

    public void initialize() {
        setLoadActionListener(new LoadTweetsActionListener());
        setItemSelectionActionListener(new ItemSelectionActionListener());
    }

    private class LoadTweetsActionListener implements ActionListener {

        public void execute() {
            final ProgressDialog pdTweetsLoader = new ProgressDialog(getTitle(listType, userAccount), "Cargando...");

            Thread tTweetsLoader = new Thread(new Runnable() {

                public void run() {
                    SearchDevice s = SearchDevice.getInstance();

                    Query q = QueryComposer.from(userAccount.getString(MetadataSet.USERACCOUNT_USER_NAME));
                    q = QueryComposer.append(q, QueryComposer.count(20));

                    try {
                        tweets = s.searchTweets(q);
                        pdTweetsLoader.close();
                        return;
                    } catch (IOException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                    } catch (LimitExceededException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                    } catch (Exception ex) {
                        showDialog(new MessageDialog(getTitle(listType, userAccount), "Error al cargar tweets."));
                    }
                    pdTweetsLoader.close(false);
                }
            });

            pdTweetsLoader.setDismissActionListener(new ActionListener() {

                public void execute() {
                    if (tweets != null) {
                        clearControls();

                        Tweet t;
                        Date dt;
                        StringBuffer sbText;

                        for (int i = 0; i < tweets.length; i++) {
                            t = tweets[i];

                            dt = new Date(Long.parseLong(t.getString(MetadataSet.TWEET_PUBLISH_DATE)));

                            sbText = new StringBuffer();
                            sbText.append(t.getString(MetadataSet.TWEET_CONTENT));
                            if (sbText.length() > 44) {
                                sbText.setLength(44);
                                sbText.append(" [más]");
                            }

                            addListItem(sbText.toString(), accountImage, Utilities.formatDate(dt), false, new Object[]{t, accountImage});
                        }
                    }
                }
            });

            showDialog(pdTweetsLoader);
            tTweetsLoader.start();
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
                    "@" + twt.getString(MetadataSet.TWEET_AUTHOR_USERNAME),
                    listType == MY_TWEETS ? true : false);
            getController().addView(td, img);
        }
    }
}
