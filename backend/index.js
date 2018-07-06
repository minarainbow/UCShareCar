// Express
const express = require('express')
const bodyParser = require('body-parser');
const app = express()
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
const port = 8000

// Mongo
const mongoose = require('mongoose')
mongoose.connect('mongodb://localhost/test')
const db = mongoose.connection
db.on('error', console.error)
db.once('open', () => {
	console.log('Connected to MongoDB Server')
});


// Google login service helpers
const google_login = require('./google_login')

// GET request for users
app.get('/users', (req, res) => {
	res.json('This is where users will be GET')
});

// POST request to create/register a new user
app.post('/users/register', async (req, res) => {
	const userid = await google_login.verify(req.body.token)
	if (!userid) {
		res.json({'success': false})
		return
	}
	console.log("Verified", userid)
	res.json({'success': true})
});

app.listen(port, (err) => {
	if (err) {
		return console.log('Failed to start:', err);
	}
	
	console.log('Running server on', port);
})
