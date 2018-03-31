package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.UserAccount;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Tweet;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.twitter.client.util.ImageCache;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.components.containers.HorizontalSplittedContainer;
import ec.edu.epn.fis.uil4midp.components.controls.Button;
import ec.edu.epn.fis.uil4midp.components.controls.ImageBox;
import ec.edu.epn.fis.uil4midp.components.controls.Label;
import ec.edu.epn.fis.uil4midp.util.FontManager;
import ec.edu.epn.fis.uil4midp.views.Form;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import javax.microedition.lcdui.Image;

public class MyProfile extends Form {

    private TweetForME midlet;
    private UserAccount account;
    private Environment env;
    private Image profileImage;
    private ImageBox ibUserPicture;
    private Label lblUserName;
    private Label lblLastTweet;
    private Label lblFollowersCount;
    private Label lblFollowingCount;
    private Label lblTweetsCount;
    private Label lblFavoritesCount;
    private Button btnMenciones;
    private Button btnMisTweets;
    private HorizontalSplittedContainer hscUserInfo = new HorizontalSplittedContainer(50);
    private HorizontalSplittedContainer hscPeopleStats = new HorizontalSplittedContainer(-1);
    private HorizontalSplittedContainer hscTweetsStats = new HorizontalSplittedContainer(-1);

    public MyProfile(final TweetForME midlet) {
        super("Mi Perfil");

        this.midlet = midlet;
        this.env = Environment.getInstance();

        lblUserName = new Label();
        ibUserPicture = new ImageBox();
        lblLastTweet = new Label();
        lblFollowersCount = new Label();
        lblFollowingCount = new Label();
        lblTweetsCount = new Label();
        lblFavoritesCount = new Label();
        btnMenciones = new Button("Menciones");
        btnMisTweets = new Button("Mis Tweets");

        setLoadActionListener(new ProfileLoadActionListener());

        setLeftButton("Salir", new QuitActionListener());
    }

    public void initialize() {
        // Label: Bienvenido @usuario
        lblUserName.setFont(FontManager.getBoldFont());
        addVisualComponent(lblUserName);

        // ImageBox: Imagen del Perfil
        ibUserPicture.setMaxWidth(48);
        hscUserInfo.addVisualComponent(ibUserPicture);

        // Label: Tweet más reciente
        lblLastTweet.setFont(FontManager.getItalicFont());
        hscUserInfo.addVisualComponent(lblLastTweet);
        addVisualComponent(hscUserInfo);

        // Labels: Número de Amigos y Seguidores
        hscPeopleStats.addVisualComponent(lblFollowingCount);
        hscPeopleStats.addVisualComponent(lblFollowersCount);
        addVisualComponent(hscPeopleStats);

        // Labels: Número de Tweets y Favoritos
        hscTweetsStats.addVisualComponent(lblTweetsCount);
        hscTweetsStats.addVisualComponent(lblFavoritesCount);
        addVisualComponent(hscTweetsStats);

        // Button: Menciones
        btnMenciones.setActionListener(new ActionListener() {

            public void execute() {
                getController().addView(new TweetsTimeline(midlet, TweetsTimeline.TIMELINE_MENTIONS), null);
            }
        });
        addVisualComponent(btnMenciones);

        // Button: Mis Tweets
        btnMisTweets.setActionListener(new ActionListener() {
            
            public void execute() {
                getController().addView(new TweetsList(midlet, account, profileImage, TweetsList.MY_TWEETS), null);
            }
        });
        addVisualComponent(btnMisTweets);

        // Label: Informativo.
        addVisualComponent(new Label("Si desea actualizar sus datos, ingrese a Twitter desde su computador."));
    }

    private void loadAccount() {
        lblUserName.setCaption("Bienvenido @" + account.getString(MetadataSet.USERACCOUNT_USER_NAME));
        
        profileImage = ImageCache.getImage(account.getString(MetadataSet.USERACCOUNT_PICTURE_URI));
        ibUserPicture.setImage(profileImage);

        Tweet lt = account.getLastTweet();

        lblLastTweet.setCaption(lt.getString(MetadataSet.TWEET_CONTENT));

        lblFollowingCount.setCaption(account.getString(MetadataSet.USERACCOUNT_FRIENDS_COUNT) + " siguiendo.");
        lblFollowersCount.setCaption(account.getString(MetadataSet.USERACCOUNT_FOLLOWERS_COUNT) + " seguidores.");
        lblTweetsCount.setCaption(account.getString(MetadataSet.USERACCOUNT_TWEETS_COUNT) + " tweets.");
        lblFavoritesCount.setCaption(account.getString(MetadataSet.USERACCOUNT_FAVOURITES_COUNT) + " favoritos.");
    }

    private class QuitActionListener implements ActionListener {

        public void execute() {
            final ProgressDialog pdLogout = new ProgressDialog("Tweet 4 ME", "Cerrando Sesión");
            pdLogout.setDismissActionListener(new ActionListener() {

                public void execute() {
                    midlet.destroyApp(true);
                }
            });
            showDialog(pdLogout);

            Thread tLogout = new Thread(new Runnable() {

                public void run() {
                    try {
                        Environment.getInstance().getUserAccountManager().signOut();
                    } catch (IOException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                    } catch (LimitExceededException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                    } catch (Exception ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Error al cerrar la sesión."));
                    }
                    pdLogout.close();
                }
            }, "T4M_LogoutThread");
            tLogout.start();
        }
    }

    private class ProfileLoadActionListener implements ActionListener {

        public void execute() {
            final ProgressDialog pdLoadRefresh = new ProgressDialog("Mi Perfil", "Cargando...");

            Thread tRefresh = new Thread(new Runnable() {

                public void run() {
                    try {
                        account = env.getUserAccountManager().getUserAccount();
                        loadAccount();
                    } catch (IOException ex) {
                    } catch (LimitExceededException ex) {
                    } catch (Exception ex) {
                    }
                    pdLoadRefresh.close();
                }
            }, "T4M_LoadProfileThread");

            tRefresh.start();
            showDialog(pdLoadRefresh);
        }
    }
}
