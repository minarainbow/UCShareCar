module.exports = {
	// Create sets up a signed cookie that saves the id of the currently logged
	// in user. It also saves data about its start and expiration date in JSON
	// along with the actual cookie expiration date.
	create: (res, id, timeout) => {
		// Default timeout will be 24 hours (* 60 min/hr * 60 seconds/min * 1000 ms/s)
		timeout = timeout || 24*60*60*1000
		res.cookie('session', {
				'id': id,
				'start_time': Date.now(),
				'exp_time': Date.now() + timeout,
			},
			{
				maxAge: timeout,
				signed: true,
			})
	},
	destroy: (res) => {
		res.clearCookie('session')
	},
	validate: (req, res) => {
		// validate writes an error and returns false if the session was not
		// valid. Use a pattern like "if (!session.validate(...)) return".
		if (!('session' in req.signedCookies
				&& Date.now() <= req.signedCookies.session.exp_time)) {
			console.log("Recieved an invalid session")
			res.json({'error': 'Not a valid session'})
			return false
		}
		return true
	},
}
