from flask import Flask, request, jsonify, json
from pyfcm import FCMNotification
from pymongo import MongoClient
from bson.objectid import ObjectId
import pymongo
import hashlib

app = Flask(__name__)
client = MongoClient("ds255265.mlab.com",55265)

db = client["pollr"]
db.authenticate("pollr-server","pollr")

push_service = FCMNotification(api_key="AAAAShAnsbk:APA91bGm7bK9Jlc7pFu38oQ-9w1_q0qQEfiJwrIgg0NS1sV-F47Il8kn6cJWPwUHo4RlSJAGN0nWkCQDMUNOs2xGAGu53dP1x7Ha5YZachCfqB2osU0SK7NVpBI34A1GAVAv8wO9WFFL")

pokedex = [{'number': 14, 'name': 'Kakuna'},
           {'number': 16, 'name': 'Pidgey'},
           {'number': 50, 'name': 'Diglett'}]

@app.route('/api/v1/register', methods=('GET', 'POST'))
def register():
    if request.method == 'POST':
        # print(request.form["username"])
        # print(type(request.form))
        # print(request.form.to_dict())
        user_register = request.form.to_dict()
        print(user_register)
        # hash_p = hashlib.md5(str(user_register["password"]).encode("utf-8"))
        hash_f = hashlib.md5((str(user_register["firebase_id"]) + str(user_register["username"])  + "pollr").encode("utf-8"))
        # hashlib.md5(d.encode("utf-8"))
        db.usrs.insert_one({
            "username": user_register["username"],
            "password": user_register["password"],
            "firebase_id": user_register["firebase_id"],
            "hash_f": hash_f.hexdigest(),
            "polls":[]
        })
        # print(json.dumps(request.data))

        # print(jsonify(str(request.data.decode('utf-8'))).replace("\"",""))
        # return request.form["username"]

    return hash_f.hexdigest()
@app.route('/api/v1/login', methods=['POST'])
def login():
    if request.method == 'POST':
        # print(request.form["username"])
        # print(type(request.form))
        # print(request.form.to_dict())
        user_login = request.form.to_dict()
        print(user_login)
        # hash_p = hashlib.md5(str(user_login["password"]).encode("utf-8"))
        user_match = db.usrs.find_one({
            "username" : user_login["username"]
        })
        if(user_match == None):
            return "Fail"

        print(user_match["password"],user_login["password"])
        if(user_match["password"] == user_login["password"]):
            # hash_f = hashlib.md5((str(user_match["firebase_id"]) +  str(user_match["username"])+ "pollr").encode("utf-8"))
            registration_id = user_match["firebase_id"]
            message_title = "Update!"
            message_body = "Hey " + user_match["username"] + "! You just logged your ass in!"
            result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
            return user_match["hash_f"]

        return "Fail"
@app.route('/api/v1/pregister', methods=('GET', 'POST'))
def pregister():
    if request.method == 'POST':
        # print(request.form["username"])
        # print(type(request.form))
        # print(request.form.to_dict())
        user_register = request.form.to_dict()
        print(user_register)
        # hash_p = hashlib.md5(str(user_register["password"]).encode("utf-8"))
        hash_f = hashlib.md5((str(user_register["username"]) + "pollr").encode("utf-8"))
        # hashlib.md5(d.encode("utf-8"))
        db.politicians.insert_one({
            "username": user_register["username"],
            "password": user_register["password"],
            "name": user_register["name"],
            "zip": user_register["zip"],
            "polls":[],
            "password": user_register["password"],
            "email": user_register["email"],
            "session_id": ""
            # "firebase_id": user_register["firebase_id"],
            # "hash_f": hash_f.hexdigest()
        })
        # print(json.dumps(request.data))

        # print(jsonify(str(request.data.decode('utf-8'))).replace("\"",""))
        # return request.form["username"]
    return "True"
    # return hash_f.hexdigest()
@app.route('/api/v1/plogin', methods=['POST'])
def plogin():
    if request.method == 'POST':
        # print(request.form["username"])
        # print(type(request.form))
        # print(request.form.to_dict())
        user_login = request.form.to_dict()
        print(user_login)
        # hash_p = hashlib.md5(str(user_login["password"]).encode("utf-8"))
        user_match = db.politicians.find_one({
            "username" : user_login["username"]
        })
        if(user_match == None):
            return "Fail"
        print(user_match["password"],user_login["password"])
        if(user_match["password"] == user_login["password"]):
            # hash_f = hashlib.md5((str(user_match["firebase_id"]) + "pollr").encode("utf-8"))
            # registration_id = user_match["firebase_id"]
            # message_title = "Update!"
            # message_body = "Hey " + user_match["username"] + "! You just logged your ass in!"
            # result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
            sess_id = hashlib.md5((str(user_login["username"]) + "pollr").encode("utf-8")).hexdigest()
            db.politicians.update({"username" : user_login["username"]},{"$set":{"session_id" : sess_id}})
            return sess_id

        return "Fail"
