package com.example.ruldy.test1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
//import org.apache.http.util.ByteArrayBuffer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import org.json.JSONObject;

import static android.service.autofill.Validators.and;

public class RunActivity extends AppCompatActivity {

    ImageView imView;
    String imgUrl = "http://52.79.181.10/"; //읽어올 파일의 주소
    Bitmap bmImg;
    back task;

    String serial, id_data, temp;
    View f, c, chk, fin;

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 5001;
    private final String ip = "52.79.181.10";
    private MyHandler myHandler;
    private MyThread myThread;  //서버 환경 설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        Intent intent_id = getIntent(); //intent해서
        id_data = intent_id.getStringExtra("i_id_run"); //아이디값 받아오기

        f = findViewById(R.id.finish);
        c = findViewById(R.id.contin);
        chk = findViewById(R.id.chk);
        fin = findViewById(R.id.close);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
            clientSocket = new Socket(ip ,port);
            socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e){
            e.printStackTrace();
        }   //서버와 통신 시작

        socketOut.println(id_data); //서버에게 페이지 알림

        myHandler = new MyHandler();
        myThread = new MyThread();   //서버로부터 데이터를 받는 스레드
        myThread.start();   //스레드 시작
    }

    class MyThread extends Thread{
        public void run(){
            while(true){
                try{
                    String data = socketIn.readLine();
                    Message msg = myHandler.obtainMessage();
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class MyHandler extends Handler{
        public void handleMessage(Message msg) {
            serial = msg.obj.toString();
            temp = msg.obj.toString();
            if ((serial != null) && (!serial.equals("start"))){
                notificationManager();
                serial = null;
                fin.setVisibility(View.GONE);
                chk.setVisibility(View.VISIBLE);
            }
        }
    }
    private class back extends AsyncTask<String, Integer,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            // TODO Auto-generated method stub
            try{
                URL myFileUrl = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();

                bmImg = BitmapFactory.decodeStream(is);

            }catch(IOException e){
                e.printStackTrace();
            }
            return bmImg;
        }

        protected void onPostExecute(Bitmap img){
            imView.setImageBitmap(bmImg);
        }

    }   //url 확인 클라스

    public void onClick(View view){
        switch (view.getId()){

            case R.id.close:
                finish();
                break;

            case R.id.chk:
                task = new back(); //여기부터 3줄은 사진읽어오기
                imView = (ImageView) findViewById(R.id.imageView1);
                task.execute(imgUrl+ temp + ".jpg"); //읽어올 파일의 이름
                imView.setVisibility(View.VISIBLE);
                c.setVisibility(View.VISIBLE);
                f.setVisibility(View.VISIBLE);
                break;

            case R.id.contin:
                imView.setVisibility(View.GONE);
                c.setVisibility(View.GONE);
                f.setVisibility(View.GONE);
                fin.setVisibility(View.VISIBLE);
                chk.setVisibility(View.GONE);
                socketOut.println(id_data); //서버에게 페이지 알림
                break;

            case R.id.finish:
                socketOut.println(id_data + "_"); //서버에게 페이지 알림
                finish();
                break;
        }
    }
    public void notificationManager(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, RunActivity.class); //다른 엑티비티로 해보고 저 내용들을 출력해봐
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT); //찾아보고
        Notification.Builder builder = new Notification.Builder(this);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),android.R.drawable.ic_menu_camera));
        builder.setSmallIcon(android.R.drawable.ic_menu_camera);
        builder.setTicker("사진이 등록되었습니다.");
        builder.setContentTitle("사진이 등록되었습니다.");
        builder.setContentText("사진을 확인하십시오!");
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setNumber(999);

        notificationManager.notify(0, builder.build());
    }
}