package top.gcszhn.movie.utils;
import java.awt.Desktop;
import java.net.URI;

public class WebBrowerUtils {
    public static void openUrl(String url) {
        openUrl(url, 0);
    }
    public static void openUrl(String url, long delay) {
        if (Desktop.isDesktopSupported()) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(delay);
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception e) {
                        LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
                    }
                }
            }.start();
        } else {
            LogUtils.printMessage("Current platform not support open web browser", LogUtils.Level.ERROR);
        }
    }
}
