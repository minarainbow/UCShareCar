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

function send(user_ids, post_id) {
	console.log("Sending a notification to some users...")
	return db.user.all_with_ids(user_ids)
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

module.exports = {
	send: send,
	initialize: initialize,
}
