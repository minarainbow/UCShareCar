const mongoose = require('mongoose')
const Schema = mongoose.Schema

const userSchema = new Schema({
	name: String,
	email: String,
	phnum: String,
	banned: Boolean,
})

module.exports = mongoose.model('user', userSchema)
