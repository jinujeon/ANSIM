
import sys, socket
import RPi.GPIO as gpio
import time
import picamera
import subprocess

a = "4141\n"
def echo_client(server_addr):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(server_addr)
    sock.send(a.encode('utf-8'))

    while True:
        gpio.setmode(gpio.BCM)
        data = sock.recv(1024).decode('utf-8')

        if data == (a.replace("\n", "-\n")):
            trig = 13
            echo = 19
            gpio.setup(trig,gpio.OUT)
            gpio.setup(echo,gpio.IN)

            try :
                while True:

                    gpio.output(trig,False)
                    time.sleep(0.5)
                    gpio.output(trig,True)

                    time.sleep(0.00001)
                    gpio.output(trig,False)

                    while gpio.input(echo) == 0:
                       pulse_start=time.time()
                    while gpio.input(echo) == 1:
                       pulse_end = time.time()

                    pulse_duration = pulse_end - pulse_start
                    distance = pulse_duration * 17000
                    distance = round(distance,2)
                    if distance < 100:
                        with picamera.PiCamera() as camera:
                            camera.start_preview()
                            time.sleep(1)
                            camera.capture('/var/www/html/4141.jpg')
                            camera.stop_preview()
                        subprocess.call('scp -i /home/pi/team2/AWS_enoma_pswd.pem /var/www/html/4141.jpg ubuntu@52.79.181.10:/var/www/html',shell = True)
                        print(data)
                        sock.send(data.encode('utf-8'))
                        data = sock.recv(1024).decode('utf-8')
                        if data.decode('utf-8') == a.replace('\n', '-\n'):
                            pass
                        elif data.decode('utf-8') == a.replace('\n','_\n'):
                            break
                    print("Distance : " , distance, "cm")

            except:
                gpio.cleanup()

    sock.close()

if __name__ == '__main__':
    echo_client(('52.79.181.10', 5001))
