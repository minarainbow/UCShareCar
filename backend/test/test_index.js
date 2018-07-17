const request = require('supertest')
const mongoose = require('mongoose')
const app = require('../app')
const db = require('../db.js')
const google_login = require('../google_login')
const session = require('../session_helpers')

describe('server handlers', function() {
	before(function(done) {
		db.connect('mongodb://localhost:27017/ucsharecar_test', done)
		app.listen(8000, (err) => {
			if (err) {
				return console.log('Failed to start:', err)
			}
			console.log('Running server on', 8000)
		})
	})
	after(function() {
		mongoose.connection.db.dropDatabase()
	})

	describe('handles user login', function(){
		it('respond with json', function(done) {
			google_login.verify = async () => { return {name: 'Joe', email: 'joe@x.com'}}
			request(app)
				.post('/users/register')
				.send({token: 'bad_token'})
				.set('Accept', 'application/json')
				.expect(200)
				.end(function(err, res){
					if (err) return done(err)
					done()
				})
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
					if (err) return done(err)
					else done()
				})
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
					if (err) return done(err)
					done()
				})
		})
		it('logs users out', function(done) {
			var agent = request.agent(app)
			google_login.verify = async () => { return {name: 'Joe', email: 'joe@x.com'}}
			agent
				.post('/users/login')
				.send({token: 'bad_token'})
				.set('Accept', 'application/json')
				.expect(200, {
					success: false,
					needs_register: true,
				}).end(function(err, res) {
					agent
						.post('/users/logout')
						.set('Accept', 'application/json')
						.expect('set-cookie',
							'session=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT',
							done)
				})
		})
		it('retrieves users by id', function(done) {
			var agent = request.agent(app)
			db.user.create({
				name: "John Smith",
				email: "jsmith@example.com",
			}).then((lookup_id) => {
				agent
					.post('/users/login')
					.send({token: 'bad_token'})
					.set('Accept', 'application/json')
					.expect(200, {
						success: false,
						needs_register: true,
					})
					.end(function(err, res){
						agent
							.get('/users/by_id/'+lookup_id)
							.set('Accept', 'application/json')
							.expect(200)
							.end(function(err, res) {
								if (err) return done(err)
								if (res.body.result !== 1) {
									return done(new Error("Result was not 1: "+res.body.error))
								}
								if (res.body.user._id != lookup_id) {
									return done(new Error("Got wrong user, wanted "
										+lookup_id+" but got "+res.body.user._id))
								}
								done()
							})
					})
			}).catch(done)
		})
	})

	describe('handles post creation and retrieval', function() {
		var agent = request.agent(app)
		before(function(done) {
			// Create a logged in session
			google_login.verify = async () => { return {name: 'Joe', email: 'joe@x.com'}}
			agent
				.post('/users/login')
				.send({token: 'bad_token'})
				.set('Accept', 'application/json')
				.expect(200, {
					success: false,
					needs_register: true,
				}, done)
		})
		it('fails when no post is sent to save', function(done) {
			agent
				.post('/posts/create')
				.send(/* No post sent!! */)
				.set('Accept', 'application/json')
				.expect(200, {
					result: 0,
					error: "No post passed to create",
				})
				.end(function(err, res) {
					if (err) return done(err)
					done()
				})
		})
		it('saves posts and returns id', function(done) {
			agent
				.post('/posts/create')
				.send({post: {
					memo: 'Does not matter',
				}})
				.set('Accept', 'application/json')
				.expect(200)
				.end(function(err, res) {
					if (err) return done(err)
					if (res.body.result !== 1) return done(new Error("Result was "+res.body.result+", not 1"))
					if (!res.body.post_id) return done(new Error("No post id"))
					done()
				})
		})
		it('retrieves posts by id', function(done) {
			agent
				.post('/posts/create')
				.send({post: {
					memo: 'Does not matter',
				}})
				.set('Accept', 'application/json')
				.expect(200)
				.then((res) => {
					agent
						.get('/posts/by_id/'+res.body.post_id)
						.set('Accept', 'application/json')
						.expect(200)
						.end(function(err, res) {
							if (err) return done(err)
							if (res.body.result !== 1)
								return done(new Error("Result was not 1"))
							if (!res.body.post)
								return done(new Error("No post in response"))
							done()
						})
				})
		})
		it('retrieves all posts', function(done) {
			agent
				.get('/posts/all')
				.set('Accept', 'application/json')
				.expect(200)
				.end(function(err, res) {
					if (err) return done(err)
					if (res.body.result !== 1)
						return done(new Error("Result was not 1"))
					if (!res.body.posts)
						return done(new Error("No posts in response"))
					done()
				})
		})
		it('adds a passenger successfully', function(done) {
			agent
				.post('/posts/create')
				.send({post: {
					memo: 'This post will get one passenger',
					driver: 'FFFFFFFFFFFFFFFFFFFFFFFF',
					totalseats: 10,
				}})
				.set('Accept', 'application/json')
				.expect(200)
				.then((res) => {
					var post_id = res.body.post_id

					agent
						.post('/posts/add_passenger')
						.send({post_id: res.body.post_id})
						.set('Accept', 'application/json')
						.expect(200)
						.then((res) => {
							if (res.body.result !== 1)
								return done(new Error("Result was not 1 when adding passenger"))
							else {
								agent
									.get('/posts/by_id/'+post_id)
									.set('Accept', 'application/json')
									.expect(200)
									.end(function(err, res) {
										if (err) return done(err)
										if (res.body.result !== 1)
											return done(new Error("Result was not 1 when checking post"))
										if (res.body.post.passengers.length === 0)
											return done(new Error("No passengers in final result"))
										done()
									})
							}
						})
				})
		})
		it('does not add a passenger when full', function(done) {
			agent
				.post('/posts/create')
				.send({post: {
					memo: 'This should not recieve more passengers',
					driver: 'FFFFFFFFFFFFFFFFFFFFFFFF',
					totalseats: 1,
					passengers: [
						// ObjectIds are 24 character hex strings.
						'FFFFFFFFFFFFFFFFFFFFFFFF',
					],
				}})
				.set('Accept', 'application/json')
				.expect(200)
				.then((res) => {
					var post_id = res.body.post_id

					agent
						.post('/posts/add_passenger')
						.send({post_id: res.body.post_id})
						.set('Accept', 'application/json')
						.expect(200)
						.then((res) => {
							if (res.body.result !== 0)
								return done(new Error("Result was not 0 when adding passenger over limit"))
							else {
								agent
									.get('/posts/by_id/'+post_id)
									.set('Accept', 'application/json')
									.expect(200)
									.end(function(err, res) {
										if (err) return done(err)
										if (res.body.result !== 1)
											return done(new Error("Result was not 1 when checking post"))
										if (res.body.post.passengers.length !== 1)
											return done(new Error("Amount of passengers is not 1, got "+
												res.body.post.passengers.length))
										done()
									})
							}
						})
				})
		})
		it('does not add passengers without a driver', function(done) {
			agent
				.post('/posts/create')
				.send({post: {
					memo: 'We should never get passengers w/o a driver',
					totalseats: 1,
				}})
				.set('Accept', 'application/json')
				.expect(200)
				.then((res) => {
					var post_id = res.body.post_id

					agent
						.post('/posts/add_passenger')
						.send({post_id: res.body.post_id})
						.set('Accept', 'application/json')
						.expect(200)
						.then((res) => {
							if (res.body.result !== 0)
								return done(new Error("Result was not 0 when adding passenger w/o driver"))
							else {
								agent
									.get('/posts/by_id/'+post_id)
									.set('Accept', 'application/json')
									.expect(200)
									.end(function(err, res) {
										if (err) return done(err)
										if (res.body.result !== 1)
											return done(new Error("Result was not 1 when checking post"))
										if (res.body.post.passengers.length !== 0)
											return done(new Error("Amount of passengers is not 0, got "+
												res.body.post.passengers.length))
										done()
									})
							}
						})
				})
		})
		it('adds a driver successfully', function(done) {
			agent
				.post('/posts/create')
				.send({post: {
					memo: 'This post will soon get a driver',
					totalseats: 1,
				}})
				.set('Accept', 'application/json')
				.expect(200)
				.then((res) => {
					var post_id = res.body.post_id

					agent
						.post('/posts/add_driver')
						.send({post_id: res.body.post_id})
						.set('Accept', 'application/json')
						.expect(200)
						.then((res) => {
							if (res.body.result !== 1)
								return done(new Error("Result was not 1 when adding driver"))
							else {
								agent
									.get('/posts/by_id/'+post_id)
									.set('Accept', 'application/json')
									.expect(200)
									.end(function(err, res) {
										if (err) return done(err)
										if (res.body.result !== 1)
											return done(new Error("Result was not 1 when checking post"))
										if (!res.body.post.driver)
											return done(new Error("Post does not have a driver"))
										done()
									})
							}
						})
				})
		})
	})
	describe('rejects requests with no session', function() {
		const agent = request.agent(app)
		const validated_get_endpoints = [
			'/users/by_id/5', '/posts/all', '/posts/by_start', '/posts/by_end',
		]
		const validated_post_endpoints = [
			'/users/logout', '/users/register', '/posts/create',
			'/posts/add_passenger', '/posts/add_driver', '/report',
		]
		const check = function(endpoint, method) {
			it('refuses '+endpoint, function(done) {
				agent[method](endpoint)
					.set('Accept', 'application/json')
					.expect(200, {
						error: "Not a valid session"
					}, done)

			})
		}
		for (endpoint of validated_get_endpoints) {
			check(endpoint, "get")
		}
		for (endpoint of validated_post_endpoints) {
			check(endpoint, "post")
		}
	})
})
