// Set up mongoose
const mongoose = require('mongoose')
mongoose.connect('mongodb://localhost:27017/ucsharecar', { useNewUrlParser : true} )
const db = mongoose.connection
db.on('error', console.error)
db.once('open', () => {
	console.log('Connected to MongoDB Server')
});

// Load the schemas
const User = require('./models/user.js')
const Post = require('./models/post')
const Report = require('./models/report')

module.exports = {

	user: {
		// TODO guserid needs to become this index id. This changes things.

		// Creates a new user. user_info must follow the form defined by
		// models/user.js.
		new: (user_info) => {
			const user = new User(user_info)
			user.save().then(() => console.log("Saved new user", user_info.guserid, "to DB"))
		},

		// Args should be self explanatory.
		add_phnum: (guserid, phnum) => {
			User.update({ guserid: guserid }, { phnum: phnum }, {}, (err, raw) => {
				if (err) console.log("For user", guserid, "add phnum error:", err)
				else console.log("Updated phnum for", guserid)
			})
		},

		// Takes a userid and two callbacks. If the user is registered, then the
		// is_registered callback is conditionally called. Otherwise,
		// is_not_registered gets called.
		cond_registered: (guserid, is_registered, is_not_registered) => {
			User.findOne({ guserid: guserid }, (err, doc) => {
				if (err) {
					console.log("Can't check if", guserid, "is registered")
					console.log(err)
					is_not_registered()
					return
				}

				if (!doc || !doc.phnum) {
					is_not_registered()
					return
				}

				is_registered()
			})
		},
	},

	post: {

		// Returns all posts in the db now
		find_all_posts: () => {
			Post.find((err, posts) => {
				if(err) { 
					return null;
				}
				return posts;	
			})	
		},

		// Returns the specific post with post_id 
		find_with_id: (post_id) => {
			Post.findOne({_id: post_id}, (err, post) => {
				if(err) {
					console.log('error occurred while finding the post with id')
					return null;
				}
				if(!post) {
					console.log('post not found')
					return null;
				}
				return post;
			})
		},

		// Create new post
		create_post: (user_id, req) => {
			var post = new Post()
			post.uploader = user_id
			post.start = req.start
			post.end = req.end
			post.driver = req.driver
			post.driverneeded = req.driverneeded
			post.totalseats = req.totalseats
			if(req.driverneeded) { 
				post.passengers = [user_id] 
			}
			else {
				post.passengers = [ ]
			}
			post.memo = req.memo
			post.departtime = new ISODate(req.departtime)

			post.save((err) => {
				if(err) {
					console.log(err)
					return false
				}
				else {
					return true
				}
			})	
		},
 		
		// Updates the driver or passenger status in the db
		update_post: (user_id, req) => {
			Post.findById(req.params.post_id, (err, post) => {
				if(err) {
					console.log('database failure')
					return false;
				}
				if(!post) {
					console.log('post not found')
					return false;
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
						return false;
					}
					return true;
				});
			});
		},	
	},

	report : {
		crete_post: (user_id, req) => {
			var report = new Report()

			report.uploader = user_id
			report.reported = req.reported
			report.title = req.title
			report.body = req.body
			//reporttime file is deafult
	
			report.save((err) => {
				if(err) {
					console.log(err)
					return false;
				}
				else {
					return true;
				}
			})	
		},
	},	
}
