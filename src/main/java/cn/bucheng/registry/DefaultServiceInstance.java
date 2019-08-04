package cn.bucheng.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultServiceInstance implements ServiceInstance {

    private final String serviceId;

    private final String host;

    private final int port;

    private final String version;

    private final int state;

    private final String leaderName;

    private final Map<String, String> metadata;

    @Override
    public String getVersion() {
        return version;
    }


    @Override
    public int getState() {
        return state;
    }

    public DefaultServiceInstance(String serviceId, String host, int port, String version, int state,String leaderName,
                                  Map<String, String> metadata) {
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.version = version;
        this.metadata = metadata;
        this.state = state;
        this.leaderName = leaderName;
    }

    public DefaultServiceInstance(String serviceId, String host, int port,
                                  String version, int state,String leaderName) {
        this(serviceId, host, port, version, state, leaderName,new LinkedHashMap<String, String>());
    }

    @Override
    public String getLeaderName() {
        return leaderName;
    }

    @Override
    public Map<String, String> getMetadata() {
        return this.metadata;
    }


    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }


    @Override
    public String toString() {
        return "DefaultServiceInstance{" +
                "serviceId='" + serviceId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", version=" + version +
                ", state=" + state +
                ", leaderName=" + leaderName +
                ", metadata=" + metadata +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultServiceInstance that = (DefaultServiceInstance) o;
        return port == that.port &&
                version == that.version &&
                Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(host, that.host) && Objects.equals(state, that.state) &&
                Objects.equals(leaderName,that.getLeaderName())&&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, host, port, version, metadata);
    }
}
