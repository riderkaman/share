package com.happyhouse.servers.ts.taslet.talk;

import com.happyhouse.core.tas.bean.TasBean;
import com.happyhouse.core.tas.taslet.TasRequest;
import com.happyhouse.core.tas.taslet.TasResponse;
import com.happyhouse.core.tas.taslet.Taslet;
import com.happyhouse.core.tas.taslet.TasletException;
import com.happyhouse.servers.ts.constants.ComConf;
import com.happyhouse.servers.ts.constants.Constants;
import com.happyhouse.servers.ts.constants.GV;
import com.happyhouse.servers.ts.core.taslet.TasletMap;
import com.happyhouse.servers.ts.constants.Constants.RoomInfo;
import com.happyhouse.servers.ts.push.PushTalkProc;
import com.happyhouse.servers.ts.service.CmBatchQueProcThreadService;
import com.happyhouse.servers.ts.service.TalkDBService;
import com.happyhouse.servers.ts.utils.CaseMap;
import com.happyhouse.servers.ts.utils.CommonUtils;
import com.happyhouse.servers.ts.utils.DateUtils;
import com.happyhouse.servers.ts.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 신관영 : 사용함
@Component("TALK.PPTALK005")
@Scope("prototype")
@Transactional
@Slf4j
public class PP_TALK005_SendMediaTaslet implements Taslet {

//    private static final Logger log = LoggerFactory.getLogger(PP_TALK005_SendMediaTaslet.class);
    private SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    @Autowired
    private ComConf comConf;

//    @Autowired
//    private ApplicationContext ac;

    @Autowired
    private CmBatchQueProcThreadService thrDB;

    @Autowired
    private TalkDBService tbDB;

    @Autowired
    private PushTalkProc push;


