package com.example.thunder.videoserverforme;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;


/**
 * Created by thunder on 2016/12/22.
 */

public class VideoServer extends NanoHTTPD {

    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final String TAG = VideoServer.class.getSimpleName();

    private static final String REQUEST_ROOT = "/";

    private String mVideoFilePath;
    private int mVideoWidth  = 0;
    private int mVideoHeight = 0;

    public VideoServer(String filepath,int width,int height,int port) {
        super(DEFAULT_SERVER_PORT);
        mVideoFilePath = filepath;
        mVideoWidth  = width;
        mVideoHeight = height;
    }


    public Response serve(IHTTPSession session){
        Log.d(TAG, "OnRequest: "+session.getUri());
        if (REQUEST_ROOT.equals(session.getUri())){
            return responseRootPage(session);
        }else if (mVideoFilePath.equals(session.getUri())){
            return responseVideoStream(session);
        }
        return response404(session,session.getUri());
    }



    private Response response404(IHTTPSession session, String url) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Sorry, Can't Found "+url + " !");
        builder.append("</body></html>\n");
        return new Response(builder.toString());
    }

    private Response responseVideoStream(IHTTPSession session) {
        try {
            FileInputStream fis = new FileInputStream(mVideoFilePath);
            return new NanoHTTPD.Response(Status.OK, "video/mp4", fis);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return response404(session,mVideoFilePath);
        }
    }

    private Response responseRootPage(IHTTPSession session) {
        File file = new File(mVideoFilePath);
        if(!file.exists()) {
            return response404(session,mVideoFilePath);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("<video ");
        builder.append("width="+getQuotaStr(String.valueOf(mVideoWidth))+" ");
        builder.append("height="+getQuotaStr(String.valueOf(mVideoHeight))+" ");
        builder.append("controls>");
        builder.append("<source src="+getQuotaStr(mVideoFilePath)+" ");
        builder.append("type="+getQuotaStr("video/mp4")+">");
        builder.append("Your browser doestn't support HTML5");
        builder.append("</video>");
        builder.append("</body></html>\n");
        return new Response(builder.toString());
    }
    protected String getQuotaStr(String text) {
        return "\"" + text + "\"";
    }
}
