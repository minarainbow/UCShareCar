const express = require('express')
const bodyParser = require('body-parser');
const app = express()
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
const port = 8000

const mongoose = require('mongoose')
mongoose.connect('mongodb://localhost/test')
const db = mongoose.connection
db.on('error', console.error)
db.once('open', () => {
	console.log('Connected to MongoDB Server')
});

const secrets = require('./secrets')

// See https://developers.google.com/identity/sign-in/android/backend-auth
const {OAuth2Client} = require('google-auth-library');
const oauth_client = new OAuth2Client(secrets.CLIENT_ID);
async function verify(token) {
	console.log("Verifying a token...")
	try {
		const ticket = await oauth_client.verifyIdToken({
			idToken: token,
			audience: secrets.CLIENT_ID,  // Specify the CLIENT_ID of the app that accesses the backend
		});
		const payload = ticket.getPayload();
		const userid = payload['sub'];
		const domain = payload['hd'];
		if (domain !== "ucsc.edu") {
			console.log("Not a valid UCSC email!")
			return undefined
		}
		return userid
	} catch(err) {
		console.log("Failed to verify user.", err)
		return undefined
	}
}

// GET request for users
app.get('/users', (req, res) => {
	res.json('This is where users will be GET')
});

// POST request to create/register a new user
app.post('/users/register', async (req, res) => {
	const userid = await verify(req.body.token)
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
