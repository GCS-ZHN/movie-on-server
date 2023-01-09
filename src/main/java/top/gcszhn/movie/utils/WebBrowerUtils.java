package top.gcszhn.movie.utils;
import java.awt.Desktop;
import java.net.URI;

public class WebBrowerUtils {
    public static void openUrl(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
            }
        }
    }
}
