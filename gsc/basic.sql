SELECT userInfo.userIdnfr, 
       userInfo.sawonNo, 
	   userInfo.name, 
	   userInfo.position, 
	   deviceInfo.userDevceIdnfr, 
	   deviceInfo.devceOsType,
	   pmsPushkeyInfo.pcPushKey,
	   ppsPushkeyInfo.UUID
  FROM [MTALK].[pms].[mPMS_USER_INFO10] userInfo,
		[MTALK].[pms].[mPMS_USER_DEVICE10] deviceInfo,
		[MTALK].[pms].[mPMS_PUSH_KEY_INFO] pmsPushkeyInfo,
		[MTALK].[pps].[mPPS_PUSH_KEY_MAP] ppsPushkeyInfo
  where userinfo.userIdnfr = deviceInfo.userIdnfr
  and deviceInfo.userDevceIdnfr = pmsPushkeyInfo.userDevceIdnfr
  and pmsPushKeyInfo.pcPushKey = ppsPushkeyInfo.PPC_PUSH_KEY
  and userInfo.regiStusDstcd = '10'
  and deviceInfo.regiStusDstcd = '10'
  and userInfo.name = '이승원'
