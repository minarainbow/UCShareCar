const secrets = require('./secrets')

// Express
const express = require('express')
const bodyParser = require('body-parser')
const cookieParser = require('cookie-parser')
const app = express()
app.use(cookieParser(secrets.COOKIE))
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: true }))

// Google login service helpers
const google_login = require('./google_login')
// Session helpers
const sessions = require('./session_helpers')
// Database interactions
const db = require('./db.js')

app.get('/', (req, res) => {
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

	// TODO Despite using promises, I seem to have not avoided callback hell.

	// Verify with google
	google_login.verify(req.body.token).then((user) => {
		// If verified, check if they are registered or not
		db.user.check_registered(user.email).then((id) => {
			// Executed if the user is verified

			console.log("Verified login from", id)
			sessions.create(res, id)
			res.json({success: true, needs_register: true})
		},
		() => {
			// Executed if the user is not in the database
			console.log("Recieved a login from unregistered user. Setting cookies and saving their info.")

			// Save their information (w/o phnum)
			db.user.create({
				'name': user.name,
				'email': user.email,
			}).then((id) => {
				// If we succeeded, then set their session cookie (save their
				// user id) and tell them to register
				sessions.create(res, id)
				res.json({success: false, needs_register: true})
			}, (err) => {
				// If they were not saved, send a failure
				res.json({success: false, needs_register: false})
			})
		})
	}, (err) => {
		console.log("Could not verify user, returning fail:", err)
		res.json({success: false, needs_register: false})
	})
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
	console.log("Saving phnum for", req.signedCookies.session.id)
	db.user.add_phnum(req.signedCookies.session.id, req.body.phnum).then(() => {
		res.json({success: true})
	}, () => {
		res.json({success: false})
	})
})

/*
 * Returns all posts in the database. Takes no arguments. Return JSON object has
 * two fields:
 *	result: 1 if success, else 0
 *	posts: an array of all posts.
 */
app.get('/posts/all', (req, res) => {
	if (!sessions.validate(req, res)) return

	db.post.find_all().then((posts) => {
		res.json({result: 1, posts: posts})
	}, (err) => {
		return res.status(500).send({result: 0, error : 'database failure'})
	})
})

/*
 * Returns one post with the given ID. The id is to be passed in the request
 * body in JSON as the field "post_id". Return value, in JSON, has two fields:
 *	result: 1 if success, else 0
 *	post: the post whose id was requested
 */
app.get('/posts/by_id', (req, res) => {
	if (!sessions.validate(req, res)) return

	if (!req.body.post_id) {
		res.json({result: 0})
	}

	db.post.find_with_id(req.body.post_id).then((post) => {
		if (post == null) {
			return res.status(404).json({result: 0, error: 'post not found'})
		}
		else {
			res.json({result: 1, post: post})
		}
	}, (err) => {
		return res.status(500).json({result: 0, error: err})
	})
})

/*
 * Creates a new post. All the data associated with the post must be in the
 * field "post" in the request body. The return value has two fields:
 *	result: 1 if good, else 0
 *	post_id: the ID of the post that was created, otherwise undefined
 */
app.post('/posts/create', (req, res) => {
	if (!sessions.validate(req, res)) return

	if (!req.body.post) {
		res.json({result: 0})
		return
	}

	// Set the uploader of this post
	req.body.post.uploader = req.signedCookies.session.id

	db.post.create(req.body.post).then((id) => {
		res.json({result: 1, post_id: id})
	}, (err) => {
		res.json({result: 0})
	})
})

/*
 * Adds a passenger to a given post. The post to be added to is passed by ID in
 * post_id, in the request body. The user that will be added as a passenger is
 * the user that is making this request. The user will NOT be added as the
 * passenger if there is no driver or if there is no space.
 * Return value:
 *	result: 1 if success, else 0
 *	error: an error string if there was one
 */
app.post('/posts/add_passenger', (req, res) => {
	if (!sessions.validate(req, res)) return

	if (!req.body.post_id) {
		res.json({result: 0})
	}

	db.post.add_passenger(req.body.post_id, req.signedCookies.session.id).then(() => {
		res.json({result: 1})
	}, (err) => {
		res.json({result: 0, error: err})
	})
})

/*
 * Adds the user making the request as the driver for the post with post_id
 * specified in the request body.
 * Return value:
 *	result: 1 if success, else 0
 *	error: an error string if there was one
 */
app.post('/posts/add_driver', (req, res) => {
	if (!sessions.validate(req, res)) return

	if (!req.body.post_id) {
		res.json({result: 0})
	}

	db.post.add_driver(req.body.post_id, req.signedCookies.session.id).then(() => {
		res.json({result: 1})
	}, (err) => {
		res.json({result: 0, error: err})
	})
})

app.put('/posts/update', (req, res) => {
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

module.exports = app
