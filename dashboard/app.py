from flask import Flask, render_template, request

app = Flask(__name__)

@app.route("/")
def index():
	return render_template("index.html")

@app.route("/dashboard", methods = ["POST"])
def my_form_post():
	username = request.form['username']
	password = request.form['password']
	if username == "admin" and password == "admin":
		return render_template("dashboard.html")
	return render_template("index.html")

if __name__ == "__main__":
	app.run(debug = True)