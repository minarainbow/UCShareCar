/*
 * google_login.js
 *
 * Nearly all of this code is straight from Google. The only things changed were
 *  1. Making the code block until the Google result is received. TODO fix
 *  2. Sending the full payload as a result
 *  3. Checking for a UCSC email address
 *
 *  Check out https://developers.google.com/identity/sign-in/android/backend-auth
 */

module.exports = {
	verify: verify,
}

const secrets = require('./secrets')

// See https://developers.google.com/identity/sign-in/android/backend-auth
const {OAuth2Client} = require('google-auth-library');
const oauth_client = new OAuth2Client(secrets.CLIENT_ID);


// Verifies a token with the google servers. Returns the payload. If there is an
// error, then the payload is undefined.
// The payload items we want are .name, and .email
async function verify(token) {
	console.log("Verifying a token...")
	try {
		const ticket = await oauth_client.verifyIdToken({
			idToken: token,
			audience: secrets.CLIENT_ID,  // Specify the CLIENT_ID of the app that accesses the backend
		})
		const payload = ticket.getPayload()
		const domain = payload['hd']
		if (domain !== "ucsc.edu") {
			console.log("Not a valid UCSC email!")
			throw new Error("Email not from UCSC")
		}
		// Success
		return payload
	} catch(err) {
		console.log("Failed to verify user.", err)
		throw(err)
	}
}
