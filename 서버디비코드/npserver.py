import socket
import threading, logging
import login
import signup
import serial
import id
import subprocess

connections = {}
socid = ''


def echo_server(my_port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind(('', my_port))
    sock.listen(100)
    print('Server started')
    while True:
        conn, cli_addr = sock.accept()
        socid = conn.recv(1024).decode('utf-8')
        if socid == "L\n" or socid == "S\n":
            pass
        else:
            connections[socid] = conn
        print('Connected by {}'.format(cli_addr))
        handler = threading.Thread(target=echo_handler, args=(conn, cli_addr))
        handler.daemon = True
        handler.start()
def echo_handler(conn, cli_addr):
    while True:
        try:
            data = conn.recv(1024).decode('utf-8')
            if not data:
                try:
                    for name, value in connections.items():
                        if value == conn:
                            data = name
                    if data in connections:
                        if ":" not in data:
                            x = serial.Serial(data)
                            subprocess.call('sudo rm -r /var/www/html/'+x+'.jpg', shell=True)
                        del connections[data]
                    print('Client closing: {}'.format(cli_addr))
                    break # 연결 끊길때 서버에 사진지우고 끊는 부분
                except:
                    print('Client closing: {}'.format(cli_addr))
                    break # 카메라 연결 끊긴 부분
            print('Received: {}'.format(data))
            if "-" in data:
                chid = data.split("-")
                chid = chid[0] + "\n"
                data = id.Id(chid)
                data = data + "\n"
                connections[data].sendall(chid.encode('utf-8')) # 카메라번호 받아서 아이디 디비
            elif "/" in data:
                sign = signup.Signup(data) + "\n"
                conn.sendall(sign.encode('utf-8')) # 회원가입 디비 들어갈 곳
            elif "?" in data:
                if data.split("?")[0]+"\n" in connections:
                    conn.sendall("already\n".encode('utf-8'))
                elif data.split("?")[0]+":\n" in connections:
                    conn.sendall("already\n".encode('utf-8'))
                else:
                    log = login.Login(data)          # 로그인 디비 들어갈 곳
                    log = log + "\n"
                    conn.sendall(log.encode('utf-8'))
            elif "logout" in data:
                for name, value in connections.items():
                    if value == conn:
                        data = name
                if data in connections:
                    del connections[data]
                data = data.replace(":\n", "\n")
                if data in connections:
                    del connections[data] # logout시 딕셔너리에서 카메라 소켓 제거
            else:
                if "_" not in data:
                    data = serial.Serial(data)
                    data1 = data + "\n"
                    data2 = data + "-\n"
                    if data1 in connections:
                        connections[data1].sendall(data2.encode('utf-8')) # 아이디 받아서 카메라번호 디비
                        conn.sendall("start\n".encode('utf-8'))
                    else:
                        conn.sendall("finish\n".encode('utf-8'))
                elif "_" in data:
                    data = data.replace('_\n', '\n')
                    data = serial.Serial(data)
                    subprocess.call('sudo rm -r /var/www/html/'+data+'.jpg', shell=True)
                    data1 = data + "\n"
                    data2 = data + "_\n"
                    connections[data1].sendall(data2.encode('utf-8')) # 종료 눌렀을시 대기상태

        except socket.error as e:
            print('socket error: {}'.format(e))
            break
    conn.close()


if __name__ == '__main__':
    echo_server(5001)


