
package ec.edu.epn.fis.twitter.client.user;

import ec.edu.epn.fis.twitter.client.MainWindow;
import ec.edu.epn.fis.twitter.client.TweetForME;
import ec.edu.epn.fis.uil4midp.controllers.NavigableController;
import ec.edu.epn.fis.uil4midp.controllers.TabsController;
import ec.edu.epn.fis.uil4midp.util.ResourceManager;
import java.io.IOException;
import javax.microedition.lcdui.Image;

public class MainController extends TabsController {

    private NavigableController ncHome;
    private NavigableController ncPeople;
    private NavigableController ncShare;
    private NavigableController ncTweetsFeed;

    public MainController(MainWindow mainWindow, TweetForME midlet) throws IOException {
        super(mainWindow);

        // Inicializar controladores navegables
        ncHome = new NavigableController(mainWindow);
        ncPeople = new NavigableController(mainWindow);
        ncShare = new NavigableController(mainWindow);
        ncTweetsFeed = new NavigableController(mainWindow);

        // Añadir vistas a los controladores navegables
        ncHome.addView(new MyProfile(midlet), null);
        ncPeople.addView(new PeopleMenu(midlet), null);
        ncShare.addView(new TweetEditor(midlet, TweetEditor.EDITOR_NEW, null), null);
        ncTweetsFeed.addView(new TweetsTimeline(midlet, TweetsTimeline.TIMELINE_HOME), null);

        // Añadir controladores al controlador principal
        addController(ncHome, ResourceManager.loadImage("/ec/edu/epn/fis/twitter/client/resources/home.png"));
        addController(ncTweetsFeed, ResourceManager.loadImage("/ec/edu/epn/fis/twitter/client/resources/tweet.png"));
        addController(ncShare, ResourceManager.loadImage("/ec/edu/epn/fis/twitter/client/resources/writeTweet.png"));
        addController(ncPeople, ResourceManager.loadImage("/ec/edu/epn/fis/twitter/client/resources/people.png"));
    }
}
