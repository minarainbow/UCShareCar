/*
 * index.js
 *
 * This simply sets up all the required components to run the server, and then
 * does so. See all the other files require()'d below.
 */
const app = require('./app')
const db = require('./db')
const notifications = require('./notifications')
const port = 8000

// Where did all the code go? It's now in app.js.
// This structure makes it easier to unit test.

db.connect()
notifications.initialize()
app.listen(port, (err) => {
	if (err) {
		return console.log('Failed to start:', err)
	}

	console.log('Running server on', port)
})