@app.route('/api/v1/user_profile', methods=['POST'])
def user_profile():

    if request.method == 'POST':
        user_info = request.form.to_dict()
        user_match = db.usrs.find_one({
            "hash_f": user_info["auth_token"]
        })
        if(user_match["hash_f"] != user_info["auth_token"]):
            return "auth failed"
        username = user_match["username"]
        db.usrs.update({"username":username},{"$set":{
            "gender" : user_info["gender"].lower(),
            "age" : user_info["age"].lower(),
            "district" : user_info["district"].lower(),
            "income" : user_info["income"].lower(),
            "race" : user_info["race"].lower(),
            "name" : user_info["name"].lower()
        }})
        return "Success!"
#

        # hashlib.md5(d.encode("utf-8"))
        # db.usrs.insert_one({
        #     "username": user_register["username"],
        #     "password": hash_p.hexdigest(),
        #     "firebase_id": user_register["firebase_id"],
        #     "hash_f": hash_f.hexdigest()
        # })
        # print(json.dumps(request.data))

        # print(jsonify(str(request.data.decode('utf-8'))).replace("\"",""))
        # return request.form["username"]
@app.route('/api/v1/dashboard', methods=['GET'])
def dashboard():
    if request.method == 'GET':
        sess_token = request.args.get("auth_token")
        ids = db.usrs.find_one({"hash_f": sess_token})["polls"]
        print("dashboard", ids)
        rese = []
        for i in ids:
            pl = db.polls.find_one({"_id": i})
            rese.append({"question":pl["question"], "id": str(i), "type": pl["type"]})
    return jsonify(rese)
@app.route('/api/v1/getpoll',methods=['GET'])
def getpoll():
    if request.method == 'GET':
        sess_token = request.args.get("auth_token")
        _id = request.args.get("poll_id")
        print(_id)
        pl = db.polls.find_one({"_id": ObjectId(_id)})
        # return jsonify(pl)
        pl["_id"] = str(ObjectId(_id))
        return str(pl)
    return "false"

@app.route('/api/v1/answer',methods=['POST'])
def answer():
    if request.method == 'POST':
        user_info = request.form.to_dict()
        _id = user_info["poll_id"]
        answer = user_info["answer"]
        username = db.usrs.find_one({"hash_f":user_info["auth_token"]})["username"]
        db.responses.insert_one({
            "username": username,
            "answer": answer,
            "poll_id": str(_id)
        })
        print(db.usrs.find_one({"username":username})["polls"])
        db.usrs.update({"username": username},{"$pull": {"polls":ObjectId(user_info["poll_id"])}})
        print(db.usrs.find_one({"username":username})["polls"])
        return "Success!"



@app.route('/api/v1/pquestion', methods=['POST'])
def pquestion():
    user_info = request.form.to_dict()
    if request.method == 'POST':
        #db.politicians.find({"session_id": user_info["session_id"]})
        dd = db.polls.insert_one({
            "question": user_info["question"],
            "type": user_info["type"],
            "choices": user_info["choices"],
            "voting_district": user_info["district"],
            "demographic": user_info["demographic"],
            "filter": user_info["filter"]
        })
        _id = dd.inserted_id
        print("hellooooo")
        print(str(_id))
        db.politicians.update({"session_id":user_info["session_id"]},{"$push":{"polls":str(_id)}})
        rslts = db.usrs.update({
            "district": user_info["district"],
            str(user_info["demographic"]): user_info["filter"],
        },{"$push":{"polls":_id}})
        for i in db.usrs.find({"polls":_id}):
            notif = i["firebase_id"]
            registration_id = notif
            message_title = "Update!"
            message_body = "Hey " + i["name"] + "! You have a new poll awaitting!," +  str(_id)
            result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
        return "success"

@app.route('/api/v1/ppolls', methods=['POST'])
def ppolls():
    if request.method == 'POST':
        user_info = request.form.to_dict()
        rslts = db.politicians.find_one({"session_id": user_info["session_id"]})
        if(rslts == None):
            return "fail"
        print(rslts['polls'])
        return jsonify(rslts['polls'])
@app.route('/api/v1/puserinfo', methods=['POST'])
def puserinfo():
    if request.method == 'POST':
        user_info = request.form.to_dict()
        rslt = db.politicians.find_one({"session_id": user_info["session_id"]})
        # p = rslt["polls"]
        # for i in range(0,len(p)):
        #     p[i] = str(p[i])
        for p in range(0,len(rslt["polls"])):
            rslt["polls"][p] = str(ObjectId(rslt["polls"][p])) + ""
            print(str(ObjectId(rslt["polls"][p])))

        if(rslt is not None):
            return jsonify(rslt)
        return "fail"
@app.route('/api/v1/ppollinfo',methods=['POST'])
def ppollinfo():
    if(request.method == 'POST'):
        user_info = request.form.to_dict()
        ses_id = user_info["session_id"]
        pol_id = user_info["poll_id"]
        print(pol_id)
        print(ses_id)
        # print(db.responses.find_one({"poll_id":ObjectId(str(user_info["poll_id"]))}))
        # # username = db.usrs.find_one({"hash_f":user_info["session_id"]})["username"]
        # p_info = db.polls.find_one({"_id": ObjectId(_id)})

        respp = db.responses.find_one({"poll_id":user_info["poll_id"]})
        print(respp)
        respp["_id"] = pol_id
        return jsonify(respp)

if __name__ == '__main__':

    app.run(host='0.0.0.0',debug=True)
