from flask import Flask, render_template, request, make_response, redirect, url_for, jsonify
import requests
import sys
import hashlib
import json

app = Flask(__name__)

@app.route("/")
def index():
	return render_template("index.html")

@app.route("/dashboard", methods = ["POST", "GET"])
def my_form_post():
	if request.method == "GET":
		return render_template("dashboard.html")
	username = request.form['username']
	password = request.form['password']
	data = {
		'username': username,
		'password': hashlib.md5((password + "pollr").encode("utf-8")).hexdigest()
	}
	r = requests.post('http://10.199.25.174:5000/api/v1/plogin', data = data)
	print(r.text, file=sys.stderr)

	if r.text != "Fail":
		resp = make_response(render_template("dashboard.html", session = r.text))
		resp.set_cookie('username', username)
		resp.set_cookie('session_id', r.text)
		return resp
	return render_template("index.html")

@app.route("/register", methods = ["POST"])
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
	r = requests.post('http://10.199.25.174:5000/api/v1/pregister', data = data)
	return render_template("dashboard.html")

@app.route("/createpoll", methods=['GET', 'POST'])
def createpoll():
	if request.method == "GET":
		return render_template("createpoll.html")
	if request.method == "POST":
		type_ = request.form['type']
		district = request.form['district']
		question = request.form['question']
		choices = [None, None, None, None, None]
		if type_ == 'mc':
			a = request.form['a']
			b = request.form['b']
			c = request.form['c']
			d = request.form['d']
			e = request.form['e']
			choices = [a,b,c,d,e]

		choices = json.dumps(choices).replace('"','')
		print(choices, file = sys.stderr)

		
		demographic = request.form['demographics']
		filter_ = None
		if demographic == 'age':
			filter_ = request.form['age_groups']
		elif demographic == 'gender':
			filter_ = request.form['gender_groups']
		elif demographic == 'income':
			filter_ = request.form['income_groups']
		elif demographic == 'race':
			filter_ = request.form['race_groups']

		session_id = request.cookies.get('session_id')

		data = {
			'session_id': session_id,
			'question': question,
			'type': type_,
			'choices': choices,
			'district': district,
			'demographic': demographic,
			'filter': filter_
		}

		r = requests.post('http://10.199.25.174:5000/api/v1/pquestion', data = data)

	return render_template("createpoll.html")


@app.route("/user")
def user():
	username = request.cookies.get('username')
	session_id = request.cookies.get('session_id')
	data = {
		'session_id': session_id
	}
	r = requests.post('http://10.199.25.174:5000/api/v1/puserinfo', data = data)
	print(r.text, file=sys.stderr)
	resp = json.loads(r.text)
	name = resp['name']
	zipcode = resp['zip']

	return render_template("user.html", name = name, zipcode = zipcode)

@app.route("/logout")
def logout():
	return render_template("index.html")

if __name__ == "__main__":
	app.run(debug = True)
