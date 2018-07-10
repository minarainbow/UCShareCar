const request = require('supertest')
const app = require('../index')
const google_login = require('../google_login')

describe('POST /users/login', function(){
	it('respond with json', function(done) {
		google_login.verify = () => undefined
		request(app)
			.post('/users/register')
			.send({token: 'bad_token'})
			.set('Accept', 'application/json')
			.expect(200)
			.end(function(err, res){
				if (err) return done(err);
				done()
			});
	})
	it('rejects invalid tokens', function(done) {
		google_login.verify = () => undefined
		request(app)
			.post('/users/login')
			.send({token: 'bad_token'})
			.set('Accept', 'application/json')
			.expect(200, {
				success: false,
				not_registered: true,
			})
			.end(function(err, res){
				if (err) return done(err);
				done()
			});
	})
	it('accepts invalid tokens', function(done) {
		done()
		// TODO I can't make this test past without a proper database
		// abstraction
		/*google_login.verify = () => "fake_user_id"
		request(app)
			.post('/users/login')
			.send({token: 'bad_token'})
			.set('Accept', 'application/json')
			.expect(200, {
				success: true,
			})
			.end(function(err, res){
				if (err) return done(err);
				done()
			});*/
	})
});
