module.exports = {
	verify: verify,
}

const secrets = require('./secrets')

// See https://developers.google.com/identity/sign-in/android/backend-auth
const {OAuth2Client} = require('google-auth-library');
const oauth_client = new OAuth2Client(secrets.CLIENT_ID);
async function verify(token) {
	console.log("Verifying a token...")
	try {
		const ticket = await oauth_client.verifyIdToken({
			idToken: token,
			audience: secrets.CLIENT_ID,  // Specify the CLIENT_ID of the app that accesses the backend
		});
		const payload = ticket.getPayload();
		const userid = payload['sub'];
		const domain = payload['hd'];
		if (domain !== "ucsc.edu") {
			console.log("Not a valid UCSC email!")
			return undefined
		}
		return userid
	} catch(err) {
		console.log("Failed to verify user.", err)
		return undefined
	}
}
