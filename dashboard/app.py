from flask import Flask, render_template
from flask_bootstrap import Bootstrap
from flask_wtf import FlaskForm
from wtforms import StringField, PasswordField, BooleanField
from wtforms.validators import InputRequired, Email, Length

app = Flask(__name__)
Bootstrap(app)

class LogInForm(FlaskForm):
	username = StringField('username', validators = [InputRequired(), Length(min=4, max=15)])
	password = PasswordField('password', validators = [InputRequired(), Length(min=8, max=80)])
	remember = BooleanField('remember me')

@app.route('/')
def index():
	return render_template('index.html')

@app.route('/login')
def login():
	form = LogInForm()
	return render_template('login.html', form = form)

@app.route('/signup')
def signup():
	return render_template('signup.html')

@app.route('/dashboard')
def dashboard():
	return render_template('dashboard.html')

if __name__ == '__main__':
	app.run(debug = True)