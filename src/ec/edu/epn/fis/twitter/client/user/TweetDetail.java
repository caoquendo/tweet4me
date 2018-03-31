package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Tweet;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.twitter.client.util.Utilities;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.components.containers.HorizontalSplittedContainer;
import ec.edu.epn.fis.uil4midp.components.controls.Button;
import ec.edu.epn.fis.uil4midp.components.controls.ImageBox;
import ec.edu.epn.fis.uil4midp.components.controls.Label;
import ec.edu.epn.fis.uil4midp.controllers.NavigableController;
import ec.edu.epn.fis.uil4midp.util.FontManager;
import ec.edu.epn.fis.uil4midp.views.ConfirmationDialog;
import ec.edu.epn.fis.uil4midp.views.Dialog;
import ec.edu.epn.fis.uil4midp.views.Form;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import java.util.Date;
import javax.microedition.lcdui.Image;

public class TweetDetail extends Form {

    private TweetForME midlet;
    private Environment env;
    private Tweet tweet;
    private Image image;
    private boolean isMyTweet;
    private String userName;
    private HorizontalSplittedContainer hscUserInfo;
    private ImageBox ibUserPic;
    private Label lblUserName;
    private Label lblTweet;
    private Label lblTweetInfo;
    private Button btnRetweet;
    private Button btnReply;

    public TweetDetail(TweetForME midlet, Tweet tweet, Image image, String userName, boolean isMyTweet) {
        super(userName);

        this.midlet = midlet;
        this.env = Environment.getInstance();
        this.tweet = tweet;
        this.image = image;
        this.isMyTweet = isMyTweet;
        this.userName = userName;

        hscUserInfo = new HorizontalSplittedContainer(50);
    }

    public void initialize() {
        ibUserPic = new ImageBox(image);
        ibUserPic.setMaxWidth(48);
        hscUserInfo.addVisualComponent(ibUserPic);

        lblUserName = new Label(isMyTweet ? "Dije:" : userName + " dijo:");
        hscUserInfo.addVisualComponent(lblUserName);

        addVisualComponent(hscUserInfo);

        lblTweet = new Label(tweet.getString(MetadataSet.TWEET_CONTENT));
        lblTweet.setFont(FontManager.getItalicFont());
        addVisualComponent(lblTweet);

        lblTweetInfo = new Label("Publicado el " +
                Utilities.formatDate(new Date(Long.parseLong(tweet.getString(MetadataSet.TWEET_PUBLISH_DATE)))) +
                " vía " +
                tweet.getString(MetadataSet.TWEET_SOURCE));
        addVisualComponent(lblTweetInfo);

        if (!isMyTweet) {
            btnRetweet = new Button("Retweet");
            btnRetweet.setActionListener(new ReTweetActionListener());
            addVisualComponent(btnRetweet);

            btnReply = new Button("Responder");
            btnReply.setActionListener(new ReplyTweetActionListener());
            addVisualComponent(btnReply);
        }
    }

    private class ReTweetActionListener implements ActionListener {

        public void execute() {
            final ConfirmationDialog cdRetweet = new ConfirmationDialog("¿Retwittear?", userName + ": " + tweet.getString(MetadataSet.TWEET_CONTENT));
            cdRetweet.setDismissActionListener(new ActionListener() {

                public void execute() {
                    if (cdRetweet.getDialogResult() == Dialog.DIALOG_YES) {
                        final ProgressDialog pdRetweet = new ProgressDialog("Retwittear", "Volviendo a publicar tweet...");

                        final MessageDialog mdRetweet = new MessageDialog("Retwittear", "Publicación exitosa.");
                        mdRetweet.setDismissActionListener(new ActionListener() {

                            public void execute() {
                                ((NavigableController) getController()).goToPreviousView();
                            }
                        });

                        Thread t = new Thread(new Runnable() {

                            public void run() {
                                try {
                                    env.getTwitter().repost(tweet);
                                    pdRetweet.close();

                                    showDialog(mdRetweet);
                                    return;
                                } catch (IOException ex) {
                                    showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                                } catch (LimitExceededException ex) {
                                    showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                                } catch (Exception ex) {
                                    showDialog(new MessageDialog("Retwittear", "Imposible volver a publicar tweet."));
                                }
                                pdRetweet.close(false);
                            }
                        });

                        showDialog(pdRetweet);
                        t.start();
                    }
                }
            });

            showDialog(cdRetweet);
        }
    }

    private class ReplyTweetActionListener implements ActionListener {

        public void execute() {
            getController().addView(new TweetEditor(midlet, TweetEditor.EDITOR_REPLY, tweet), null);
        }
    }
}