    @SuppressWarnings("unchecked")
    @Override
    public void execute(TasRequest tasRequest, TasResponse tasResponse) throws TasletException {

//        LogUtils pLog = new LogUtils();

        String extJsonStrH = "";

        try {

            procTion(null, tasRequest, tasResponse, extJsonStrH, -1);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            CommonUtils.setResponseHeader(tasRequest, tasResponse, Constants.TMT_RSLT_ERR_ETC);
//            if (GV.ComConf.CM_LogExtShowYN > 0 && pLog != null) pLog.endLog(SystemConstants.TMT_RSLT_ERR_ETC, SystemConstants.TMT_RSLT_ERR_ETC_MSG);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            if (extJsonStrH.trim().length() > 0) {
                extJsonStrH += "}";
            }
            tasResponse.setHeader("extJsonStrH", extJsonStrH);
//            if (GV.ComConf.CM_LogExtShowYN > 0 && pLog != null) pLog.getLog(log);
        }
    }

    protected void procTion(LogUtils pLog, TasRequest tasRequest, TasResponse tasResponse, String extJsonStrH, int senderId) throws Exception {

        String pushGroupKey = "";
        CaseMap imgInfoMap = new CaseMap();
        CaseMap sendInfo = null;

        short pushRslt = 0;
        short status = 0;
        int seq = 0;
        int expireTime = 0;
        List<TasBean> memberList = null;

        short roomType = tasRequest.getBody("roomType", short.class);
        boolean IS_MYTALK = roomType == GV.ComCost.P2P_TalkRoomType_MYTALK;

        boolean IS_SENDERID = senderId > 0;
        if (!IS_SENDERID) memberList = tasRequest.getBody("memberList", List.class);

        String groupCoCd = tasRequest.getHeader("groupCoCd", String.class).trim();
        int userIdnfr = tasRequest.getHeader("userIdnfr", Integer.class);
        int deviceIdnfr = tasRequest.getHeader("deviceIdnfr", Integer.class);

        // 발신자 정보를 확인.
        TasletMap<String, Object> tasletMap = new TasletMap<String, Object>();
        tasletMap.put("userIdnfr", userIdnfr);
        tasletMap.put("deviceIdnfr", deviceIdnfr);
        tasletMap.put("OS_TYPE", tasRequest.getPlatformHeader("OS_TYPE").toString().trim());
        tasletMap.put("TRANSACTION_ID", tasRequest.getPlatformHeader("TRANSACTION_ID").toString().trim());

        sendInfo = thrDB.ckAuthUser(tasletMap, null, this.getClass().getSimpleName(), "대화방 메시지 전달 요청", true);
        status = sendInfo.getShort("status");

        if (status == 0) {

            if (comConf.CM_TdvUseYN) {
                extJsonStrH = "{\"" + GV.ComCost.TdvUseYN + "\":\"Y\"";
            } else {
                extJsonStrH = "{\"" + GV.ComCost.TdvUseYN + "\":\"N\"";
            }

            int roomId = tasRequest.getBody("roomId", Integer.class);
            int talkId = tasRequest.getBody("talkId", Integer.class);
            short fileType = tasRequest.getBody("fileType", Short.class);
            String fileName = tasRequest.getBody("fileName", String.class).trim();

            if (roomType == GV.ComCost.P2P_TalkRoomType_CNS0PC) {
                CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 10);
//                if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 10, "1:1 수신거부 사용자(PC 대화방은 파일전송할 수 없음!)");
                return;
            }

            if (fileName.length() < 1) {
                fileName = "Server_T005_" + comConf.CM_ServiceId + "_" + System.currentTimeMillis() + ".jpg";
            }

            String reqDownUrl = tasRequest.getBody("downUrl", String.class);

            if (roomType == GV.ComCost.P2P_TalkRoomType_SECURITY && tasRequest.getBody("extJsonStr", String.class) != null
                    && tasRequest.getBody("extJsonStr", String.class).trim().length() > 0) {
                expireTime = Integer.parseInt(tasRequest.getBody("extJsonStr", String.class));
            }

            // memberList 의 각 entry 는 조직원 또는 조직일 수 있음
            long nTime = System.currentTimeMillis();
            String sendDate = DateUtils.getFormattedDateMS(nTime);
            String expireDate = ""; // T.B.D

            // 5 초 이내 동일 메시지 발송 요청인 있는지 확인
            if (roomId < 1 && !IS_MYTALK) {
                roomId = thrDB.ckCreateRoomInfo(userIdnfr, roomType, GV.ComCost.P2P_CK_TIME_MS_RoomCreate002, memberList);
                if (roomId > -1) memberList.clear();
            }

            RoomInfo roomInfo = null;
            if (roomId < 1) {
                if (IS_MYTALK) {
                    roomInfo = tbDB.getMyTalkRoomInfo(userIdnfr, roomType, memberList);
                } else if (memberList != null && memberList.size() > 0) {
                    roomInfo = tbDB.getRoomInfoByMemberList(groupCoCd, userIdnfr, roomType, memberList, expireTime);

                    if (roomInfo.id < 0) {// memberList 사이즈가 0 이상 인데도 -1 이라면 대화상대 모두 앱 미설치자인 경우
                        CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 11);
//                        if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 11, "상대방에게 앱이 설치된 단말 없음..!");
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return;
                    }

                    if (roomInfo.userIdnfrSet.size() > comConf.P2P_RoomMaxMemberCnt + 1) { // 대화방 참여자 최대 인원수 초과
                        CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 50);
//                        if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 50, "대화방 참여자 최대 인원수 초과..!");
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return;
                    }

                } else { // roomId 가 -1 이며 memberList 의 size 도 0이므로 방정보를 생성 또는 확인할 수 없음
                    CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 1);
//                    if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 1, "roomId 가 -1 이며 memberList 의 size 도 0이므로 방정보를 생성 또는 확인할 수 없음..!");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            } else {
                if (memberList != null && memberList.size() > 0) { // roomId 를 지정하였으나 memberList 의 size 가 0이 아님
                    CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 2);
//                    if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 2, "roomId 를 지정하였으나 memberList 의 size 가 0이 아님..!");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
                CaseMap roomInfoMap = tbDB.getRoomInfo(roomId, userIdnfr);
                roomInfo = tbDB.getRoomInfoByRoomId(GV.ComCost.P2P_RoomMsgSend, groupCoCd, roomId, userIdnfr, roomInfoMap);
            }

            if (roomInfo.id <= 0 || StringUtils.isEmpty(roomInfo.name)) { // 방정보를 제공할 수 없음
                CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 3);
//                if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 3, "방정보를 제공할 수 없음, ..!");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            } else if (!roomInfo.userIdnfrSet.contains(userIdnfr)) {
                // 요청자가 해당 대화방에 맴버가 아니라면
                CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 13);
