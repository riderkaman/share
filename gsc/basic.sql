SELECT userInfo.userIdnfr, 
       userInfo.sawonNo, 
	   userInfo.name, 
	   userInfo.position, 
	   deviceInfo.userDevceIdnfr, 
	   deviceInfo.uuid, 
	   deviceInfo.devceOsType,
	   pushkeyInfo.pcPushKey
  FROM [MTALK].[pms].[mPMS_USER_INFO10] userInfo,
		[MTALK].[pms].[mPMS_USER_DEVICE10] deviceInfo,
		[MTALK].[pms].[mPMS_PUSH_KEY_INFO] pushkeyInfo
  where userinfo.userIdnfr = deviceInfo.userIdnfr
  and deviceInfo.userDevceIdnfr = pushkeyInfo.userDevceIdnfr
  and userInfo.regiStusDstcd = '10'
  and deviceInfo.regiStusDstcd = '10'
  and userInfo.name = '이승원'
