/*
 * This file sends a notification manually to a user by email address.
 * Usage:
 * node manual_notification.js [email address here] [post id here]
 *
 * This is purely for testing purposes.
 */

const db = require('./db')
const User = require('./models/user.js')
const notifications = require('./notifications')

email = process.argv[2]
post_id = process.argv[3]

db.connect(undefined, () => {
	notifications.initialize()
	User.findByEmail(email).then((user) => {
		notifications.send([user._id], post_id)
			.then(() => process.exit())
	})
})
