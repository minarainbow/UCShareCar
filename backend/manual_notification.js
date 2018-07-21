/*
 * This file sends a notification manually to a user by email address.
 * Usage:
 * node manual_notification.js [email address here]
 */

const db = require('./db')
const User = require('./models/user.js')
const notifications = require('./notifications')

db.connect(undefined, () => {
	notifications.initialize()
	User.findByEmail(process.argv[2]).then((user) => {
		notifications.send([user._id], "fake post ID")
			.then(() => process.exit())
	})
})
