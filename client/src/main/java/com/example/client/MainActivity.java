package com.example.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.server.AidlCallback;
import com.example.server.Book;
import com.example.server.BookManager;

import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "LYP_Client";
    //由AIDL生成的Java类
    private BookManager mBookManager;
    private AidlCallback callback;
    //标志当前与服务器连接状况的布尔值，false为未连接，true为连接中
    private boolean mBound = false;
    //包含book对象的list
    private List<Book> mBooks;
    private int index;

    /*private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aidl = IMyAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            aidl = null;
        }
    };*/

    private ServiceConnection conn2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBookManager = BookManager.Stub.asInterface(iBinder);
            mBound = true;

            if (mBookManager != null) {
                try {
                    mBooks = mBookManager.getBooks();
                    Log.i(TAG,"onServiceConnected" + mBooks.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            callback = new AidlCallback.Stub() {
                @Override
                public void aidlCallback(final String result) throws RemoteException {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            button4.setText(result);
                        }
                    });
                }
            };

            try {
                mBookManager.registerListener(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    private EditText num1;
    private EditText num2;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button random;
    private TextView text;
    private TextView tt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bindService();
        initView();
    }

    private void initView() {
        num1 = (EditText) findViewById(R.id.num1);
        num2 = (EditText) findViewById(R.id.num2);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        random = (Button) findViewById(R.id.random);
        text = (TextView) findViewById(R.id.text);
        tt = (TextView) findViewById(R.id.testss);

        tt.isFocusable();
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        random.setOnClickListener(this);
    }

    /*private void bindService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.server","com.example.server.MyService"));
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }*/

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button1:
                int num11 = Integer.parseInt(num1.getText().toString());
                int num22 = Integer.parseInt(num2.getText().toString());
                Log.i(TAG,"onClick");
                int res;
                try {
                    res = mBookManager.add(num11, num22);
                    text.setText(num11 + "+" + num22 + "=" + res);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button2:
                if (!mBound) {
                    attemptToBindService();
                    Toast.makeText(this, "当前与服务端处于未连接状态，正在尝试重连，请稍后再试", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mBookManager == null) return;
                Book book = new Book();
                book.setName("APP开发"+index);
                book.setPrice(index*10);
                try {
                    mBookManager.addBook(book);
                    index ++;
                    Log.i(TAG,"ADD" + book.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button3:
                try {
                    Log.i(TAG,"getBook" + mBookManager.getBooks().toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button4:
                try {
                    mBookManager.doInBackGround();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            case R.id.random:
                int sum = 18;
                int calc = 0;
                int temp;
                int s = 0;
                int[] arr = new int[9];
                Random rand = new Random();
                while (s < 10000) {
                    for (int i = 0; i < 9; i++) {
                        temp = rand.nextInt(4);
                        arr[i] = temp * 2 + 1;
                        Log.i(TAG,""+"arr"+ i + "=" + arr[i]);
                        calc = calc + arr[i];
                    }
                    Log.i(TAG,"BBBB" +calc);
                    if((calc == sum)) {
                        for (int j = 0; j <= arr.length; j++) {
                            Log.i(TAG,"这9个数字分别是:" + arr[j]);
                        }
                        break;
                    }
                    s++;
                }

            default:
                break;
        }
    }

    private void attemptToBindService() {
        Intent intent = new Intent();
        intent.setAction("com.example.server");
        intent.setPackage("com.example.server");
        bindService(intent, conn2, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mBound) {
            attemptToBindService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(conn2);
            mBound = false;
            try {
                mBookManager.unRegisterListener(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


}