
import pymysql

def Id(serial):
    serial = serial.replace("\n","")
    conn = pymysql.connect(host='localhost',user='root',password='1234',db='login',charset='utf8')

    curs = conn.cursor(pymysql.cursors.DictCursor)

    query = ("select id from user where serial =%s" %serial)
    curs.execute(query)

    results = curs.fetchall()

    result = results[0]['id']
    return result

