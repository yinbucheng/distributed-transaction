package cn.mst.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MstMessageBuilder
 * @Author buchengyin
 * @Date 2018/12/19 15:52
 **/
public class MstMessageBuilder {

    public static final int PING =0;
    public static final int PONG =1;
    public static final int REGISTER =2;
    public static final int ROLLBACK =3;
    public static final int COMMIT =4;
    public static final int FIN =5;
    public static final int REGISTER_OK =6;

    public static String ping(){
       return createMessage(PING,null);
    }

    public static String pong(){
        return createMessage(PONG,null);
    }

    public static String sendRegister(String token){
        return createMessage(REGISTER,token);
    }

    public static String sendRollback(String token){
        return createMessage(ROLLBACK,token);
    }

    public static String sendCommit(String token){
        return createMessage(COMMIT,token);
    }

    public static String registerOk(String token){
        return createMessage(REGISTER_OK,token);
    }


    public static String sendFIN(String token){
        return createMessage(FIN,token);
    }

    private static String createMessage(int state,String content){
       return "{\"state\":"+state+",\"token\":\""+content+"\"}";
    }

    public static Map<Integer,String> resolverMessage(String content){
        if(content==null||content.equals(""))
            throw new RuntimeException("content error");
        JSONObject parse = JSON.parseObject(content);
        Integer state = parse.getInteger("state");
        String token = parse.getString("token");
        Map<Integer,String> map = new HashMap<>();
        map.put(state,token);
        return map;
    }
}