//                if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 13, "대화방 맴버 아님..!");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            String downloadUrl = null;

            // 2016.10.14 yckwon@tionsoft.co.kr  nice 일 경우, protocol 이 https 라 추가 조건 사항 추가.
            if ((fileType == Constants.FileType.FILE_TYPE_DOC && reqDownUrl.trim().length() > 1 && reqDownUrl.indexOf("http://") < 0 && reqDownUrl.indexOf("https://") < 0)
                    || reqDownUrl.indexOf("tmt://") == 0 || reqDownUrl.indexOf("fail://") == 0) {
                downloadUrl = tbDB.getAttFileInfoSeq(Integer.parseInt(reqDownUrl.replace("tmt://", "").replace("fail://", "")));
                log.info("BP2 " + downloadUrl);
            } else {
                downloadUrl = reqDownUrl;
            }

            if (tasletMap.getString("OS_TYPE").equalsIgnoreCase(GV.ComCost.Client_OS_PC)) {
                tasletMap.put("downloadUrl", downloadUrl.replaceAll(comConf.CM_FileBaseUrl, ""));
            } else {
                tasletMap.put("downloadUrl", downloadUrl);
            }

            CaseMap attFileInfo = tbDB.getAttFileInfo(tasletMap);

            imgInfoMap.put("sectionCd", "R");
            imgInfoMap.put("sectionSeq", roomInfo.id);

            /************ 낭중에 앨범 사용 기능 여부 확인 if 문 추가되어야함.***********************************************************/
            imgInfoMap.put("regId", userIdnfr);
            imgInfoMap.put("afSeq", attFileInfo.getInt("seq"));
            imgInfoMap.put("thumbUrl", attFileInfo.getString("thumbUrl"));
            imgInfoMap.put("sectionSeq", roomInfo.id);
            imgInfoMap.put("userThumbUrl", sendInfo.getString("photoUrl"));
            imgInfoMap.put("userCls", sendInfo.getString("userCls"));
            imgInfoMap.put("userName", sendInfo.getString("name"));
            imgInfoMap.put("position", sendInfo.getString("position"));
            imgInfoMap.put("department", sendInfo.getString("deptName"));

            /*******************************************************************************************************************************/
            imgInfoMap.put("fileName", attFileInfo.getString("fileName"));
            imgInfoMap.put("fileExt", attFileInfo.getString("fileExt"));
            imgInfoMap.put("fileSize", attFileInfo.getInt("fileSize"));
            imgInfoMap.put("fileType", attFileInfo.getString("fileType"));
            imgInfoMap.put("fileAbsPath", attFileInfo.getString("fileAbsPath"));
            imgInfoMap.put("fileUrl", attFileInfo.getString("fileUrl"));
            imgInfoMap.put("downUrl", attFileInfo.getString("fileUrl").replace("fail://", "tmt://"));

            String mediaPath = comConf.CM_FileBasePath + comConf.CM_FileMediaPath;
            String mediaUrl = comConf.CM_FileBaseUrl + comConf.CM_FileMediaPath;
            imgInfoMap.put("pcDownUrl", attFileInfo.getString("fileAbsPath").replace(mediaPath, mediaUrl));
            imgInfoMap.put("type", Constants.FileType.toPushType(fileType));

            seq = tbDB.insertAttcFile(imgInfoMap);

            imgInfoMap.put("seq", seq);

            /** 1:1 대화에서 자동응답 메시지가 있다면 자동응답 메시지 설정 처리 ** 시작 **/
            Object[] mList = roomInfo.userIdnfrSet.toArray();
            if (roomInfo.userIdnfrSet.size() == 2 && !IS_MYTALK) {    // 대화방에 상대방이 1명 뿐인 경우 상대방 자동응답메시지 확인. && roomInfo.typeOnCreation == GV.ComCost.P2P_TalkType_P_TO_P
                String replyMsg = "";
                if (userIdnfr != (Integer) mList[0]) replyMsg = tbDB.getAutoReplyMsg((Integer) mList[0]);
                else replyMsg = tbDB.getAutoReplyMsg((Integer) mList[1]);

                if (replyMsg != null && replyMsg.trim().length() > 0) {
                    extJsonStrH += ",\"" + GV.ComCost.AutoReply + "\":\"" + replyMsg + "\"";
                }
            }

            /** 1:1 대화에서 자동응답 메시지가 있다면 자동응답 메시지 설정 처리 ** 끝 **/

            roomInfo.userIdnfrSet.remove(userIdnfr); // 나 자신은 뺀다.

            // 20151007 kwlee 로그아웃한 사용자에게 Push 발송되는 이슈 처리
            Set<Integer> idSet = null;
            if (roomInfo.id > 0 && roomInfo.roomType == 1) {
                idSet = roomInfo.userIdnfrSet;
                log.info("TALK005 BPPP4" + idSet.toString());
            } else {
                idSet = tbDB.chkInstallStatusWithUserIdnfrSet(roomInfo.userIdnfrSet);
                log.info("TALK005 BPPP5" + idSet.toString());
            }

            // 로그인된 단말이 있는 사용자가 한명도 없다면 에러코드 11번 발
            if (idSet.size() == 0 && !IS_MYTALK) {
                CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 11);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 11, "상대방에게 앱이 설치된 단말 없음..!");
                return;
            }

            imgInfoMap.put("recvUserIdnfr", "," + roomInfo.userIdnfrSet.toString().substring(1, roomInfo.userIdnfrSet.toString().length() - 1).replaceAll(" ", "") + ",");

            pushGroupKey = comConf.CM_PushPcApplicationName + "-" + roomInfo.id + "-" + userIdnfr + "-" + deviceIdnfr + "-" + nTime;
            imgInfoMap.put("pushGroupKey", pushGroupKey);
            toTalkMediaJsonMap(sendInfo, imgInfoMap, roomInfo, talkId, fileType, fileName, sendDate, expireDate);

            CaseMap sender = null;
            if (IS_SENDERID) {
                sender = tbDB.getSenderInfo(senderId);
            }

            pushRslt = push.pushTalkText(IS_SENDERID ? sender : sendInfo, roomInfo, imgInfoMap, IS_MYTALK);

