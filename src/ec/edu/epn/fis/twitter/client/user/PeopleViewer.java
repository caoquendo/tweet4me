package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.FriendshipManager;
import com.twitterapime.rest.UserAccount;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.QueryComposer;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.twitter.client.util.ImageCache;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.views.List;
import ec.edu.epn.fis.uil4midp.views.MessageDialog;
import ec.edu.epn.fis.uil4midp.views.ProgressDialog;
import java.io.IOException;
import javax.microedition.lcdui.Image;

public class PeopleViewer extends List {

    public static final int VIEWER_FOLLOWING = 0;
    public static final int VIEWER_FOLLOWER = 1;
    public static final int VIEWER_SEARCH = 2;
    private TweetForME midlet;
    private Environment env;
    private UserAccount[] people;
    private Image[] peoplePics;
    private boolean[] peopleFollowingStatus;
    private int viewerMode;

    public PeopleViewer(TweetForME midlet, int viewerMode) {
        super(getViewerMode(viewerMode), 0);

        this.viewerMode = viewerMode;
        this.midlet = midlet;
        this.env = Environment.getInstance();
    }

    private static String getViewerMode(int viewerMode) {
        switch (viewerMode) {
            case VIEWER_FOLLOWING:
                return "Siguiendo";
            case VIEWER_FOLLOWER:
                return "Mis Seguidores";
            case VIEWER_SEARCH:
                return "Búsqueda";
            default:
                return "Personas";
        }
    }

    public void initialize() {
        setLoadActionListener(new LoadPeopleViewerActionListener());

        setItemSelectionActionListener(new ItemSelectionActionListener());
    }

    private class LoadPeopleViewerActionListener implements ActionListener {

        public void execute() {
            final ProgressDialog pdFriends = new ProgressDialog("Personas", viewerMode == PeopleViewer.VIEWER_FOLLOWING ? "Cargando personas a quienes sigo..." : "Cargando seguidores...");

            Thread tPeople = new Thread(new Runnable() {

                public void run() {
                    try {
                        FriendshipManager fm = env.getFriendsManager();
                        UserAccountManager uam = env.getUserAccountManager();

                        String[] peopleIDs = viewerMode == PeopleViewer.VIEWER_FOLLOWING ? fm.getFriendsID(QueryComposer.count(25)) : fm.getFollowersID(QueryComposer.count(25));

                        if (peopleIDs != null) {

                            people = new UserAccount[peopleIDs.length];
                            peoplePics = new Image[peopleIDs.length];
                            peopleFollowingStatus = new boolean[peopleIDs.length];

                            for (int i = 0; i < peopleIDs.length; i++) {
                                people[i] = uam.getUserAccount(new UserAccount(peopleIDs[i]));
                                peopleFollowingStatus[i] = fm.isFollowing(people[i]);
                                peoplePics[i] = ImageCache.getImage(people[i].getString(MetadataSet.USERACCOUNT_PICTURE_URI));
                            }
                        }

                        pdFriends.close();
                        return;
                    } catch (IOException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Tiempo de espera de conexión excedido."));
                    } catch (LimitExceededException ex) {
                        showDialog(new MessageDialog("Tweet 4 ME", "Excedidos límites de conexión con Twitter."));
                    } catch (Exception ex) {
                        showDialog(new MessageDialog("Personar", "No se puede cargar lista de personas."));
                    }
                    pdFriends.close(false);
                }
            });

            pdFriends.setDismissActionListener(new ActionListener() {

                public void execute() {
                    clearControls();

                    for (int i = 0; i < people.length; i++) {
                        addListItem("@" + people[i].getString(MetadataSet.USERACCOUNT_USER_NAME),
                                peoplePics[i],
                                people[i].getString(MetadataSet.USERACCOUNT_NAME),
                                false,
                                new Object[]{people[i], peoplePics[i], (peopleFollowingStatus[i] ? Boolean.TRUE : Boolean.FALSE)});
                    }
                }
            });

            showDialog(pdFriends);
            tPeople.start();
        }
    }

    private class ItemSelectionActionListener implements ActionListener {

        public void execute() {
            if (getSelectedListItem() == null) {
                return;
            }

            int personType = viewerMode == VIEWER_SEARCH ? PersonProfile.PERSON_UNKNOWN : (viewerMode == VIEWER_FOLLOWER ? PersonProfile.PERSON_FOLLOWER : PersonProfile.PERSON_FOLLOWING);

            PersonProfile ppf = new PersonProfile(midlet,
                    (Object[]) getSelectedListItem().getValue(),
                    getSelectedListItem().getCaption(),
                    personType);
            getController().addView(ppf, null);
        }
    }
}
