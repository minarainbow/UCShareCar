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

// Google login service helpers
const google_login = require('./google_login')
// Session helpers
const sessions = require('./session_helpers')
// Database interactions
const db = require('./db.js')

app.get('/', () => {
	console.log('Requested index')
	res.json({result: 1})
})

// GET request for users
app.get('/users', (req, res) => {
	res.json('This is where users will be GET')
})

/*
 * POST request to create/register a new user
 * Expects a field "token" in the request body. Verifies with google.
 * Returns a JSON object with two fields:
 *	"success": true if the user is now logged in, false otherwise
 *	"needs_register": true if the user should be prompted to register, false o/w
 */
app.post('/users/login', async (req, res) => {
	// Check if the request actually has a token
	if (req.body.token === undefined) {
		console.log("Got a login request w/o a token")
		res.json({success: false, needs_register: false})
	}

	// Verify with google
	const user = await google_login.verify(req.body.token)
	if (!user) {
		console.log("Could not verify user, returning fail")
		res.json({success: false, needs_register: false})
		return
	}

	// If verified, check if they are registered or not
	else {
		db.user.check_registered(user.email).then((id) => {
			// Execued if the user is verified

			console.log("Verified login from", id)
			sessions.create(res, id)
			res.json({success: true, needs_register: true})
		},
		() => {
			// Executed if the user is not in the database
			console.log("Recieved a login from unregistered user. Setting cookies and saving their info.")

			// Save their information (w/o phnum)
			const id = db.user.new({
				'name': user.name,
				'email': user.email,
			})

			// Set their cookie so we can remember them
			sessions.create(res, id)

			res.json({success: false, needs_register: true});
		})
	}
})

/*
 * Registers a user. Users should have already attempted a login -- this handler
 * only completes the process of setting up a user. It essentially only adds the
 * phone number.
 * It expects a field "phnum" in the request body. This is a string representing
 * their phone number.
 * Return value has two fields:
 *	"success": true if user successfully registered, else false
 *	"error": an error if there was one, o/w undefined
 */
app.post('/users/register', async (req, res) => {
	if (!sessions.validate(req, res)) return

	// Check for the phone number actually being passed
	if (req.body.phnum === undefined) {
		console.log("Missing phone number to register")
		res.json({success: false, error: 'Did not recieve a phone number'})
	}

	// Save the phnum
	console.log("Successfully saving phone number to register", req.signedCookies.session.id)
	db.user.add_phnum(req.signedCookies.session.id, req.body.phnum)
	// TODO this success should probably be sent from a callback in the database
	res.json({success: true})
})

app.get('/post_list', (req, res) => {
	if (!sessions.validate(req, res)) return

	db.post.find_all_posts().then((posts) => {
		res.json(posts)
	}, (err) => {
		return res.status(500).send({error : 'database failure'})
	})
})

app.get('/post/:post_id', (req, res) => {
	if (!sessions.validate(req, res)) return

	db.post.find_with_id(req.params.post_id).then((post) => {
		if(post == null) {
			return res.status(404).json({error: 'post not found'})
		}
		else {
			res.json(post)
		}
	}, (err) => {
		return res.status(500).json({error : err})
	})
});

app.post('/create_post', (req, res) => {
	if (!sessions.validate(req, res)) return

	db.post.create_post(req.signedCookies.session.id, req.body).then((post) => {
	//db.post.create_post(0x5b47e4068f0c2cf5fd5b785a, req.body).then((post) => {
		res.json({result: 1})
	}, (err) => {
		res.json({result: 0})
	})
})

app.put('/update/:post_id', (req, res) => {
	if (!sessions.validate(req, res)) return

	db.post.update_post(req.signedCookies.session.id, req).then((post) => {
	//db.post.update_post(0x5b47e4068f0c2cf5fd5b785a, req).then((post) => {
		if(post == null) {
			return res.status(404).json({error: 'post not found'})
		}
		else {
			res.json({result: 1})
		}
	}, (err) => {
		res.json({result: 0})
	})
})

app.post('/report', (req, res) => {
	if (!sessions.validate(req, res)) return

	db.report.create_report(req.signedCookies.session.id, req.body).then((report) => {
	//db.report.create_report(0x5b47e4068f0c2cf5fd5b785a, req.body).then((report) => {	
		res.json({result: 1})
	}, (err) => {
		res.json({result: 0})
	})
})

app.listen(port, (err) => {
	if (err) {
		return console.log('Failed to start:', err);
	}

	console.log('Running server on', port);
})

module.exports = app
