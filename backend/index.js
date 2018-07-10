const secrets = require('./secrets')

// Express
const express = require('express')
const bodyParser = require('body-parser');
const cookieParser = require('cookie-parser')
const app = express()
app.use(cookieParser(secrets.COOKIE))
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
const port = 8000

// Mongo
const mongoose = require('mongoose')
mongoose.connect('mongodb://localhost:27017/test', { useNewUrlParser : true} )
const db = mongoose.connection
db.on('error', console.error)
db.once('open', () => {
	console.log('Connected to MongoDB Server')
});

// TODO this is a very fake DB for testing only
fake_db = {}

// Google login service helpers
const google_login = require('./google_login')
// Session helpers
const sessions = require('./session_helpers')

app.get('/', ()=>{
	console.log('Hello')
})

// GET request for users
app.get('/users', (req, res) => {
	res.json('This is where users will be GET')
})

// POST request to create/register a new user
app.post('/users/login', async (req, res) => {
	const user = await google_login.verify(req.body.token)
	if (!user) {
		console.log("Could not verify user, returning fail")
		res.json({'success': false, 'needs_register': false})
	}
	else if (!(user.sub in fake_db && 'phnum' in fake_db[user.sub])) {
		// TODO look them up in the real database
		// For now, I am interepreting a lack of a user OR simply that their
		// phone number is missing as an *unregistered user*. Note I am doing
		// this because the phone number is the only thing that we don't get
		// from google.

		console.log("Recieved a login from unregistered user. Setting cookies and saving their info.")

		// Set their cookie so we can remember them
		sessions.create(res, user.sub)

		// Save their information (w/o phnum)
		// TODO use the real database
		fake_db[user.sub] = {
			'userid': user.sub,
			'name': user.name,
			'email': user.email,
		}

		res.json({'success': false, 'needs_register': true});
	}
	else {
		console.log("Verified login from", user.sub)
		sessions.create(res, user.sub)
		res.json({'success': true, 'needs_register': true})
	}
})

app.post('/users/register', async (req, res) => {
	if (!sessions.validate(req, res)) return

	// Check for the phone number actually being passed
	if (req.body.phnum === undefined) {
		console.log("Missing phone number to register")
		res.json({'success': false, 'error': 'Did not recieve a phone number'})
	}

	// Save the phnum
	console.log("Successfully saving phone number to register", req.signedCookies.session.userid)
	// TODO use actual database
	fake_db[req.signedCookies.session.userid]['phnum'] = req.body.phnum
	res.json({'success': true})
})

app.listen(port, (err) => {
	if (err) {
		return console.log('Failed to start:', err);
	}

	console.log('Running server on', port);
})

module.exports = app
