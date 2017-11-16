from flask import Flask, request, jsonify, json
from pyfcm import FCMNotification
from pymongo import MongoClient
from bson.objectid import ObjectId
import pymongo
import hashlib
from socialmedia.twitter_term_analysis import TwitterClient, TwitterClientLoc

application = Flask(__name__)
client = MongoClient("ds257495.mlab.com",57495)

db = client["pollr"]
db.authenticate("pollr-server","pollr")

push_service = FCMNotification(api_key="AAAAShAnsbk:APA91bGm7bK9Jlc7pFu38oQ-9w1_q0qQEfiJwrIgg0NS1sV-F47Il8kn6cJWPwUHo4RlSJAGN0nWkCQDMUNOs2xGAGu53dP1x7Ha5YZachCfqB2osU0SK7NVpBI34A1GAVAv8wO9WFFL")

pokedex = [{'number': 14, 'name': 'Kakuna'},
           {'number': 16, 'name': 'Pidgey'},
           {'number': 50, 'name': 'Diglett'}]
def main(term, geocode, num, count):
    json = {}
    json['word'] = term
    api = TwitterClient()
    tweets = api.get_tweets(query = term, count = count)

    json['national'] = {}
    ptweets = [tweet for tweet in tweets if tweet['sentiment'] == 'positive']
    json['national']['pos'] = 100*len(ptweets)/len(tweets)
    ntweets = [tweet for tweet in tweets if tweet['sentiment'] == 'negative']
    json['national']['neg'] = 100*len(ntweets)/len(tweets)

    posids = []
    negids = []
    for tweet in ptweets[:num]:
        posids.append(tweet['id'])
    for tweet in ntweets[:num]:
        negids.append(tweet['id'])
    json['national']['pos_ids'] = posids
    json['national']['neg_ids'] = negids

    apiLoc = TwitterClientLoc()
    tweets = apiLoc.get_tweets(query = term , geocode = geocode, count = count)

    json['local'] = {}
    ptweets = [tweet for tweet in tweets if tweet['sentiment'] == 'positive']
    json['local']['pos'] = 100*len(ptweets)/len(tweets)
    ntweets = [tweet for tweet in tweets if tweet['sentiment'] == 'negative']
    json['local']['neg'] = 100*len(ntweets)/len(tweets)


    posids = []
    negids = []
    for tweet in ptweets[:num]:
        posids.append(tweet['id'])
    for tweet in ntweets[:num]:
        negids.append(tweet['id'])
    json['local']['pos_ids'] = posids
    json['local']['neg_ids'] = negids
    return json

def words(text):
    r = Rake()
    r.extract_keywords_from_text(text)

    results = r.get_ranked_phrases()
    results = results[:int(math.log2(len(results)))]
    final = [];
    for word in results:
        if word in commonwords:
            continue
        for asdf in commonwords:
            if asdf in word.strip():
                continue
        final.append(word)
    return final
@application.route('/api/v1/register', methods=('GET', 'POST'))
def register():
    if request.method == 'POST':
        user_register = request.form.to_dict()
        print(user_register)
        hash_f = hashlib.md5((str(user_register["firebase_id"]) + str(user_register["username"])  + "pollr").encode("utf-8"))
        db.usrs.insert_one({
            "username": user_register["username"],
            "password": user_register["password"],
            "firebase_id": user_register["firebase_id"],
            "hash_f": hash_f.hexdigest(),
            "polls":[]
        })


    return hash_f.hexdigest()
@application.route('/api/v1/login', methods=['POST'])
def login():
    if request.method == 'POST':
        user_login = request.form.to_dict()
        print(user_login)
        user_match = db.usrs.find_one({
            "username" : user_login["username"]
        })
        if(user_match == None):
            return "Fail"

        print(user_match["password"],user_login["password"])
        if(user_match["password"] == user_login["password"]):
            registration_id = user_match["firebase_id"]
            return user_match["hash_f"]

        return "Fail"
@application.route('/api/v1/pregister', methods=('GET', 'POST'))
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
@application.route('/api/v1/plogin', methods=['POST'])
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
            sess_id = hashlib.md5((str(user_login["username"]) + "pollr").encode("utf-8")).hexdigest()
            db.politicians.update({"username" : user_login["username"]},{"$set":{"session_id" : sess_id}})
            return sess_id
        return "Fail"
@application.route('/api/v1/user_profile', methods=['POST'])
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
            "district" : user_info["district"],
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
@application.route('/api/v1/dashboard', methods=['GET'])
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

