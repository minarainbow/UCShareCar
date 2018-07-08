const mongoose = require('mongoose')
const Schema = mongoose.Schema

const postSchema = new Schema({
	posttime: {type: Date, default: Date.now},
	start: String,
	end: String,
	departtime: Date,
	driver: {
		email: String
	},
	totalseats: Number,
	passengers: [String],
})

module.exports = mongoose.model('post', postSchema)
