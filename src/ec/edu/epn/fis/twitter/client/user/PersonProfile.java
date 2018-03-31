package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.UserAccount;
import com.twitterapime.search.LimitExceededException;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.components.containers.HorizontalSplittedContainer;
import ec.edu.epn.fis.uil4midp.components.controls.Button;
import ec.edu.epn.fis.uil4midp.components.controls.ImageBox;
import ec.edu.epn.fis.uil4midp.components.controls.Label;
import ec.edu.epn.fis.uil4midp.controllers.NavigableController;
import ec.edu.epn.fis.uil4midp.util.FontManager;
import ec.edu.epn.fis.uil4midp.views.ConfirmationDialog;
import ec.edu.epn.fis.uil4midp.views.Form;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import javax.microedition.lcdui.Image;

public class PersonProfile extends Form {

    public static final int PERSON_UNKNOWN = 0;
    public static final int PERSON_FOLLOWER = 1;
    public static final int PERSON_FOLLOWING = 2;
    private TweetForME midlet;
    private Environment env;
    private int personType;
    private UserAccount account;
    private Image image;
    private boolean amIFollowingHim;
    private ImageBox ibUserPicture;
    private Label lblFullName;
    private Label lblPersonalDescription;
    private Label lblLastTweet;
    private Button btnSeguirDejarSeguir;
    private Button btnBlock;
    private Button btnTweets;
    private HorizontalSplittedContainer hscUserInfo;

    public PersonProfile(TweetForME midlet, Object[] personInfo, String userName, int personType) {
        super(userName);

        this.midlet = midlet;
        this.personType = personType;
        this.account = (UserAccount) personInfo[0];
        this.image = (Image) personInfo[1];
        this.amIFollowingHim = ((Boolean) personInfo[2]).booleanValue();
        this.env = Environment.getInstance();
    }

    public void initialize() {
        // Label: Nombre completo
        lblFullName = new Label(account.getString(MetadataSet.USERACCOUNT_NAME));
        lblFullName.setFont(FontManager.getBoldFont());
        addVisualComponent(lblFullName);

        String personalDescription = account.getString(MetadataSet.USERACCOUNT_DESCRIPTION);
        if (personalDescription != null && personalDescription.length() > 0) {
            lblPersonalDescription = new Label(personalDescription);
            addVisualComponent(lblPersonalDescription);
        }

        // ImageBox: Imagen del Perfil
        if (image != null) {
            ibUserPicture = new ImageBox(image);
            ibUserPicture.setMaxWidth(48);

            hscUserInfo = new HorizontalSplittedContainer(50);
            hscUserInfo.addVisualComponent(ibUserPicture);
        }

        // Label: Tweet más reciente.
        lblLastTweet = new Label(account.getLastTweet().getString(MetadataSet.TWEET_CONTENT));
        lblLastTweet.setFont(FontManager.getItalicFont());
        if (hscUserInfo != null) {
            hscUserInfo.addVisualComponent(lblLastTweet);
            addVisualComponent(hscUserInfo);
        } else {
            addVisualComponent(lblLastTweet);
        }

        btnSeguirDejarSeguir = new Button(amIFollowingHim ? "Dejar de seguir" : "Seguir");
        if (amIFollowingHim) {
            btnSeguirDejarSeguir.setActionListener(new UnfollowPersonActionListener());
        } else {
            btnSeguirDejarSeguir.setActionListener(new FollowPersonActionListener());
        }

        addVisualComponent(btnSeguirDejarSeguir);

        if (personType == PERSON_FOLLOWER) {
            btnBlock = new Button("Bloquear");
            btnBlock.setActionListener(new BlockPersonActionListener());
            addVisualComponent(btnBlock);
        }

        //TODO: If personType == PERSON_FOLLOWING... Solo en el caso de los que sigo?
        btnTweets = new Button("Tweets");
        btnTweets.setActionListener(new ActionListener() {

            public void execute() {
                getController().addView(new TweetsList(midlet, account, image, TweetsList.THIRD_PARTY_TWEETS), null);
            }
        });
        addVisualComponent(btnTweets);
    }

    private class BlockPersonActionListener implements ActionListener {

