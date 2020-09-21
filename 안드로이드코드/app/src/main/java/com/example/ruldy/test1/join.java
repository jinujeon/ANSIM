package com.example.ruldy.test1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class join extends AppCompatActivity {

    Button btn, btn_idchk;
    EditText number, id, pwd;
    Intent intent_main;

    private Socket clientSocket;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private int port = 5001;
    private final String ip = "52.79.181.10";
    private MyHandler myHandler;
    private MyThread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        intent_main = new Intent(this, MainActivity.class);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
            clientSocket = new Socket(ip ,port);
            socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e){
            e.printStackTrace();
        }   //서버와 연결시작

        socketOut.println("S"); //서버에게 페이지 알림

        myHandler = new MyHandler();
        myThread = new MyThread();   //서버로부터 데이터를 받는 스레드
        myThread.start();   //스레드 시작

        btn = (Button) findViewById(R.id.btn);
        id = (EditText) findViewById(R.id.id);
        pwd = (EditText) findViewById(R.id.pwd);
        pwd.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
        pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        number = (EditText) findViewById(R.id.number);  //변수설정

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id_s = id.getText().toString();
                String pwd_s = pwd.getText().toString();
                String num_s = number.getText().toString();
                if (id_s.contains("?")){
                    Toast.makeText(join.this, "'?'를 포함한 아이디는 생성 불가합니다", Toast.LENGTH_SHORT).show();
                }else if (id_s.contains("/")){
                    Toast.makeText(join.this, "'/'를 포함한 아이디는 생성 불가합니다", Toast.LENGTH_SHORT).show();
                }else if (id_s.contains("-")){
                    Toast.makeText(join.this, "'-'를 포함한 아이디는 생성 불가합니다", Toast.LENGTH_SHORT).show();
                }else {
                    socketOut.println(id_s + "/" + pwd_s + "/" + num_s); //서버로 메세지 보내기
                }
            }
        });

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
            if (msg.obj.toString().equals("good")) {//받은값이 good일 경우
                Toast.makeText(join.this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();
                finish();   //창 끝내기
            }else if(msg.obj.toString().equals("idError")) {
                Toast.makeText(join.this, "이미 사용중인 아이디입니다.", Toast.LENGTH_SHORT).show();
            }else if(msg.obj.toString().equals("idError2")) {
                Toast.makeText(join.this, "이미 등록된 시리얼넘버입니다.", Toast.LENGTH_SHORT).show();
            }else if(msg.obj.toString().equals("noSerial")) {
                Toast.makeText(join.this, "시리얼넘버를 잘 못 입력하셨습니다..", Toast.LENGTH_SHORT).show();
            }   //잘못된 회원가입 방식일 경우 토스트로 알림
        }
    }
}