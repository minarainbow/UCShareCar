const mongoose = require('mongoose')
const Schema = mongoose.Schema

const postSchema = new Schema({
	posttime: {type: Date, default: Date.now},
	start: String,
	end: String,
	departtime: Date,
	driver: String,
	totalseats: Number,
	passengers: [String],
	memo: String,
	uploader: String,
	driverneeded: Boolean,	
})

module.exports = mongoose.model('post', postSchema)
