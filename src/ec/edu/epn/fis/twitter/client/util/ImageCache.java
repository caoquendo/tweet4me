
package ec.edu.epn.fis.twitter.client.util;

import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.lcdui.Image;

public class ImageCache {

    public static Hashtable imageCache = new Hashtable();

    public static Image getImage(String url) {
        if (imageCache.containsKey(url)) {
            System.out.println("Image from cache: " + url);
            return (Image)imageCache.get(url);
        }

        try {
            Image img = Utilities.loadImage(url);
            imageCache.put(url, img);
            System.out.println("Image from web: " + url);
            return img;
        } catch (IOException ex) {
            return null;
        }
    }
}