        public void execute() {
            ConfirmationDialog cdBlock = new ConfirmationDialog("Bloquear", "Bloquear a @" + account.getString(MetadataSet.USERACCOUNT_USER_NAME));
            cdBlock.setDismissActionListener(new ActionListener() {

                public void execute() {
                    final ProgressDialog pdBlock = new ProgressDialog("Bloquear", "Espere...");
                    pdBlock.setDismissActionListener(new ActionListener() {

                        public void execute() {
                            showDialog(new MessageDialog("Bloquear", "@" + account.getString(MetadataSet.USERACCOUNT_USER_NAME) + " está bloqueado."));
                            ((NavigableController) getController()).goToStartView();
                        }
                    });

                    Thread tBlock = new Thread(new Runnable() {

                        public void run() {
                            try {
                                env.getFriendsManager().block(account);
                                pdBlock.close();
                                return;
                            } catch (IOException ex) {
                                showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                            } catch (LimitExceededException ex) {
                                showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                            } catch (Exception ex) {
                                showDialog(new MessageDialog("Bloquear", "No se puede procesar solicitud de bloqueo."));
                            }
                            pdBlock.close(false);
                        }
                    });

                    showDialog(pdBlock);
                    tBlock.start();
                }
            });

            showDialog(cdBlock);
        }
    }

    private class FollowPersonActionListener implements ActionListener {

        public void execute() {
            final ProgressDialog pdFollow = new ProgressDialog("Seguir", "Espere un momento...");
            pdFollow.setDismissActionListener(new ActionListener() {

                public void execute() {
                    MessageDialog mdFollow = new MessageDialog("Seguir", "Ahora sigues a @" + account.getString(MetadataSet.USERACCOUNT_USER_NAME));
                    mdFollow.setDismissActionListener(new ActionListener() {

                        public void execute() {
                            ((NavigableController) getController()).goToStartView();
                        }
                    });
                    showDialog(mdFollow);
                }
            });

            Thread t = new Thread(new Runnable() {

                public void run() {
                    try {
                        env.getFriendsManager().follow(account);
                        pdFollow.close();
                        return;
                    } catch (IOException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                    } catch (LimitExceededException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                    } catch (Exception ex) {
                        showDialog(new MessageDialog("Seguir", "Error al procesar la solicitud de seguimiento."));
                    }
                    pdFollow.close(false);
                }
            });

            showDialog(pdFollow);
            t.start();
        }
    }

    private class UnfollowPersonActionListener implements ActionListener {

        public void execute() {
            final ConfirmationDialog cdUnfollow = new ConfirmationDialog("Dejar de seguir", "¿Quieres dejar de seguir a @" + account.getString(MetadataSet.USERACCOUNT_USER_NAME) + "?");
            cdUnfollow.setDismissActionListener(new ActionListener() {

                public void execute() {
                    if (cdUnfollow.getDialogResult() == ConfirmationDialog.DIALOG_NO) {
                        return;
                    }

                    final ProgressDialog pdUnfollow = new ProgressDialog("Dejar de seguir", "Espere un momento...");
                    pdUnfollow.setDismissActionListener(new ActionListener() {

                        public void execute() {
                            MessageDialog mdUnfollow = new MessageDialog("Dejar de seguir", "Ya no sigues a @" + account.getString(MetadataSet.USERACCOUNT_USER_NAME));
                            mdUnfollow.setDismissActionListener(new ActionListener() {

                                public void execute() {
                                    ((NavigableController) getController()).goToStartView();
                                }
                            });
                            showDialog(mdUnfollow);
                        }
                    });

                    Thread t = new Thread(new Runnable() {

                        public void run() {
                            try {
                                env.getFriendsManager().unfollow(account);
                                pdUnfollow.close();
                                return;
                            } catch (IOException ex) {
                                showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                            } catch (LimitExceededException ex) {
                                showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                            } catch (Exception ex) {
                                showDialog(new MessageDialog("Dejar de Seguir", "Falló al procesar la petición."));
                            }
                            pdUnfollow.close(false);
                        }
                    });

                    showDialog(pdUnfollow);
                    t.start();
                }
            });

            showDialog(cdUnfollow);
        }
    }
}
