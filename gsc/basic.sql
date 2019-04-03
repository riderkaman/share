SELECT userInfo.userIdnfr, 
       userInfo.sawonNo, 
	   userInfo.name, 
	   userInfo.position, 
	   deviceInfo.userDevceIdnfr, 
	   deviceInfo.devceOsType,
	   pmsPushkeyInfo.pcPushKey,
	   ppsPushkeyInfo.UUID,
	   pushHistory.ID,
	   pushHistory.PPC_PUSH_STATUS
  FROM [MTALK].[pms].[mPMS_USER_INFO10] userInfo,
		[MTALK].[pms].[mPMS_USER_DEVICE10] deviceInfo,
		[MTALK].[pms].[mPMS_PUSH_KEY_INFO] pmsPushkeyInfo,
		[MTALK].[pps].[mPPS_PUSH_KEY_MAP] ppsPushkeyInfo,
	    [MTALK].[pps].[MPPS_PUSH_DEVICE] pushDevice,
	    [MTALK].[pps].[MPPS_PUSH_DATA_HISTORY] pushHistory
  where userinfo.userIdnfr = deviceInfo.userIdnfr
  and deviceInfo.userDevceIdnfr = pmsPushkeyInfo.userDevceIdnfr
  and pmsPushKeyInfo.pcPushKey = ppsPushkeyInfo.PPC_PUSH_KEY
  and ppsPushkeyInfo.PPC_PUSH_KEY = pushDevice.PPC_PUSH_KEY
  and pushDevice.DEVICE_MSG_UUID = pushHistory.DEVICE_MSG_UUID
  and userInfo.regiStusDstcd = '10'
  and deviceInfo.regiStusDstcd = '10'
  and userInfo.name = '이승원'
  order by pushHistory.reg_dt asc;
