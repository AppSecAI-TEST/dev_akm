package com.zongsheng.drink.h17.common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.zongsheng.drink.h17.interfaces.ICallBackProgressListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;


public class UpdateService extends Service {

    private File tempFile = null;
    private boolean cancelUpdate = false;
    private MyHandler myHandler;
    private int download_precent = 0;
    private Reference<ICallBackProgressListener> iCallBackProgressListenerReference;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }


    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iCallBackProgressListenerReference != null) {
            iCallBackProgressListenerReference.clear();
            iCallBackProgressListenerReference = null;
        }
    }

    private void attachs(ICallBackProgressListener iCallBackProgressListener) {
        iCallBackProgressListenerReference = new WeakReference<>(iCallBackProgressListener);
    }


    //下载更新文件
    private void downFile(final String url) {
        new Thread() {
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    // params[0]代表连接的url
                    HttpGet get = new HttpGet(url);
                    HttpResponse response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    long length = entity.getContentLength();
                    InputStream is = entity.getContent();
                    if (is != null) {
                        File rootFile = new File(Environment.getExternalStorageDirectory().getPath(), "/pinke");
                        if (!rootFile.exists() && !rootFile.isDirectory())
                            rootFile.mkdir();

                        tempFile = new File(Environment.getExternalStorageDirectory().getPath(),

                                "/pinke/" + url.substring(url.lastIndexOf("/") + 1));
                        if (tempFile.exists())
                            tempFile.delete();
                        tempFile.createNewFile();

                        //已读出流作为参数创建一个带有缓冲的输出流
                        BufferedInputStream bis = new BufferedInputStream(is);

                        //创建一个新的写入流，讲读取到的图像数据写入到文件中
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        //已写入流作为参数创建一个带有缓冲的写入流
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        int read;
                        long count = 0;
                        int precent = 0;
                        byte[] buffer = new byte[1024];
                        while ((read = bis.read(buffer)) != -1 && !cancelUpdate) {
                            bos.write(buffer, 0, read);
                            count += read;
                            precent = (int) (((double) count / length) * 100);

                            //每下载完成3%就通知任务栏进行修改下载进度
                            if (precent - download_precent >= 3) {
                                download_precent = precent;
                                Message message = myHandler.obtainMessage(3, precent);
                                myHandler.sendMessage(message);
                            }
                        }
                        bos.flush();
                        bos.close();
                        fos.flush();
                        fos.close();
                        is.close();
                        bis.close();
                    }
                    if (!cancelUpdate) {
                        Message message = myHandler.obtainMessage(2, tempFile);
                        myHandler.sendMessage(message);
                    } else {
                        tempFile.delete();
                    }
                } catch (Exception e) {
                    Message message = myHandler.obtainMessage(4, "下载更新文件失败");
                    myHandler.sendMessage(message);
                }
            }
        }.start();
    }

    public class MyBinder extends Binder {
        MyBinder() {
        }

        public UpdateService getService() {
            return UpdateService.this;
        }

        public void attach(ICallBackProgressListener iCallBackProgressListener) {
            attachs(iCallBackProgressListener);
        }

        public void startDownload(String url) {
            myHandler = new MyHandler(Looper.myLooper(), UpdateService.this);
            //初始化下载任务内容views
            Message message = myHandler.obtainMessage(3, 0);
            myHandler.sendMessage(message);
            //启动线程开始执行下载任务
            downFile(url);
        }

    }


    /*事件处理类*/
    private class MyHandler extends Handler {

        MyHandler(Looper looper, Context c) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                switch (msg.what) {
                    case 2:
                        //下载完成后清除所有下载信息，执行安装提示
                        download_precent = 0;
                        // nm.cancel(notificationId);
                        if (iCallBackProgressListenerReference != null && iCallBackProgressListenerReference.get() != null) {
                            iCallBackProgressListenerReference.get().closePopWindow();
                            iCallBackProgressListenerReference.get().downloadSuccess((File) msg.obj);
                        }
                        //Instanll((File) msg.obj, context);
                        //停止掉当前的服务
                        stopSelf();
                        break;
                    case 3:
                        //更新状态栏上的下载进度信息
                        if (iCallBackProgressListenerReference != null && iCallBackProgressListenerReference.get() != null) {
                            L.v(SysConfig.ZPush, "------------>" + download_precent);
                            iCallBackProgressListenerReference.get().callProgress(download_precent);
                        }
                        // nm.notify(notificationId, builder.getNotification());
                        break;
                    case 4:
                        // nm.cancel(notificationId);
                        if (iCallBackProgressListenerReference != null && iCallBackProgressListenerReference.get() != null) {
                            iCallBackProgressListenerReference.get().closePopWindow(msg.obj.toString());
                        }
                        break;
                }
            }
        }
    }

}

