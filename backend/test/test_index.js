const request = require('supertest')
const app = require('../index')
const google_login = require('../google_login')

describe('POST /users/login', function(){
	it('respond with json', function(done) {
		google_login.verify = async () => { return {name: 'Joe', email: 'joe@x.com'}}
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
		google_login.verify = async () => { throw new Error("invalid") }
		request(app)
			.post('/users/login')
			.send({token: 'bad_token'})
			.set('Accept', 'application/json')
			.expect(200, {
				success: false,
				needs_register: false,
			})
			.end(function(err, res){
				if (err) return done(err);
				else done()
			});
	})
	it('asks unregistered users to register', function(done) {
		google_login.verify = async () => { return {name: 'Joe', email: 'joe@x.com'}}
		request(app)
			.post('/users/login')
			.send({token: 'bad_token'})
			.set('Accept', 'application/json')
			.expect(200, {
				success: false,
				needs_register: true,
			})
			.end(function(err, res){
				if (err) return done(err);
				done()
			});
	})
});
