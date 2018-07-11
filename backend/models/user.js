const mongoose = require('mongoose')
const Schema = mongoose.Schema

const userSchema = new Schema({
	username: String,
	email: String,
	phnum: String,
	banned: Boolean,
	guserid: Number,
})

module.exports = mongoose.model('user', userSchema)
