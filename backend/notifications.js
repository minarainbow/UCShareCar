const admin = require('firebase-admin')
const secrets = require('./secrets')
const db = require('./db')

function initialize() {
	var serviceAccount = require('./serviceAccountKey.json');

	admin.initializeApp({
		credential: admin.credential.cert(serviceAccount),
		projectId: secrets.FIREBASE_PROJECTID,
	});

	console.log("Initialized Firebase service")
}

/*
 * This method sends a notification to all user IDs specified, with data
 * specifying which post changed. It will look up the FCM tokens required to
 * send the notification itself.
 */
function send(user_ids, post_id) {
	// send_by_postid may sometimes give us an empty list, which would be an
	// error. skip if no users to notify.
	if (user_ids.length === 0) {
		console.log("No users to notify for post", post_id, "!")
		return
	}

	console.log("Sending a notification to user array for post", post_id)
	return db.user.all_fcm_with_ids(user_ids)
		.then((tokens) => {
			return admin.messaging().sendToDevice(tokens, {
				data: {
					post_id: post_id,
				}
			})
		})
		.catch((error) => {
			console.log("Could not send notification.")
			console.log(error)
		})
}

/*
 * This method sends a notification to all users of a post. It is different from
 * send() in that it will find the users for you first.
 *
 * except_user_id can optionally be passed to specify a user that should *not*
 * get the notification.
 */
function send_by_postid(post_id, except_user_id) {
	console.log("Finding all users to notify for post", post_id)
	return db.post.find_with_id(post_id)
		.then((post) => {
			// Find the list of users in this post
			users = []
			for (passenger of post.passengers) {
				if (except_user_id != passenger) {
					users.push(passenger)
				}
			}
			if (except_user_id != post.driver) {
				users.push(post.driver)
			}

			// Send notification
			return send(users, post_id)
		})
		.catch((error) => {
			console.log("Could not find users to notify for post", post_id)
			console.log(error)
		})
}

module.exports = {
	send: send,
	send_by_postid: send_by_postid,
	initialize: initialize,
}