//            if (GV.ComConf.CM_LogExtShowYN > 0) pLog.procLog("대화방 첨부파일 push 발송..!(" + pushRslt + ")");

            if (pushRslt != 0 && !IS_MYTALK && comConf.P2P_SendMsgOffLineYN < 0) {
                CommonUtils.setResponseHeader(tasRequest, tasResponse, pushRslt);
//                if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog(pushRslt, "대화방 첨부파일 push 발송 실패..!");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            if (roomId < 0 && !IS_MYTALK) {    // 대화방 생성진행 큐에 지금 등록을 했다면 생성된 대화방 정보 갱신.
                thrDB.upCreateRoomInfo(userIdnfr, roomType, memberList, roomInfo.id);
//                if (GV.ComConf.CM_LogExtShowYN > 0) pLog.procLog("생성된 대화방 정보 갱신.");
            }

            imgInfoMap.put("serverID", comConf.CM_ServerName);
            imgInfoMap.put("pushSendState", 1);

            if (IS_SENDERID) {
                imgInfoMap.put("userIdnfr", senderId);
                imgInfoMap.put("userName", sender.getString("name"));
                imgInfoMap.put("position", sender.getString("position"));
                imgInfoMap.put("department", sender.getString("deptName"));
            } else {
                imgInfoMap.put("userIdnfr", userIdnfr);
                imgInfoMap.put("userName", sendInfo.getString("name"));
                imgInfoMap.put("position", sendInfo.getString("position"));
                imgInfoMap.put("department", sendInfo.getString("deptName"));
            }

            imgInfoMap.put("roomId", roomInfo.id);
            imgInfoMap.put("talkId", talkId);
            imgInfoMap.put("msg", "");
            imgInfoMap.put("pushGroupKey", pushGroupKey);

            imgInfoMap.put("readCnt", roomInfo.userIdnfrSet.size());
            imgInfoMap.put("sendDate", dt.format(nTime));

//            if (GV.ComConf.CM_LogExtShowYN > 0) pLog.procLog("DB Talk log");

            CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 0);
            tasResponse.setBody("roomId", roomInfo.id);
            tasResponse.setBody("roomName", roomInfo.name);
            tasResponse.setBody("roomLeader", roomInfo.leader);
            tasResponse.setBody("roomType", (short) roomInfo.roomType);
            tasResponse.setBody("memberType", (short) roomInfo.typeOnCreation);
            tasResponse.setBody("talkId", talkId);
            tasResponse.setBody("thumbnailUrl", imgInfoMap.getString("thumbUrl"));
            tasResponse.setBody("downloadUrl", tasletMap.getString("OS_TYPE").equals(GV.ComCost.Client_OS_PC) ? imgInfoMap.getString("pcDownUrl") : imgInfoMap.getString("downUrl"));
            tasResponse.setBody("sendDate", sendDate);
            tasResponse.setBody("userIdnfrList", toTasBean(roomInfo.userIdnfrSet));
            if (expireTime != 0) {
                tasResponse.setBody("extJsonStr", "{\"expireTime\":" + expireTime + ",\"expireDate\":\"" + expireDate + "\"}");
            } else {
                tasResponse.setBody("extJsonStr", "{\"expireDate\":\"" + expireDate + "\"}");
            }

            thrDB.putTalkMsgLog(imgInfoMap);

