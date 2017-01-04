package tab.list;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thunder on 2016/12/28.
 */

public class AttachParameter {
    public static String Homeip = "140.138.150.26";
    public static String login_name;
    public static String out_ip ="0.0.0.0";
    public static String in_ip ="0.0.0.0";
    public static String latest_cookie;
    public static String connect_ip,connect_port;
    public static String selfId = "";

    int count, id_this;
    int[] arrDuration, arrSize;
    Bitmap[] thumbnails;
    boolean[] thumbnailsselection;
    String[] arrPath, arrName, Path;
    private Uri uri;

    public static int music = 0;
    public static int video = 1;
    public static int photo = 2;
    public static int filetype = 3;

    public static int content = 0;
    public static int reply = 1;
    public static int d2d = 2;
    public static int msgType = 3;

    public static String sdcardPath = Environment.getExternalStorageDirectory().toString() + File.separator + "KM" + "/";

    public void setcount(int count) {
        this.count = count;
    }
    public void setid(int id_this) {
        this.id_this = id_this;
    }

    public void setarrDuration(int[] arrDuration) {
        this.arrDuration = arrDuration;
    }

    public void setarrSize(int[] arrSize) {
        this.arrSize = arrSize;
    }

    public void setthumbnails(Bitmap[] thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setthumbnailsselection(boolean[] thumbnailsselection) {
        this.thumbnailsselection = thumbnailsselection;
    }

    public void setarrPath(String[] arrPath) {
        this.arrPath = arrPath;
    }

    public void setPath(String[] Path) {
        this.Path = Path;
    }

    public void setarrName(String[] arrName) {
        this.arrName = arrName;
    }

    public void seturi(Uri uri) {
        this.uri = uri;
    }

    public int getcount() {
        return count;
    }

    public int getid() {
        return id_this;
    }

    public Uri geturi() {
        return uri;
    }

    public static boolean[] checkType(String file) {
        //2016/07/04更改
        boolean[] checktype = new boolean[filetype];
        Pattern patternmusic = Pattern.compile(".*.mp3$|.*.wma|.*.m4a|.*.3ga|.*.ogg|.*.wav"); // check
        // file
        // type
        Matcher matchermusic = patternmusic.matcher(file);
        Pattern patternvideo = Pattern.compile(".*.3gp$|.*.mp4|.*.wmv|.*.movie|.*.flv");
        Matcher matchervideo = patternvideo.matcher(file);
        Pattern patternphoto = Pattern.compile(".*.jpg$|.*.bmp|.*.jpeg|.*.gif|.*.png|.*.image");
        Matcher matcherphoto = patternphoto.matcher(file);

        if (matchermusic.find()) {
            checktype[music] = true;
        } else {
            checktype[music] = false;
        }
        if (matchervideo.find()) {
            checktype[video] = true;
        } else {
            checktype[video] = false;
        }
        if (matcherphoto.find()) {
            checktype[photo] = true;
        } else {
            checktype[photo] = false;
        }
        return checktype;
    }

    public static boolean chechsuccess(String respone) {

        boolean result;
        Pattern pattern = Pattern.compile("ret=0.*"); // check file type
        Matcher matcher = pattern.matcher(respone);
        if (matcher.find()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public static boolean[] checkState(String value) {
        boolean[] checktype = new boolean[msgType];
        Pattern cpattern = Pattern.compile("&content.*"); // check file type
        Matcher cmatcher = cpattern.matcher(value);
        Pattern rpattern = Pattern.compile("&reply.*"); // check file type
        Matcher rmatcher = rpattern.matcher(value);
        Pattern ppattern = Pattern.compile("&d2d.*"); // check file type
        Matcher pmatcher = ppattern.matcher(value);
        if (cmatcher.find()) {
            checktype[content] = true;
        } else {
            checktype[content] = false;
        }
        if (rmatcher.find()) {
            checktype[reply] = true;
        } else {
            checktype[reply] = false;
        }
        if (pmatcher.find()) {
            checktype[d2d] = true;
        } else {
            checktype[d2d] = false;
        }
        return checktype;
    }
}
