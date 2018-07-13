// Set up mongoose
const mongoose = require('mongoose')
mongoose.connect('mongodb://localhost:27017/ucsharecar', { useNewUrlParser : true} )
const db = mongoose.connection
db.on('error', console.error)
db.once('open', () => {
	console.log('Connected to MongoDB Server')
});

const Promise = require('promise')

// Load the schemas
const User = require('./models/user.js')
const Post = require('./models/post')
const Report = require('./models/report')

module.exports = {

	user: {
		// Creates a new user. user_info must follow the form defined by
		// models/user.js. Returns the id.
		new: (user_info) => {
			const user = new User(user_info)
			user.save().then(() => console.log("Saved new user", user_info.email, "to DB"))
			return user.id
		},

		// Args should be self explanatory.
		add_phnum: (id, phnum) => {
			User.findByIdAndUpdate(id, { phnum: phnum }, {}, (err, raw) => {
				if (err) console.log("For user", id, "add phnum error:", err)
				else console.log("Updated phnum for", id)
			})
		},

		// Takes an email and two callbacks. If the user is registered, then the
		// is_registered callback is conditionally called, with the id of the
		// existing user. Otherwise, is_not_registered gets called.
		cond_registered: (email, is_registered, is_not_registered) => {
			User.findOne({ email: email }, (err, doc) => {
				if (err) {
					console.log("Can't check if", email, "is registered")
					console.log(err)
					is_not_registered()
					return
				}

				if (!doc || !doc.phnum) {
					is_not_registered()
					return
				}

				is_registered(doc.id)
			})
		},
	},

	post: {

		// Returns all posts in the db now
		find_all_posts: () => {
			return new Promise((resolve, reject) => {
				Post.find((err, posts) => {
					if(err) { 
						reject(err)
					}
					resolve(posts)
				})	
			})
		},

		// Returns the specific post with post_id 
		find_with_id: (post_id) => {
			return new Promise((resolve, reject) => {
				Post.findOne({_id: post_id}, (err, post) => {
					if(err) {
						console.log('error occurred while finding the post with id')
						reject(err)
					}
					if(!post) {
						console.log('post not found')
						resolve(null)
					}
					resolve(post)
				})
			})
		},

		// Create new post
		create_post: (user_id, req) => {
			return new Promise((resolve, reject) => {
				var post = new Post()
				post.uploader = user_id
				post.start = req.start
				post.end = req.end
				// req.driver comes as hexstring
				post.driver = parseInt(req.driver)
				post.driverneeded = req.driverneeded
				post.totalseats = req.totalseats
				if(req.driverneeded) { 
					post.passengers = [user_id] 
				}
				else {
					post.passengers = [ ]
				}
				post.memo = req.memo
				post.departtime = new Date(req.departtime)
			
				is_success = false
				post.save((err) => {
					if(err) {
						reject(err)
					}
					else {
						resolve(post)
					}
				})
			})
		},
 		
		// Updates the driver or passenger status in the db
		update_post: (user_id, req) => {
			return new Promise((resolve, reject) => {
				Post.findById(req.params.post_id, (err, post) => {
					if(err) {
						console.log('database failure')
						reject(err)
					}
					if(!post) {
						console.log('post not found')
						resolve(null)
					}

					if(post.driverneeded) {
						post.driver = user_id
						post.driverneeded = false
					}
					else {
						post.passengers.push(user_id)
					}
					post.totalseats -= 1
	
					post.save((err) => {
						if(err) {
							console.log('failed to update')
							reject(err)
						}
						else {
							resolve(post)
						}			
					});
				});
			})
		},	
	},

	report : {
		create_report: (user_id, req) => {
			return new Promise((resolve, reject) => {
				var report = new Report()

				report.uploader = user_id
				report.reported = req.reported
				report.title = req.title
				report.body = req.body
				//reporttime file is deafult
	
				report.save((err) => {
					if(err) {
						console.log(err)
						reject(err)
					}
					else {
						resolve(report)
					}
				})	
			})
		},
	},	
}
