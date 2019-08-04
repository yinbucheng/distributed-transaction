package cn.bucheng.model.res;

import cn.bucheng.common.constant.TransferConstant;

import java.io.Serializable;

/**
 * @author ：yinchong
 * @create ：2019/7/11 12:38
 * @description：
 * @modified By：
 * @version:
 */
public class TXResponse implements Serializable {
    private String uuid;
    private Integer status;

    public TXResponse() {
    }

    public TXResponse(String uuid, Integer status) {
        this.uuid = uuid;
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    //这里表示接受到服务端消息
    public static TXResponse ack(String uuid){
        return new TXResponse(uuid, TransferConstant.ACK);
    }

    public static TXResponse commit(String uuid){
        return new TXResponse(uuid,TransferConstant.COMMIT);
    }

    public static TXResponse rollback(String uuid){
        return new TXResponse(uuid,TransferConstant.ROLLBACK);
    }


}
