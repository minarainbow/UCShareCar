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
}
