import pymysql

def Signup(str):
    conn = pymysql.connect(host='localhost',user='root',password='1234',db='login',charset='utf8',)
    str = str.replace("\n","")
    curs = conn.cursor(pymysql.cursors.DictCursor)
    list = str.split('/')

    usr_id = list[0]
    usr_pwd = list[1]
    input_serial = list[2]
    sql = """SELECT serial from user"""
    curs.execute(sql)
    results = curs.fetchall()
    try:
        bool = False
        for sNum in range(len(results)): #시리얼이 등록된건지 for문으로 돌아가면서 확인
            if results[sNum]['serial'] == int(input_serial):
                sql = ("SELECT id FROM user")
                curs.execute(sql)
                idresults = curs.fetchall()
                for idNum in range(len(idresults)): # 사용중인 아이디를 for문으로 확인하는부분
                    if idresults[idNum]['id'] != None:
                        if idresults[idNum]['id'] == usr_id:
                            return "idError"
                sql = ("SELECT id FROM user WHERE serial = %s"%input_serial)
                curs.execute(sql)
                endResults = curs.fetchall() # 이제 내가 내가 쓸 카메라의 시리얼이 이미 회원가입이 된건지 확인
                if endResults[0]['id'] == None: # 등록하고싶은 시리얼에 가입이안되서 id가 none인경우
                    bool = True
                    break
                else:
                    return "idError2"
                    break
        if bool == False:
            raise Exception("no serial")
    except Exception as e:
        return "noSerial"
    else:
        sql = ("UPDATE user SET id = '%s' , pwd = '%s' WHERE serial = %s"%(usr_id,usr_pwd ,input_serial))
        curs.execute(sql.encode('utf8'))
        conn.commit()
        return "good"
    finally:
        conn.close()
