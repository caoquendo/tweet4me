package ec.edu.epn.fis.twitter.client;

import ec.edu.epn.fis.twitter.client.login.Login;
import ec.edu.epn.fis.uil4midp.controllers.StandaloneController;
import ec.edu.epn.fis.uil4midp.ui.Window;
import ec.edu.epn.fis.uil4midp.views.SplashScreen;
import javax.microedition.lcdui.Image;

public class MainWindow extends Window {

    public MainWindow(TweetForME midlet) throws Exception {
        super(midlet);
        
        StandaloneController controller = new StandaloneController(this);
        setViewController(controller);

        Login loginForm = new Login(midlet);
        controller.addView(loginForm, null);
    }
}
