const mongoose = require('mongoose')
const db = require('../db.js')

describe('manages users', function(){
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
