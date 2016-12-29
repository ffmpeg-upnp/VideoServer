package database.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import tab.list.AttachParameter;
import tab.list.FileContentProvider.*;

/**
 * Created by thunder on 2016/12/28.
 */

public class MessageProcess {

    public void chcekWlan(ContentResolver content, String aliveIp){
        String tempDate;
        String s_ret = "", s_rep = "", s_d2d = "";
        boolean[] checkType ;
        checkType = AttachParameter.checkState(aliveIp);

        if (checkType[AttachParameter.content] & checkType[AttachParameter.reply]) {
            s_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.indexOf("&reply"));
            s_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6,aliveIp.length() );
        }else if(checkType[AttachParameter.content] & checkType[AttachParameter.d2d]) {
            s_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.indexOf("&d2d"));
            s_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
        }
        else if(checkType[AttachParameter.reply] & checkType[AttachParameter.d2d]) {

            s_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6,aliveIp.indexOf("&d2d") );
            s_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
        }
        else if(checkType[AttachParameter.content] & checkType[AttachParameter.reply] & checkType[AttachParameter.d2d]) {
            s_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.indexOf("&reply"));
            s_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6,aliveIp.indexOf("&d2d") );
            s_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
        }
        else if (checkType[AttachParameter.content]) {
            s_ret = aliveIp.substring(aliveIp.indexOf("&content") + 8, aliveIp.length());
        } else if (checkType[AttachParameter.reply]) {
            s_rep = aliveIp.substring(aliveIp.indexOf("&reply") + 6, aliveIp.length());
        }else if (checkType[AttachParameter.d2d]) {
            s_d2d = aliveIp.substring(aliveIp.indexOf("&d2d") + 4, aliveIp.length());
        }

        if (!s_ret.equals("")) {
            String[] ret = s_ret.split("&content");
            for (int i = 0; i < ret.length; i++) {
                ret[i] = ret[i].replace(i + "=", "");
                //Caused by: java.lang.StringIndexOutOfBoundsException: length=67; regionStart=48; regionLength=-49
                tempDate = ret[i].substring(ret[i].indexOf("d=") + 2, ret[i].lastIndexOf("."));
                ret[i] = ret[i].substring(0, ret[i].indexOf("&d="));
                insertData(content, ret[i], tempDate);
            }
        }
        // 同上
        if (!s_rep.equals("")) {
            String[] rep = s_rep.split("&reply");
            for (int i = 0; i < rep.length; i++) {
                rep[i] = rep[i].replace(i + "=", "");
                tempDate = rep[i].substring(rep[i].indexOf("d=") + 2, rep[i].lastIndexOf("."));
                rep[i] = rep[i].substring(0, rep[i].indexOf("&d="));
                insertData(content, rep[i], tempDate);
            }

        }
        // 同上
        if (!s_d2d.equals("")) {
            String[] d2d = s_d2d.split("&d2d");
            for (int i = 0; i < d2d.length; i++) {
                d2d[i] = d2d[i].replace(i + "=", "");
                tempDate = d2d[i].substring(d2d[i].indexOf("d=") + 2, d2d[i].lastIndexOf("."));
                d2d[i] = d2d[i].substring(0, d2d[i].indexOf("&d="));
                insertData(content, d2d[i], tempDate);
            }
        }
    }

    public void insertData(ContentResolver contentResolver, String data, String tempDate){
        String message[] = data.split("&");// 簡訊body的分割

        if (message.length == 1){

        }else {
            String msg = message[0].replace("m=", "");
            String messageToken = message[1].replace("t=", "");
            String getSender = message[2].replace("u=", "");
            String subject = message[3].replace("c=", "");
            String fileSize= "";
            String fileCount= "";
            String[] form = { UserSchema._ID, UserSchema._SENDER, UserSchema._CONTENT, UserSchema._MESSAGETOKEN, UserSchema._FILESIZE, UserSchema._DATE };
            String[] reply = { UserSchema._ID, UserSchema._SENDER, UserSchema._FILENAME, UserSchema._MESSAGETOKEN, UserSchema._DATE, UserSchema._MSG };

            // 檢查寄件者是否存在,不存在加入到group
            Cursor add_sender_cursor = contentResolver.query(Uri.parse("content://tab.list.d2d/user_group"), null, "sender='" + getSender + "'", null, null);
            if (add_sender_cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(UserSchema._SENDER, getSender);
                contentResolver.insert(Uri.parse("content://tab.list.d2d/user_group"), values);
                values = null;
            }
            add_sender_cursor.close();

            // 檢查SMS狀態是可接收(retrievable)，還是要求上傳(reply)
            if (msg.equals("retrievable")) {
                fileSize = message[4].replace("s=", "");
                fileCount = message[5].replace("f=", "");
                // 檢查簡訊是否已存在DB,若不存在則新增
                Cursor check_cursor = contentResolver.query(Uri.parse("content://tab.list.d2d/user_data"), form, "messagetoken='" + messageToken + "'", null, null);
                check_cursor.moveToFirst();
                if (check_cursor.getCount() == 0) {
                    ContentValues values = new ContentValues();
                    values.put(UserSchema._SENDER, getSender);
                    values.put(UserSchema._MESSAGETOKEN, messageToken);
                    values.put(UserSchema._FILESIZE, fileSize);
                    values.put(UserSchema._TITTLE, subject);
                    values.put(UserSchema._DATE, tempDate);
                    values.put(UserSchema._MSG, msg);
                    values.put(UserSchema._FILECOUNT, fileCount);
                    contentResolver.insert(Uri.parse("content://tab.list.d2d/user_data"), values);
                    values = null;
                }
                check_cursor.close();//產生Notification物件，並設定基本屬性

            }else if (msg.equals("reply")||msg.equals("d2d")){
                String selfId = message[4].replace("i=", "");
                // 檢查此token是否以存在自定義的user_reply 的table，若不存在則新增
                Cursor check_cursor_reply = contentResolver.query(Uri.parse("content://tab.list.d2d/user_reply"), reply, "messagetoken='" + messageToken + "'", null, null);
                if (check_cursor_reply.getCount() == 0){
                    ContentValues values = new ContentValues();
                    values.put(UserSchema._SENDER, getSender);
                    values.put(UserSchema._MESSAGETOKEN, messageToken);
                    values.put(UserSchema._MSG, msg);
                    values.put(UserSchema._FILENAME, subject);
                    values.put(UserSchema._DATE, tempDate);
                    values.put(UserSchema._SELFID, selfId);

                    if (msg.equals("d2d")){
                        values.put(UserSchema._D2D, "1");
                    }
                    contentResolver.insert(Uri.parse("content://tab.list.d2d/user_reply"), values);
                    values = null;
                }
                check_cursor_reply.close();

            }
        }
    }
}
