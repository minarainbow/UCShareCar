const mongoose = require('mongoose')
const Schema = mongoose.Schema
var ObjectId = mongoose.Schema.Types.ObjectId

const reportSchema = new Schema({
	uploader: ObjectId,
	reported: ObjectId,
	title: String,
	body: String,
	reporttime: {type: Date, default: Date.now},
})

module.exports = mongoose.model('report', reportSchema)
