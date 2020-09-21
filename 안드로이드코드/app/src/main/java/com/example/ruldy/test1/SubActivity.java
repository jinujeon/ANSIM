package com.example.ruldy.test1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SubActivity extends AppCompatActivity {

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 5001;
    private final String ip = "52.79.181.10";
    private MyHandler myHandler;
    private MyThread myThread;  //서버환경설정

    Intent intent_run;
    String id_data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        Intent intent_id = getIntent(); //intent해서
        id_data = intent_id.getStringExtra("i_id"); //아이디값 받아오기
        intent_run = new Intent(this, RunActivity.class);   //run페이지로 넘김
        intent_run.putExtra("i_id_run", id_data);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
            clientSocket = new Socket(ip ,port);
            socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e){
            e.printStackTrace();
        }   //서버와 통신시작

        socketOut.println(id_data+":"); //서버에게 페이지 알림

        myHandler = new MyHandler();
        myThread = new MyThread();  //서버의 값을 받는 스레드 설정
        myThread.start();   //스레드 시작

        Button btn_run = (Button) findViewById(R.id.button_run);
        Button btn_logout = (Button) findViewById(R.id.button_logout);

        btn_run.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View v)
            {
                socketOut.println(id_data); //
            }
        }); //실행창으로 넘어감

        btn_logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View v)
            {
                socketOut.println("logout"); //
                finish();
            }
        }); //실행창으로 넘어감

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
        public void handleMessage(Message msg){
            if (msg.obj.toString().equals("start")) {//받은값이 true일 경우
                Toast.makeText(SubActivity.this, "카메라를 실행합니다!", Toast.LENGTH_SHORT).show();
                startActivity(intent_run);  //sub페이지로 넘어감
            }else if(msg.obj.toString().equals("finish")) {
                Toast.makeText(SubActivity.this, "카메라의 연결상태를 확인하세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}