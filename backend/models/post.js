const mongoose = require('mongoose')
const Schema = mongoose.Schema

const postSchema = new Schema({
	posttime: {type: Date, default: Date.now},
	start: String,
	end: String,
	departtime: Date,
	driver: Number,
	totalseats: Number,
	passengers: [Number],
	memo: String,
	uploader: Number,
	driverneeded: Boolean,	
})

module.exports = mongoose.model('post', postSchema)
