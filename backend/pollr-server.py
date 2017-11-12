from flask import Flask, request, jsonify, json
from pyfcm import FCMNotification
from pymongo import MongoClient
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
            hash_f = hashlib.md5((str(user_match["firebase_id"]) +  str(user_match["username"])+ "pollr").encode("utf-8"))
            registration_id = user_match["firebase_id"]
            message_title = "Update!"
            message_body = "Hey " + user_match["username"] + "! You just logged your ass in!"
            result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
            return hash_f.hexdigest()

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

        return "False"
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
            "gender" : user_info["gender"],
            "age" : user_info["age"],
            "district" : user_info["district"],
            "income" : user_info["income"],
            "race" : user_info["race"],
            "name" : user_info["name"]
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
# @app.route('/api/v1/dashboard', methods=['POST'])
# def dashboard():
#
@app.route('/api/v1/pquestion', methods=['POST'])
def pquestion():
    user_info = request.form.to_dict()
    if request.method == 'POST':
        #db.politicians.find({"session_id": user_info["session_id"]})
        _id = db.polls.insert_one({
            "question": user_info["question"],
            "type": user_info["type"],
            "choices": user_info["choices"],
            "voting_district": user_info["voting_district"],
            "demographic": user_info["demographic"],
            "filter": user_info["filter"]
        })
        print(_id)
        db.politicians.update({"session_id":user_info["session_id"]},{"$push":{"polls":_id}})
        rslts = db.usrs.update({
            "district": user_info["voting_district"],
            str(user_info["demographic"]): user_info["filter"],
        },{"$push":{"polls":_id}})
        

if __name__ == '__main__':

    app.run(host='0.0.0.0',debug=True)
