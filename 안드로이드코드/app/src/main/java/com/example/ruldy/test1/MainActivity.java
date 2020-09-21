package com.example.ruldy.test1;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Button btn;

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 5001;
    private final String ip = "52.79.181.10";
    private MyHandler myHandler;
    private MyThread myThread;  //서버 환경 설정


    EditText editId, editPassword, editPasswordchk;
    String sId, sPw; //여러곳에서 쓰게 전역변수로 설정
    Intent intent_join, intent_sub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editId = (EditText) findViewById(R.id.edit_id); //버튼변수설정
        editPassword = (EditText) findViewById(R.id.edit_password);
        editPassword.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        editPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        Button btnlogin = (Button) findViewById(R.id.btn_login);
        Button btnjoin = (Button) findViewById(R.id.btn_join);  //변수설정마침

        intent_sub = new Intent(this, SubActivity.class);
        intent_join = new Intent(this, join.class); //페이지 넘기는 intent설정

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

        socketOut.println("L"); //서버에게 페이지 알림

        myHandler = new MyHandler();
        myThread = new MyThread();   //서버로부터 데이터를 받는 스레드
        myThread.start();   //스레드 시작

        btnjoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View v)
            {
                startActivity(intent_join);
            }
        }); //회원가입창으로 이동

        btnlogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View v)
            {
                sId = editId.getText().toString();
                sPw = editPassword.getText().toString();
                intent_sub.putExtra("i_id", sId);
                socketOut.println(sId + "?" + sPw);
            }
        }); //로그인하자

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
            if (msg.obj.toString().equals("success")) {//받은값이 true일 경우
                Toast.makeText(MainActivity.this, "로그인되었습니다!", Toast.LENGTH_SHORT).show();
                startActivity(intent_sub);  //sub페이지로 넘어감
            }else if(msg.obj.toString().equals("fail")){
                Toast.makeText(MainActivity.this, "아이디 혹은 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }else if(msg.obj.toString().equals("already")){
                Toast.makeText(MainActivity.this, "이미 접속중인 아이디입니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}