import pymysql

def Login(str1):
    con = pymysql.connect(host='localhost',user='root',password='1234',db='login',charset='utf8') 
    cur = con.cursor()
    str1 = str1.replace("\n","")
    usr2 = str1.split('?')

    usr_id  = usr2[0]
    usr_pwd = usr2[1]
    query = ("select pwd from user where id ='%s'" %usr_id)
    cur.execute(query)

    results = cur.fetchall()
    result = results[0][0]
    if usr_pwd == result:
        return "success"
    else :
        return "fail"