@application.route('/api/v1/getpoll',methods=['GET'])
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

@application.route('/api/v1/answer',methods=['POST'])
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

@application.route('/api/v1/pquestion', methods=['POST'])
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
            message_body = "Hey " + i["name"] + "! You have a new poll awaiting!," +  str(_id) + "," + user_info["question"] + "," + user_info["type"]
            result = push_service.notify_single_device(registration_id=registration_id, message_title=message_title, message_body=message_body)
        return "success"

@application.route('/api/v1/ppolls', methods=['POST'])
def ppolls():
    if request.method == 'POST':
        user_info = request.form.to_dict()
        rslts = db.politicians.find_one({"session_id": user_info["session_id"]})
        print(user_info["session_id"])
        print(rslts)
        if(rslts == None):
            return "fail"
        print(rslts['polls'])
        return jsonify(rslts['polls'])
@application.route('/api/v1/puserinfo', methods=['POST'])
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
@application.route('/api/v1/ppollinfo',methods=['POST'])
def ppollinfo():
    if(request.method == 'POST'):
        user_info = request.form.to_dict()
        ses_id = user_info["session_id"]
        pol_id = user_info["poll_id"]
        print("pol_id", pol_id)
        print(ses_id)
        # print(db.responses.find_one({"poll_id":ObjectId(str(user_info["poll_id"]))}))
        # # username = db.usrs.find_one({"hash_f":user_info["session_id"]})["username"]
        # p_info = db.polls.find_one({"_id": ObjectId(_id)})
        #politicians demographics
        #politicians filter
        respp_list = []
        respp = db.responses.find({"poll_id":pol_id})
        for i in respp:
            i["_id"] = pol_id
            respp_list.append(i)
        poll_info = db.polls.find_one({"_id":ObjectId(pol_id)})
        print(respp)
        print(poll_info)
        dem = poll_info["demographic"]
        filtr = poll_info["filter"]
        df = {"dem": dem,"filter":filtr,"choices": poll_info["choices"],"question": poll_info["question"],"voting_district":poll_info["voting_district"]}
        # print(usr_d)
        # dems = {"age":usr_d["age"],"gender":usr_d["gender"],"race":usr_d["race"],"district":usr_d["district"],"income":usr_d["income"]}
        rsp = dict()
        rsp["responses"] = respp_list
        # respp["_id"] = pol_id
        rsp["df"] = df
        # respp["demographics"] = dems
        print(rsp)
        return jsonify(rsp)
@application.route('/api/v1/socialmedia',methods=['POST'])
def socialmedia():
    if(request.method == 'POST'):
        user_info = request.form.to_dict()
        ses_id = user_info["session_id"]
        politician = db.politicians.find_one({"session_id": ses_id})
        polls = politician["polls"]
        zp = politician["zipcode"]
        message = ""
        for i in polls:
            post = db.posts.find_one({"_id":ObjectId(i)})["question"]
            message += (post + " ")

        myzip = zipcode.isequal(zp)
        geocode = str(myzip.lat) + "," + str(myzip.lon) + ",100mi"
        text = message
        json = {}
        data = []
        for word in words(text):
            try:
                data.append(main(word, geocode, 1, 500))
            except ZeroDivisionError as e:
                continue
        json['num_words'] = len(data)
        json['words'] = data
        print(json)
    return jsonify(json)
@application.route('/api/v1/user_profile_get',methods=['POST'])
def user_profile_get():
    if(request.method == 'POST'):
        user_info = request.form.to_dict()
        ses_id = user_info['auth_token']
        user = db.usrs.find_one({"hash_f": ses_id})
        if(user is None):
            return "fail"
        print(user)
        responses_cnt = db.responses.count({"username":user["username"]})
        respp = {"name":user["name"],"race":user["race"],"username":user["username"],"gender":user["gender"],"age":user["age"],"district":user["district"],"income":user["income"],"pollResponse":responses_cnt}
        return jsonify(respp)
@application.route('/api/v1/responses',methods=['GET'])
def responses():
    if(request.method == 'GET'):
        ses_id = request.args.get("auth_token")
        user = db.usrs.find_one({"hash_f": ses_id})
        username = user["username"]
        rslts = db.responses.find({"username": username})
        lst = []
        for i in rslts:
            poll_id = i["poll_id"]
            qd = db.polls.find_one({"_id":ObjectId(poll_id)})["question"]
            ans = i["answer"]
            lst.append({"question":qd,"answer":ans})
        return jsonify(lst)

if __name__ == '__main__':

    application.run(host='0.0.0.0',debug=True)
