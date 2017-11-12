from flask import Flask, render_template, request, make_response, redirect, url_for

app = Flask(__name__)

@app.route("/")
def index():
	return render_template("index.html")

@app.route("/dashboard", methods = ["POST"])
def my_form_post():
	username = request.form['username']
	password = request.form['password']
	if username == "admin" and password == "admin":
		resp = make_response(render_template("dashboard.html"))
		resp.set_cookie('username', username)
		return resp
	return render_template("index.html")

@app.route("/register", methods = ["POST"])
def my_form_register():
	username = request.form['username']
	password = request.form['password']
	if username == "admin" and password == "admin":
		resp = make_response(render_template("dashboard.html"))
		resp.set_cookie('username', username)
		return resp
	return render_template("index.html")

@app.route("/logout")
def logout():
	return render_template("index.html")

if __name__ == "__main__":
	app.run(debug = True)
