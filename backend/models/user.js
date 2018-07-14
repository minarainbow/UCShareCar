const mongoose = require('mongoose')
const Schema = mongoose.Schema

const userSchema = new Schema({
	email: {
		type: String,
		index: true,
	},
	name: String,
	phnum: String,
	banned: {
		type: Boolean,
		default: false,
	},
})

userSchema.statics.findByEmail = function(email) {
	return this.findOne({ email: email })
}

module.exports = mongoose.model('user', userSchema)
