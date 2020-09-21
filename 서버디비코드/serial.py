import pymysql

def Serial(usr_id):
    usr_id = usr_id.replace("\n","")
    conn = pymysql.connect(host='localhost',user='root',password='1234',db='login',charset='utf8',)

    curs = conn.cursor(pymysql.cursors.DictCursor)

    query = ("select serial from user where id ='%s'" %usr_id)
    curs.execute(query)

    results = curs.fetchall()
    result = results[0]['serial']
    return str(result)


