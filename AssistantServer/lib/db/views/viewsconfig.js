/**
 * Created by garychen on 11/16/15.
 */
module.exports = {
	//"user": {
	//	"designdocumentName":"user",
	//	"designdocumentViews":{
	//		"user_by_yiyinumber": {
	//			"name":"user_by_yiyinumber",
	//			"needUpdate":true,
	//			"oldViewname":"user_by_yiyinumber",
	//			"mapfunc":{
	//				map: function (doc, meta) {
	//					if (doc.type === "User" && doc._sync) {
	//						if (doc.yiyi_number != undefined) {
	//							emit(doc.yiyi_number, null);
	//						}
	//					}
	//				}
	//			}
	//		},
	//		"user_by_name": {
	//			"name":"user_by_name",
	//			"needUpdate":true,
	//			"oldViewname":"user_by_name",
	//			"mapfunc":{
	//				map: function (doc, mate) {
	//					if (doc.type=="User" && doc._sync) {
	//						emit(doc.name, doc.password);
	//					}
	//				}
	//			}
	//		},
	//		"user_by_userid_password": {
	//			"name":"user_by_userid_password",
	//			"needUpdate":true,
	//			"oldViewname":"user_by_userid_password",
	//			"mapfunc":{
	//				map: function (doc, meta) {
	//					if (doc.type === "User" && doc._sync) {
	//						emit(meta.id, doc.password);
	//					}
	//				}
	//			}
	//		},
	//		"user_by_role_type": {
	//			"name":"user_by_role_type",
	//			"needUpdate":true,
	//			"oldViewname":"user_by_role_type",
	//			"mapfunc":{
	//				map: function (doc, meta) {
	//					if (doc.type === "User" && doc._sync) {
	//						emit(doc.role_type, doc);
	//					}
	//				}
	//			}
	//		}
	//	}
	//},
	//"sharemessage": {
	//	"designdocumentName":"sharemessage",
	//	"designdocumentViews":{
	//		"user_by_userId": {
	//			"name":"user_by_userId",
	//			"needUpdate":true,
	//			"oldViewname":"user_by_userId",
	//			"mapfunc":{
	//				map: function (doc, meta) {
	//					if (doc.type === "ShareMessage" && doc._sync) {
	//						if (doc.user != undefined) {
	//							emit([doc.user, doc.createAt], doc);
	//						}
	//					}
	//				}
	//			}
	//		}
	//	}
	//},
	"record":{
		"designdocumentName":"record",
		"designdocumentViews":{
			"record_by_patientId": {
				"name":"record_by_patientId",
				"needUpdate":true,
				"oldViewname":"record_by_patientId",
				"mapfunc":{
					map: function (doc, meta) {
						if (doc.type === "Record" && doc._sync) {
							if (doc.patient != undefined) {
								emit(doc.patient, doc);
							}
						}
					}
				}
			}
		}
	},
	"patient":{
		"designdocumentName":"patient",
		"designdocumentViews":{
			"patient_by_userId": {
				"name":"patient_by_userId",
				"needUpdate":true,
				"oldViewname":"patient_by_userId",
				"mapfunc":{
					map: function (doc, meta) {
						if (doc.type === "Patient" && doc._sync) {
							if (doc.user != undefined) {
								emit(doc.user, doc);
							}
						}
					}
				}
			}
		}
	},
	"user":{
		"designdocumentName":"user",
		"designdocumentViews":{
			"doctor_friends_by_assistantId": {
				"name":"doctor_friends_by_assistantId",
				"needUpdate":true,
				"oldViewname":"doctor_friends_by_assistantId",
				"mapfunc":{
					map: function (doc, meta) {
						if (doc.type === "User" && doc.role_type == 0 && doc._sync) {
							emit(doc.assistant, doc);
						}
					}
				}
			}
		}
	},
	"service":{
		"designdocumentName":"service",
		"designdocumentViews":{
			"service_by_patientId": {
				"name":"service_by_patientId",
				"needUpdate":true,
				"oldViewname":"service_by_patientId",
				"mapfunc":{
					map: function (doc, meta) {
						if (doc.type === "Service" && doc.payStatus == 3 && doc._sync) {
							if (doc.patient != undefined) {
								emit([doc.patient, "" + doc.date], doc);
							}
						}
					}
				}
			},
			"service_by_doctorId": {
				"name":"service_by_doctorId",
				"needUpdate":true,
				"oldViewname":"service_by_doctorId",
				"mapfunc":{
					map: function (doc, meta) {
						if (doc.type === "Service" && doc.payStatus == 3 && doc._sync) {
							if (doc.patient != undefined) {
								emit([doc.from, "" + doc.date], doc);
							}
						}
					},
					reduce: function (keys, values, rereduce) {
						if(rereduce) {
							return sum(values);
						} else {
							var priceSum = 0;
							for (var i=0; i<values.length; i++) {
								if(values[i] && values[i].price){
									priceSum += values[i].price;
								}
							}
							return priceSum;
						}
					}
				}
			}
		}
	}

};