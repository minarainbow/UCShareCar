const mongoose = require('mongoose')
const Schema = mongoose.Schema

// TODO delete username?
// TODO set default value for banned, maybe phnum
// TODO set "guserid" as the id index
const userSchema = new Schema({
	username: String,
	email: String,
	phnum: String,
	banned: Boolean,
	guserid: Number,
})

module.exports = mongoose.model('user', userSchema)