//            if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 0, SystemConstants.TMT_RSLT_SUCCESS_MSG);
        } else {
            if (status == Constants.TMT_RSLT_ERR_NODATA_USER) pLog = null;
            CommonUtils.setResponseHeader(tasRequest, tasResponse, status);
        }
    }

    private void toTalkMediaJsonMap(CaseMap senderInfo, CaseMap jsonMap, RoomInfo roomInfo, int talkId, short fileType, String orgFileName, String sendDate, String expireDate) {

        jsonMap.put("ver", "1.0");
        jsonMap.put("sendDate", sendDate);
        jsonMap.put("expireDate", expireDate);
        jsonMap.put("talkId", talkId);
        jsonMap.put("orgFileName", orgFileName);
        jsonMap.put("message-key", Constants.FileType.toMsgKey(fileType));
    }

//    private File copyToMediaDirectory(File srcFile, String srcFileExt) {
//        try {
//            File destFile = null;
//            String subPath = DateUtils.getFormattedDate(new Date(), "yyyyMMdd");
//            if (srcFileExt != null && srcFileExt.length() > 1) {
//                destFile = new File(GV.ComConf.CM_FileBasePath + "/" + GV.ComConf.CM_FileMediaPath + "/" + subPath + "/" + System.currentTimeMillis() + "." + srcFileExt.toLowerCase());
//            } else {
//                destFile = new File(GV.ComConf.CM_FileBasePath + "/" + GV.ComConf.CM_FileMediaPath + "/" + subPath + "/" + srcFile.getName());
//            }
//            log.debug(">>>>>>>>>>>>>>>>>>>>>>>> copyToMediaDirectory path: " + destFile.getAbsolutePath());
//            FileUtils.copyFile(srcFile, destFile);
//            return destFile;
//        } catch (Exception e) {
//            log.error(">>>>>>>>>>>>>>>>>>>>>>>>>> copyToMediaDirectory >>>>>>>>>>>>>>>>>>\n", e);
//            return null;
//        }
//    }

//    private String convertMedia(File srcFile, String srcFileDownloadUrl) {
//        McsClient mcsClient = ac.getBean(McsClient.class);
//        String orgMediaUrl = mcsClient.convertMedia(srcFile, srcFileDownloadUrl);
//
//        return orgMediaUrl;
//    }

//    private List<CaseMap> convertDoc(File srcFile, String srcFileDownloadUrl, String fileSeq) {
//        McsClient mcsClient = ac.getBean(McsClient.class);
//        List<CaseMap> list = mcsClient.responsePdv(srcFile, srcFileDownloadUrl, fileSeq);
//
//        return list;
//    }

//    private String makeThumbnailUrl(File srcFile) {
//        try {
//            String subPath = GV.ComConf.CM_FileBasePath + "/" + GV.ComConf.CM_FileThumbnailPath + "/" + DateUtils.getFormattedDate(new Date(), "yyyyMMdd");
//            FileUtils.forceMkdir(new File(subPath));
//            File thumbnailFile = new File(subPath + "/" + FilenameUtils.getBaseName(srcFile.getName()) + ".png");
//            BufferedImage original = ImageIO.read(srcFile);
//            BufferedImage thumbnail = Scalr.resize(original, Scalr.Method.SPEED, GV.ComConf.P2P_AttFileThumbnailImgW, GV.ComConf.P2P_AttFileThumbnailImgH, Scalr.OP_ANTIALIAS);
//            ImageIO.write(thumbnail, "png", thumbnailFile);
//            String osDependentFileRelPath = StringUtils.replace(thumbnailFile.getCanonicalPath(), FilenameUtils.separatorsToSystem(GV.ComConf.CM_FileBasePath), "");
//            String unixTypeFileRelPath = FilenameUtils.separatorsToUnix(osDependentFileRelPath);
//
//            return GV.ComConf.CM_FileBaseUrl + unixTypeFileRelPath;
//        } catch (Exception e) {
//            return null;
//        }
//    }

    private List<TasBean> toTasBean(Set<Integer> idSet) {
        List<TasBean> userIdnfrList = new ArrayList<TasBean>();
        for (int userIdnfr : idSet) {
            TasBean bean = new TasBean();
            bean.setValue("id", userIdnfr);
            userIdnfrList.add(bean);
        }

        return userIdnfrList;
    }


//    private boolean isTimeout(LogUtils pLog, TasRequest tasRequest, TasResponse tasResponse, long lStartTime, int nTimeout) {    // check current time & exit
//        long lCurrTime = System.currentTimeMillis();    // curr of time
//
//        if ((lCurrTime - lStartTime) > nTimeout) {
//            CommonUtils.setResponseHeader(tasRequest, tasResponse, (short) 20);
//            if (GV.ComConf.CM_LogExtShowYN > 0) pLog.endLog((short) 20, "처리 Timeout !!");
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return true;
//        }
//
//        return false;
//    }
}