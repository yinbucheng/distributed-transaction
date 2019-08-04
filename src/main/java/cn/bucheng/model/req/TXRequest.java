package cn.bucheng.model.req;

import cn.bucheng.common.constant.TransferConstant;

import java.io.Serializable;

/**
 * @author ：yinchong
 * @create ：2019/7/11 12:38
 * @description：
 * @modified By：
 * @version:
 */
public class TXRequest implements Serializable {
    private String uuid;
    private Integer type;

    public Integer getType() {
        return type;
    }

    public TXRequest() {
    }

    public TXRequest(String uuid, Integer type) {
        this.uuid = uuid;
        this.type = type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public static TXRequest commitTx(String uuid) {
        return new TXRequest(uuid, TransferConstant.COMMIT);
    }

    public static TXRequest rollbackTx(String uuid) {
        return new TXRequest(uuid, TransferConstant.ROLLBACK);
    }

    public static TXRequest registerTx(String uuid) {
        return new TXRequest(uuid, TransferConstant.REGISTER);
    }

    public static TXRequest finalTx(String uuid){
        return new TXRequest(uuid,TransferConstant.FIN);
    }
}
