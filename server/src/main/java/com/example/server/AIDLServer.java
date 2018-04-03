package com.example.server;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyp on 18-3-8.
 */

public class AIDLServer extends Service {

    public final String TAG = "LYP_AIDL_SERVER";
    public final String TAG_CALLABCK = "LYP_CALLBACK";

    private List<Book> mBooks = new ArrayList<>();

    private final BookManager.Stub mBookManager = new BookManager.Stub() {
        @Override
        public List<Book> getBooks() throws RemoteException {
            synchronized (this) {
                Log.e(TAG,"invoking getBooks() method , now the list is : " + mBooks.toString());
                if (mBooks != null) {
                    return mBooks;
                }
                return new ArrayList<>();
            }
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            synchronized (this) {
                if (mBooks == null) {
                    mBooks = new ArrayList<>();
                }
                if (book == null) {
                    Log.i(TAG,"Book is null in");
                    book = new Book();
                }
                //修改book参数，主要是为了观察客户端的反馈
                if (!mBooks.contains(book)) {
                    Log.i(TAG,"Book is adding:" + book.toString());
                    mBooks.add(book);
                    AlertDialog.Builder builder = new AlertDialog.Builder(AIDLServer.this);
                    builder.setMessage("Receive new Message show or not");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("owen", "Yes is clicked");
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("owen", "No is clicked");
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                }
            }
        }

        @Override
        public int add(int num1, int num2) throws RemoteException {
            Log.i(TAG,num1 + num2 + "");
            return num1 + num2;
        }

        @Override
        public void registerListener(AidlCallback callback) throws RemoteException {
            Log.i(TAG_CALLABCK,"callback:" + callback);
            Log.i(TAG_CALLABCK,"remoteCallbackList:" + remoteCallbackList);
            if (remoteCallbackList == null) {
                Toast.makeText(getApplicationContext(),"remoteCallbackList==null", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "mRemoteCallbackList==null");
                return;
            }
            remoteCallbackList.register(callback);
        }

        @Override
        public void unRegisterListener(AidlCallback callback) throws RemoteException {
            remoteCallbackList.unregister(callback);
        }

        @Override
        public void doInBackGround() throws RemoteException {
            AIDLServer.this.doInBackGround();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Book book = new Book();
        book.setName("Android 开发");
        book.setPrice(28);
        mBooks.add(book);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.server.recever");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AIDLServer.this.doInBackGround();
            }
        }, filter);
        //注册一个广播接受者

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, String.format("on bind,intent = %s", intent.toString()));
        return mBookManager;
    }

    private RemoteCallbackList<AidlCallback>  remoteCallbackList = new RemoteCallbackList<>();

    public void doInBackGround() {
        new  Thread() {
            @Override
            public void run() {
                String result = "Hello Client";
                int count = 0;
                Log.i(TAG, "mRemoteCallbackList: " + remoteCallbackList+",mRemoteCallbackList.mCallBack:"+remoteCallbackList);
                count = remoteCallbackList.beginBroadcast();
                if (count == 0) {
                    return;
                }
                try {
                    for (int i = 0; i < count; i++) {
                        remoteCallbackList.getBroadcastItem(i).aidlCallback(result);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    remoteCallbackList.finishBroadcast();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        remoteCallbackList.kill();
        super.onDestroy();
    }
}
