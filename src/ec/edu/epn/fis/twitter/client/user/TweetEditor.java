package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Tweet;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.components.controls.Button;
import ec.edu.epn.fis.uil4midp.components.controls.TextBox;
import ec.edu.epn.fis.uil4midp.controllers.NavigableController;
import ec.edu.epn.fis.uil4midp.views.Form;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import javax.microedition.lcdui.TextField;

public class TweetEditor extends Form {

    public static final int EDITOR_NEW = 0;
    public static final int EDITOR_REPLY = 1;
    private TextBox tbTweetInput;
    private Button btnPublish;
    private int editorMode;
    private Tweet tweet;
    private TweetForME midlet;
    private Environment env;
    private String publisherUserName; // Utilizado únicamente cuando se responde un tweet.

    public TweetEditor(TweetForME midlet, int editorMode, Tweet tweet) {
        super(getEditorTitle(editorMode));

        this.editorMode = editorMode;
        this.midlet = midlet;
        this.tweet = tweet;
        this.env = Environment.getInstance();
    }

    private static String getEditorTitle(int editorMode) {
        switch (editorMode) {
            case EDITOR_NEW:
                return "Publicar";
            case EDITOR_REPLY:
                return "Responder";
            default:
                return "Editor";
        }
    }

    public void initialize() {
        btnPublish = new Button("Tweet");

        switch (editorMode) {
            case EDITOR_NEW:
                tbTweetInput = new TextBox("¿Qué está pasando?", 140, TextField.ANY);
                btnPublish.setActionListener(new PublishNewTweetActionListener());
                break;
            case EDITOR_REPLY:
                publisherUserName = "@" + tweet.getUserAccount().getString(MetadataSet.USERACCOUNT_USER_NAME);
                tbTweetInput = new TextBox("Responder a " + publisherUserName, 140 - publisherUserName.length() - 1, TextField.ANY);
                btnPublish.setActionListener(new PublishReplyActionListener());
                break;
        }

        addVisualComponent(tbTweetInput);

        addVisualComponent(btnPublish);
    }

    private class PublishNewTweetActionListener implements ActionListener {

        public void execute() {
            if (tbTweetInput.getText().length() == 0) {
                showDialog(new MessageDialog("Publicar", "El tweet está vacío."));
                return;
            }

            final ProgressDialog pdPublishNew = new ProgressDialog("Publicar", "Publicando tweet...");
            pdPublishNew.setDismissActionListener(new ActionListener() {

                public void execute() {
                    showDialog(new MessageDialog("Publicar", "Publicación exitosa."));
                    tbTweetInput.setText("");

                }
            });

            Thread tPublishNew = new Thread(new Runnable() {

                public void run() {
                    try {
                        env.getTwitter().post(new Tweet(tbTweetInput.getText()));
                        pdPublishNew.close();
                        return;
                    } catch (IOException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                    } catch (LimitExceededException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                    } catch (Exception ex) {
                        showDialog(new MessageDialog("Publicar", "No se puede publicar el nuevo tweet."));
                    }
                    pdPublishNew.close(false);
                }
            });

            showDialog(pdPublishNew);
            tPublishNew.start();
        }
    }

    private class PublishReplyActionListener implements ActionListener {

        public void execute() {
            if (tbTweetInput.getText().length() == 0) {
                showDialog(new MessageDialog("Responder", "La respuesta está vacía."));
                return;
            }

            final ProgressDialog pdPublishResponse = new ProgressDialog("Responder", "Publicando respuesta...");
            pdPublishResponse.setDismissActionListener(new ActionListener() {

                public void execute() {

                    MessageDialog mdPublishSuccess = new MessageDialog("Responder", "Respuesta exitosa.");
                    mdPublishSuccess.setDismissActionListener(new ActionListener() {

                        public void execute() {
                            ((NavigableController) getController()).goToPreviousView();
                        }
                    });

                    showDialog(mdPublishSuccess);
                }
            });

            Thread tReply = new Thread(new Runnable() {

                public void run() {
                    try {
                        env.getTwitter().post(new Tweet(publisherUserName + " " + tbTweetInput.getText()));
                        pdPublishResponse.close();
                        return;
                    } catch (IOException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                    } catch (LimitExceededException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                    } catch (Exception ex) {
                        showDialog(new MessageDialog("Responder", "No se puede publicar el tweet de respuesta."));
                    }
                    pdPublishResponse.close(false);
                }
            });

            showDialog(pdPublishResponse);
            tReply.start();
        }
    }
}
