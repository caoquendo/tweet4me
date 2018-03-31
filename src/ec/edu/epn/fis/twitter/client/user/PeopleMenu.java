package ec.edu.epn.fis.twitter.client.user;

import com.twitterapime.rest.FriendshipManager;
import ec.edu.epn.fis.twitter.client.Environment;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.uil4midp.actions.ActionListener;
import ec.edu.epn.fis.uil4midp.views.List;
import java.io.IOException;
import javax.microedition.lcdui.Image;

public class PeopleMenu extends List {

    private TweetForME midlet;
    private Environment env;

    public PeopleMenu(TweetForME midlet) {
        super("Personas");

        this.midlet = midlet;
        this.env = Environment.getInstance();
    }

    public void initialize() {
        try {
            addListItem("Siguiendo", Image.createImage("/ec/edu/epn/fis/twitter/client/resources/following.png"), true, "0");
            addListItem("Seguidores", Image.createImage("/ec/edu/epn/fis/twitter/client/resources/follower.png"), true, "1");
            addListItem("Seguir a alguien", Image.createImage("/ec/edu/epn/fis/twitter/client/resources/addFollower.png"), true, "2");

            setItemSelectionActionListener(new ItemSelectionActionListener());
        } catch (IOException ex) {
        }
    }

    private class ItemSelectionActionListener implements ActionListener {

        public void execute() {
            // Verificar si existe un elemento seleccionado
            if (getSelectedListItem() == null) {
                return;
            }

            final String value = getSelectedListItem().getValue().toString();

            if (value.equals("2")) {
                getController().addView(new PersonAddFollowing(midlet), null);
            } else {
                // Instanciar Friendship Manager
                final FriendshipManager fm = env.getFriendsManager();

                // Determinar el modo de visualización de PeopleViewer
                int viewerMode = value.equals("0") ? PeopleViewer.VIEWER_FOLLOWING : PeopleViewer.VIEWER_FOLLOWER;

                // Añadir un visor de personas al controlador
                getController().addView(new PeopleViewer(midlet, viewerMode), null);
            }
        }
    }
}
