package com.example.todo;

// ohos相关接口包
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.content.Intent;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.javax.xml.transform.Result;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.MessageOption;
import ohos.utils.net.Uri;
import ohos.utils.zson.ZSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAbility extends Ability {
    // 定义日志标签
    private static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0, "MY_TAG");
    private DataAbilityHelper databaseHelper;

    private static final String BASE_URI = "dataability:///com.example.todo.TodoDataAbility";
    private static final String DATA_PATH = "/todo";
    private static final String DB_COLUMN_PERSON_IDX = "idx";
    private static final String DB_COLUMN_TITLE = "title";
    private static final String DB_COLUMN_DESCRIPTION = "description";
    private static final String[] DB_COLUMN = { DB_COLUMN_PERSON_IDX, DB_COLUMN_TITLE, DB_COLUMN_DESCRIPTION};

    private MyRemote remote = new MyRemote();
    // FA在请求PA服务时会调用Ability.connectAbility连接PA，连接成功后，需要在onConnect返回一个remote对象，供FA向PA发送消息
    public class TODO {
        public int idx;
        public String title;
        public String description;
    }
    @Override
    protected IRemoteObject onConnect(Intent intent) {
        super.onConnect(intent);
        return remote.asObject();
    }

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        databaseHelper = DataAbilityHelper.creator(this);
    }
    class MyRemote extends RemoteObject implements IRemoteBroker {
        private static final int SUCCESS = 0;
        private static final int ERROR = 1;
        private static final int SELECT = 1001;
        private static final int INSERT = 1002;
        private static final int DELETE = 1003;
        private static final int UPDATE = 1004;
        private static final int CONTINUE = 1005;

        MyRemote() {
            super("MyService_MyRemote");
        }

        @Override
        public boolean onRemoteRequest(int code, MessageParcel data, MessageParcel reply, MessageOption option) {
            switch (code) {
                case SELECT: {
                    try {
                        Map<String, Object> result = new HashMap<String, Object>();
                        DataAbilityPredicates predicates = new DataAbilityPredicates();
                        ResultSet resultSet = databaseHelper.query(Uri.parse(BASE_URI+DATA_PATH), DB_COLUMN, predicates);
                        HiLog.info(LABEL,"SELECT");
                        if (resultSet == null || resultSet.getRowCount() == 0) {
                            HiLog.info(LABEL, "query: resultSet is null or no result found");
                            result.put("code", ERROR);
                            result.put("abilityResult", resultSet);
                            reply.writeString(ZSONObject.toZSONString(result));
                            return true;
                        }
                        resultSet.goToFirstRow();
                        List<TODO> newTODO= new ArrayList<TODO>();
                        do {
                            int idx = resultSet.getInt(resultSet.getColumnIndexForName(DB_COLUMN_PERSON_IDX));
                            String title = resultSet.getString(resultSet.getColumnIndexForName(DB_COLUMN_TITLE));
                            String description = resultSet.getString(resultSet.getColumnIndexForName(DB_COLUMN_DESCRIPTION));
                            TODO list_item = new TODO();
                            list_item.idx = idx;list_item.title = title;list_item.description = description;
                            newTODO.add(list_item);
                            HiLog.info(LABEL, "query: Idx :" + idx + " Title :" + title + " Description :" + description);
                        } while (resultSet.goToNextRow());
                        HiLog.info(LABEL, "select success!", resultSet);
                        result.put("code", SUCCESS);
                        result.put("abilityResult", newTODO);
                        reply.writeString(ZSONObject.toZSONString(result));
                    } catch (DataAbilityRemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case DELETE: {
                    String dataStr = data.readString();
                    RequestParam param = new RequestParam();
                    try {
                        param = ZSONObject.stringToClass(dataStr, RequestParam.class);
                    } catch (RuntimeException e) {
                        HiLog.error(LABEL, "convert failed.");
                    }
                    Map<String, Object> result = new HashMap<String, Object>();
                    DataAbilityPredicates predicates = new DataAbilityPredicates().equalTo(DB_COLUMN_PERSON_IDX, param.idx);
                    try {
                        if (databaseHelper.delete(Uri.parse(BASE_URI + DATA_PATH), predicates) != -1) {
                            HiLog.info(LABEL, "delete successful");
                            result.put("code", SUCCESS);
                            result.put("abilityResult", param.idx);
                            reply.writeString(ZSONObject.toZSONString(result));
                        }
                        break;
                    } catch (DataAbilityRemoteException | IllegalStateException exception) {
                        HiLog.error(LABEL, "delete: dataRemote exception | illegalStateException");
                        result.put("code", ERROR);
                        reply.writeString(ZSONObject.toZSONString(result));
                    }
                    break;
                }
                case INSERT: {
                    String dataStr = data.readString();
                    RequestParam param = new RequestParam();
                    try {
                        param = ZSONObject.stringToClass(dataStr, RequestParam.class);
                    } catch (RuntimeException e) {
                        HiLog.error(LABEL, "convert failed.");
                    }
                    ValuesBucket valuesBucket = new ValuesBucket();
                    valuesBucket.putInteger(DB_COLUMN_PERSON_IDX, param.idx);
                    valuesBucket.putString(DB_COLUMN_TITLE, param.title);
                    valuesBucket.putString(DB_COLUMN_DESCRIPTION, param.description);
                    HiLog.info(LABEL, "inserting...",valuesBucket);
                    Map<String, Object> result = new HashMap<String, Object>();
                    try {
                        if (databaseHelper.insert(Uri.parse(BASE_URI + DATA_PATH), valuesBucket) != -1) {
                            HiLog.info(LABEL, "insert successful");
                            result.put("code", SUCCESS);
                            result.put("abilityResult", param.title);
                            reply.writeString(ZSONObject.toZSONString(result));
                        }
                    } catch (DataAbilityRemoteException | IllegalStateException exception) {
                        HiLog.error(LABEL, "insert: dataRemote exception|illegalStateException");
                        result.put("code", ERROR);
                        reply.writeString(ZSONObject.toZSONString(result));
                    }
                    break;
                }
                case UPDATE: {
                    String dataStr = data.readString();
                    RequestParam param = new RequestParam();
                    try {
                        param = ZSONObject.stringToClass(dataStr, RequestParam.class);
                    } catch (RuntimeException e) {
                        HiLog.error(LABEL, "convert failed.");
                    }
                    ValuesBucket valuesBucket = new ValuesBucket();
                    valuesBucket.putInteger(DB_COLUMN_PERSON_IDX, param.idx);
                    valuesBucket.putString(DB_COLUMN_TITLE, param.title);
                    valuesBucket.putString(DB_COLUMN_DESCRIPTION, param.description);

                    DataAbilityPredicates predicates = new DataAbilityPredicates();
                    predicates.equalTo(DB_COLUMN_PERSON_IDX, param.idx);
                    Map<String, Object> result = new HashMap<String, Object>();
                    try {
                        if (databaseHelper.update(Uri.parse(BASE_URI + DATA_PATH), valuesBucket, predicates) != -1) {
                            HiLog.info(LABEL, "update successful");
                            result.put("code", SUCCESS);
                            result.put("abilityResult", param.idx);
                            reply.writeString(ZSONObject.toZSONString(result));
                        }
                    } catch (DataAbilityRemoteException | IllegalStateException exception) {
                        HiLog.error(LABEL, "update: dataRemote exception | illegalStateException");
                        result.put("code", ERROR);
                    }
                    break;
                }
                default: {
                    Map<String, Object> result = new HashMap<String, Object>();
                    result.put("abilityError", ERROR);
                    reply.writeString(ZSONObject.toZSONString(result));
                    return false;
                }
            }
            return true;
        }

        @Override
        public IRemoteObject asObject() {
            return this;
        }
    }
}