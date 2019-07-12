package cn.mst.common;

import java.io.Serializable;

/**
 * @author ：yinchong
 * @create ：2019/7/12 16:27
 * @description：注册的协议
 * @modified By：
 * @version:
 */
public class URL implements Serializable {
    //注册采用的协议zookeeper，eureka，redis等
    private  String protocol;
    //注册使用的账号
    private  String username;
    //注册使用的密码
    private  String password;
    //注册使用的域名
    private  String hostName;
    //注册使用的端口
    private  int port;
    //组成的路径
    private  String path;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
