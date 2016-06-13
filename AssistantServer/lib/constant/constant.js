/**
 * Created by garychen on 11/17/15.
 */
module.exports = {
	RECORD_STATUS: {
		UPLOADING: 0,
		JUDGING: 1,
		COMPLETED: 2
	},
	response_code: {
		ok: {
			code: 200
		},
		error: {
			code: 500
		},
		created: {
			code: 201
		}
	},
	user_role: {
		doctor: 0,
		patient: 1,
		assistant: 2
	},
	document_type: {
		User: "User",
		FriendShip: "FriendShip"
	},
	payment_money: {
		charge: 366,
		discount_charge: 99
	},
	pagination: {
		page: 1,
		pageSize: 5
	},
	viewQuery: {
		orderDesc: 2,
		orderAsc: 1,
		updateBefore: 1,
		updateNone: 2,
		updateAfter: 3
	},
	service_pay_status: {
		unpay: 2,
		payed: 3
	},
	service_need_pay: {
		neddPay: 1,
		noPay: 0
	},
	document_type: {
		User: "User",
		FriendShip: "FriendShip",
		Message: "Message",
		ShareMessage: "ShareMessage",
		SecurityCode: "SecurityCode",
		InvitationCode: "InvitationCode",
		Record: "Record",
		Patient: "Patient",
		UserProfile: "Profile",
		Contracter: "Contracter", //医生项目合作协议
		Version: "Version" //版本信息
	},
	query: {
		orderBy: {
			asc: 1,
			desc: 2
		}
	},
	//app版本校验url
	versionCheckUrl: {
		exclude: [
			"/version/add_version",
			"/version/checkVersion",
			"/records/upload_complete",
			"/records/cannot_recognize",
			"/records/amd_submit_record",
			"/records/import_patient_records"
		],
		allCheck: [
			"/version/checkVersion",
		]
	},
	//app版本校验错误码
	versionCheckCode: {
		appNeedReInstall: 600,
		appCanUpdate: 601,
		appJsBundleUpdate: 602
	}
};