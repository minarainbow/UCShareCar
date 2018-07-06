const request = require('supertest')
const app = require('../index')

describe('POST /users/register', function(){
	it('passes with flying colors', function(done) {
		done()
	})
	it('respond with json', function(done) {
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
});
