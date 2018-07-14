const mongoose = require('mongoose')
const Schema = mongoose.Schema
var ObjectId = mongoose.Schema.Types.ObjectId

const postSchema = new Schema({
	posttime: {type: Date, default: Date.now},
	start: String,
	end: String,
	departtime: Date,
	driver: ObjectId,
	totalseats: Number,
	passengers: {
		type: [ObjectId],
		default: [],
	},
	memo: String,
	uploader: ObjectId,
	driverneeded: {
		type: Boolean,
		default: true,
	}
})

module.exports = mongoose.model('post', postSchema)
