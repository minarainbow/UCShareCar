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
}
