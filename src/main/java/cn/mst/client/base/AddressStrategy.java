package cn.mst.client.base;

import cn.mst.client.constant.SystemConstant;
import cn.mst.common.ZKUtils;

/**
 * 地址解析策略
 * @ClassName AddressStrategy
 * @Author buchengyin
 * @Date 2018/12/20 10:56
 **/
public class AddressStrategy {

    /**
     * 从Zookeeper中解析出服务端的地址
     * @param namespace
     * @return
     */
    public static Object[] resolveIpAndPort(String namespace){
       boolean flag = ZKUtils.exist(MstAttributeHolder.getZkClient(),"/"+ SystemConstant.ROOT_PATH+"/"+namespace+"/master");
       if(!flag){
           return null;
       }
       String content = ZKUtils.getData(MstAttributeHolder.getZkClient(),"/"+ SystemConstant.ROOT_PATH+"/"+namespace+"/master");
       String[] temps = content.split(":");
       Integer data = Integer.parseInt(temps[1]);
       Object[] results = new Object[2];
       results[0]=temps[0];
       results[1]=data;
       return results;
    }
}
