const mongoose = require('mongoose')
const Schema = mongoose.Schema

const reportSchema = new Schema({
	uploader: Number,
	reported: Number,
	title: String,
	body: String,
	reporttime: {type: Date, default: Date.now},
})

module.exports = mongoose.model('report', reportSchema)
