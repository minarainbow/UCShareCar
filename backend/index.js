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
		return
	}
	db.user.cond_registered(user.sub, () => {
		// Execued if the user is verified

		console.log("Verified login from", user.sub)
		sessions.create(res, user.sub)
		res.json({'success': true, 'needs_register': true})
	},
	() => {
		// Executed if the user is not in the database
		console.log("Recieved a login from unregistered user. Setting cookies and saving their info.")

		// Set their cookie so we can remember them
		sessions.create(res, user.sub)

		// Save their information (w/o phnum)
		db.user.new({
			'guserid': user.sub,
			'name': user.name,
			'email': user.email,
		})

		res.json({'success': false, 'needs_register': true});
	})
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
	db.user.add_phnum(req.signedCookies.session.userid, req.body.phnum)
	// TODO this success should probably be sent from a callback in the database
	res.json({'success': true})
})

app.get('/post_list', (req, res) => {
	if (!sessions.validate(req, res)) return

	posts = db.posts.find_all_posts()
	if(posts == null) {
		return res.status(500).send({error: 'database failure'});
	}
	res.json(posts)	
})

app.get('/api/books/:post_id', (req, res) => {
	if (!sessions.validate(req, res)) return

	post = db.post.find_with_id(req.params.post_id)
	if(post == null){
		return res.status(404).send({error: 'failed'})
	}
	res.json(post)
});

app.post('/create_post', (req, res) => {
	if (!sessions.validate(req, res)) return

	is_created = db.post.create_post(req.signedCookies.session.user_id, req)
	if(is_created) {
		res.json({result : 1})
	}
	else {
		res.json({result : 0})
	}
})

app.put('/api/update/:post_id', function(req, res){
	if (!sessions.validate(req, res)) return

	is_updated = db.post.update_post(req.signedCookies.session.user_id, req)
	if(is_updated) {
		res.json({result : 1})
	}
	else {
		res.json({result : 0})
	}
});

app.post('/report', (req, res) => {
	if (!sessions.validate(req, res)) return

	is_created = db.report.create_post(req.signedCookies.session.user_id, req)
	if(is_created) {
		res.json({result : 1})
	}
	else {
		res.json({result : 0})
	}
})

app.listen(port, (err) => {
	if (err) {
		return console.log('Failed to start:', err);
	}

	console.log('Running server on', port);
})

module.exports = app
