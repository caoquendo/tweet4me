package ec.edu.epn.fis.twitter.client;

import ec.edu.epn.fis.uil4midp.ui.Window;
import ec.edu.epn.fis.uil4midp.views.SplashScreen;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.*;

public class TweetForME extends MIDlet {

    private Window mainWindow;

    public void startApp() {
        try {
            mainWindow = new MainWindow(this);

            try {
                Image logo = Image.createImage("/ec/edu/epn/fis/twitter/client/resources/T4MLogo.png");
                SplashScreen splash = new SplashScreen("Tweet 4 ME", logo, "Cargando...", true);

                splash.setSplashScreenDelay(3000);

                mainWindow.setSplashScreen(splash, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            mainWindow.show();

        } catch (Exception e) {
            e.printStackTrace();
            destroyApp(true);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }

    public Window getWindow() {
        return mainWindow;
    }
}
