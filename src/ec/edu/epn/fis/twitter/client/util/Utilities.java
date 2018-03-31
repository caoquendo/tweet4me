package ec.edu.epn.fis.twitter.client.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;

public class Utilities {

    public static String formatDate(Date dt) {
        Calendar cl = Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00"));
        cl.setTime(dt);

        StringBuffer sb = new StringBuffer();

        int day = cl.get(Calendar.DAY_OF_MONTH);
        sb.append(formatNumber(day));
        sb.append("/");
        int month = cl.get(Calendar.MONTH) + 1;
        sb.append(formatNumber(month));
        sb.append("/");
        sb.append(cl.get(Calendar.YEAR));
        sb.append(" ");
        int hour = cl.get(Calendar.HOUR_OF_DAY);
        sb.append(formatNumber(hour));
        sb.append(":");
        int minute = cl.get(Calendar.MINUTE);
        sb.append(formatNumber(minute));

        return sb.toString();
    }

    public static String formatNumber(int value) {
        return value < 10 ? "0" + String.valueOf(value) : String.valueOf(value);
    }

    public static Image loadImage(String url) throws IOException {
        HttpConnection conn = null;
        DataInputStream inputStream = null;
        Image img;
        try {
            conn = (HttpConnection) Connector.open(url);
            int length = (int) conn.getLength();
            byte[] data = new byte[length];
            inputStream = new DataInputStream(conn.openInputStream());
            inputStream.readFully(data);
            img = Image.createImage(data, 0, data.length);
        } catch (Exception ex) {
            img = Image.createImage(24,24);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.close();
            }
            System.gc();
        }
        return img;
    }
}
