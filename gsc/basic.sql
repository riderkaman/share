SELECT userInfo.userIdnfr, 
       userInfo.sawonNo, 
       userInfo.name, 
       userInfo.position, 
       deviceInfo.userDevceIdnfr, 
       deviceInfo.devceOsType,
       pmsPushkeyInfo.pcPushKey,
       ppsPushkeyInfo.UUID,
       pushHistory.ID,
       pushHistory.PPC_PUSH_STATUS,
       pushHistory.REG_DT,
       pushData.PAYLOAD
  FROM [MTALK].[pms].[mPMS_USER_INFO10] userInfo,
       [MTALK].[pms].[mPMS_USER_DEVICE10] deviceInfo,
       [MTALK].[pms].[mPMS_PUSH_KEY_INFO] pmsPushkeyInfo,
       [MTALK].[pps].[mPPS_PUSH_KEY_MAP] ppsPushkeyInfo,
       [MTALK].[pps].[MPPS_PUSH_DEVICE] pushDevice,
       [MTALK].[pps].[MPPS_PUSH_DATA_HISTORY] pushHistory,
       [MTALK].[pps].[MPPS_PUSH_DATA] pushData
  WHERE userinfo.userIdnfr = deviceInfo.userIdnfr
  AND deviceInfo.userDevceIdnfr = pmsPushkeyInfo.userDevceIdnfr
  AND pmsPushKeyInfo.pcPushKey = ppsPushkeyInfo.PPC_PUSH_KEY
  AND ppsPushkeyInfo.PPC_PUSH_KEY = pushDevice.PPC_PUSH_KEY
  AND pushDevice.DEVICE_MSG_UUID = pushHistory.DEVICE_MSG_UUID
  AND pushDevice.MSG_UUID = pushData.MSG_UUID
  AND userInfo.regiStusDstcd = '10'
  AND deviceInfo.regiStusDstcd = '10'
  AND userInfo.name = '이승원'
  AND deviceInfo.devceOsType in ('A','P')
  ORDER BY pushHistory.reg_dt DESC;
