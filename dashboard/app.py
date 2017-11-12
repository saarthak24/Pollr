from flask import Flask, render_template, request, make_response, redirect, url_for
import requests
import sys
import hashlib

app = Flask(__name__)


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/dashboard", methods=["POST", "GET"])
def my_form_post():
    if request.method == "GET":
        return render_template("dashboard.html")
    username = request.form['username']
    password = request.form['password']
    data = {
        'username': username,
        'password': hashlib.md5((password + "pollr").encode("utf-8")).hexdigest()
    }
    r = requests.post('http://10.199.25.174:5000/api/v1/plogin', data=data)
    print(r.text, file=sys.stderr)

    if r.text != "Fail":
        resp = make_response(render_template("dashboard.html", session=r.text))
        resp.set_cookie('username', username)
        resp.set_cookie('session_id', r.text)
        return resp
    return render_template("index.html")


@app.route("/register", methods=["POST"])
def my_form_register():
    name = request.form['fullName']
    email = request.form['email']
    username = request.form['username']
    password = request.form['password']
    zipCode = request.form['zip']
    data = {
        'name': name,
        'email': email,
        'username': username,
        'password': hashlib.md5((password + "pollr").encode("utf-8")).hexdigest(),
        'zip': zipCode
    }
    r = requests.post('http://10.199.25.174:5000/api/v1/pregister', data=data)
    return render_template("dashboard.html")


@app.route("/createpoll")
def createpoll():

    session_id = request.cookies.get('session_id')

    data = {
        'session_id': session_id,
        'question': 'What is love?',
        'type': 'mc',
        'choices': {
                    'a': 'fear',
                    'b': 'hate',
                    'c': 'shrek',
                    'd': 'bart'
        },
        'district': 'VA03',
        'demographic': 'race',
        'filter': 'white'
    }

    r = requests.post('http://10.199.25.174:5000/api/v1/pquestion', data=data)
    return render_template("createpoll.html")


@app.route("/ppolls", methods=["POST"])
def ppolls():
    session = request.cookies.get('session_id')
    data = {
        'session_id': session
    }
    r = requests.post('http://10.199.25.174:5000/api/v1/ppolls', data=data)
    return r.text


@app.route("/ppollinfo", methods=["GET"])
def ppollinfo():
	session = request.cookies.get('session_id')
	poll_id = request.args.get('poll_id')
	data = {
        'poll_id': poll_id,
		'session_id': session
    }
	r = requests.post('http://10.199.25.174:5000/api/v1/ppollinfo', data=data)
	return r.text


@app.route("/user")
def user():
    username = request.cookies.get('username')
    session_id = request.cookies.get('session_id')
    data = {
        'username': username,
        'session_id': session_id
    }
    # r = requests.post()
    return render_template("user.html")


@app.route("/logout")
def logout():
    return render_template("index.html")


if __name__ == "__main__":
    app.run(debug=True)
