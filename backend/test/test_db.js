const mongoose = require('mongoose')
const db = require('../db.js')

describe('database manages users', function(){
	before(function() {
		db.connect('mongodb://localhost:27017/ucsharecar_test')
	})
	after(function() {
		mongoose.connection.db.dropDatabase();
	})
	it('creates a user', function(done) {
		db.user.create({
			name: "John Smith",
			email: "jsmith@example.com",
		}).then((id) => {
			done()
		}, (err) => {
			done(err)
		})
	})
	it('retrieves a user by id', function(done) {
		db.user.create({
			name: "John Smith",
			email: "jsmith@example.com",
		}).then((id) => {
			return db.user.find_with_id(id)
		}).then((user) => {
			if (!user || user.name !== "John Smith" || user.email !== "jsmith@example.com") {
				return done(new Error("Got the wrong user: "+user))
			}
			done()
		}).catch(done)
	})
	it('adds a phone number to a user', function(done) {
		db.user.create({
			name: "John Smith",
			email: "jsmith@example.com",
		}).then((id) => {
			return db.user.add_phnum(id, "867-5309")
		}).then(() => {
			done()
		}).catch(done)
	})
	it('recognizes nonexistent users', function(done) {
		db.user.check_registered('not_registered').then((id) => {
			done(new Error("Found an id for user that doesn't exist"))
		}, (err) => {
			done()
		})
	})
	it('recognizes unregistered users', function(done) {
		db.user.create({
			name: "Never Registered",
			email: "never@example.com",
		}).then((id) => {
			return db.user.check_registered('never@example.com')
		}, (err) => {
			done(err)
		}).then((id) => {
			done(new Error("Found an id for user that isn't registered"))
		}, (err) => {
			done()
		})
	})
	it('recognizes registered users', function(done) {
		db.user.create({
			name: "Immabe Registered",
			email: "but_not_yet@example.com",
		}).then((id) => {
			return db.user.add_phnum(id, "867-5309")
		}, (err) => {
			done(err)
		}).then((id) => {
			return db.user.check_registered('but_not_yet@example.com')
		}, (err) => {
			done(err)
		}).then((id) => {
			done()
		}, (err) => {
			done(err)
		})
	})
})

describe('database stores and retrieves posts', function() {
	before(function() {
		db.connect('mongodb://localhost:27017/ucsharecar_test')
	})
	after(function() {
		mongoose.connection.db.dropDatabase();
	})
	it('creates posts', function(done) {
		db.post.create({
			start: 'start',
			end: 'end',
			departtime: Date.now(),
			totalseats: 5,
			memo: 'This is a post 0',
		}).then((id) => {
			done()
		}, (err) => {
			done(err)
		})
	})
	it('retrieves posts by id', function(done) {
		var unique_memo = 'This is a post 1 uniqasdfasdf'
		db.post.create({
			start: 'start',
			end: 'end',
			departtime: Date.now(),
			totalseats: 5,
			memo: unique_memo,
		}).then((id) => {
			return db.post.find_with_id(id)
		}, (err) => {
			done(err)
		}).then((post) => {
			if (post.memo === unique_memo) {
				done()
			}
			else {
				done(new Error("Got the wrong post back"))
			}
		}, (err) => {
			done(err)
		})
	})
	it('retrieves all posts', function(done) {
		db.post.create({
			start: 'start',
			end: 'end',
			departtime: Date.now(),
			totalseats: 5,
			memo: 'Yet another post 2',
		}).then((id) => {
			return db.post.find_all()
		}, (err) => {
			done(err)
		}).then((posts) => {
			if (posts.length > 0) {
				done()
			}
			else {
				done(new Error("Did not get any posts back"))
			}
		}, (err) => {
			done(err)
		})
	})
	it('adds a driver', function(done) {
		var created_id, driver_added = "5b4a39ff74a2d138b93b2273"
		db.post.create({
			start: 'start',
			end: 'end',
			departtime: Date.now(),
			totalseats: 5,
			memo: 'Yet another post 2',
		}).then((id) => {
			created_id = id
			return db.post.add_driver(id, driver_added)
		}, (err) => {
			done(err)
		}).then(() => {
			return db.post.find_with_id(created_id)
		}, (err) => {
			done(err)
		}).then((post) => {
			if (post.driver != driver_added) {
				done(new Error('Expected driver '+driver_added+', got '+post.driver))
			}
			else if (post.driverneeded) {
				done(new Error('Added driver, but still claims driverneeded'))
			}
			else {
				done()
			}
		}, (err) => {
			done(err)
		})
	})
	it('adds a passenger', function(done) {
		var created_id, passenger_added = "5b4a39ff74a2d138b93b2273"
		db.post.create({
			start: 'start',
			end: 'end',
			departtime: Date.now(),
			totalseats: 5,
			memo: 'Yet another post 3',
			driver: passenger_added,
		}).then((id) => {
			created_id = id
			return db.post.add_passenger(id, passenger_added)
		}, (err) => {
			done(err)
		}).then(() => {
			return db.post.find_with_id(created_id)
		}, (err) => {
			done(err)
		}).then((post) => {
			if (post.passengers.length !== 1 || post.passengers[0] != passenger_added) {
				done(new Error('Expected passenger array '+[passenger_added]+
					', got '+post.passengers))
			}
			else {
				done()
			}
		}, (err) => {
			done(err)
		})
	})
})
