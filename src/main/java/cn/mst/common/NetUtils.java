package cn.mst.common;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetUtils {
    public static InetUtilsProperties properties =  new InetUtilsProperties();

    public static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface
                    .getNetworkInterfaces(); nics.hasMoreElements();) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    }
                    else if (result != null) {
                        continue;
                    }
                    if (!ignoreInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc
                                .getInetAddresses(); addrs.hasMoreElements();) {
                            InetAddress address = addrs.nextElement();
                            if (address instanceof Inet4Address
                                    && !address.isLoopbackAddress()
                                    && isPreferredAddress(address)) {
                                result = address;
                            }
                        }
                    }
                }
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            System.err.println(e);
        }

        return null;
    }

    public static boolean ignoreInterface(String interfaceName) {
        for (String regex : properties.getIgnoredInterfaces()) {
            if (interfaceName.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPreferredAddress(InetAddress address) {

        if (properties.isUseOnlySiteLocalInterfaces()) {
            final boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
            }
            return siteLocalAddress;
        }
        final List<String> preferredNetworks = properties.getPreferredNetworks();
        if (preferredNetworks.isEmpty()) {
            return true;
        }
        for (String regex : preferredNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        return false;
    }

    static public class InetUtilsProperties {

        /**
         * The default hostname. Used in case of errors.
         */
        private String defaultHostname = "localhost";

        /**
         * The default ipaddress. Used in case of errors.
         */
        private String defaultIpAddress = "127.0.0.1";

        private int timeoutSeconds = 1;

        /**
         * List of Java regex expressions for network interfaces that will be ignored.
         */
        private List<String> ignoredInterfaces = new ArrayList<>();


        private boolean useOnlySiteLocalInterfaces = false;

        /**
         * List of Java regex expressions for network addresses that will be preferred.
         */
        private List<String> preferredNetworks = new ArrayList<>();


        public String getDefaultHostname() {
            return defaultHostname;
        }

        public void setDefaultHostname(String defaultHostname) {
            this.defaultHostname = defaultHostname;
        }

        public String getDefaultIpAddress() {
            return defaultIpAddress;
        }

        public void setDefaultIpAddress(String defaultIpAddress) {
            this.defaultIpAddress = defaultIpAddress;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public List<String> getIgnoredInterfaces() {
            return ignoredInterfaces;
        }

        public void setIgnoredInterfaces(List<String> ignoredInterfaces) {
            this.ignoredInterfaces = ignoredInterfaces;
        }

        public boolean isUseOnlySiteLocalInterfaces() {
            return useOnlySiteLocalInterfaces;
        }

        public void setUseOnlySiteLocalInterfaces(boolean useOnlySiteLocalInterfaces) {
            this.useOnlySiteLocalInterfaces = useOnlySiteLocalInterfaces;
        }

        public List<String> getPreferredNetworks() {
            return preferredNetworks;
        }

        public void setPreferredNetworks(List<String> preferredNetworks) {
            this.preferredNetworks = preferredNetworks;
        }
    }

}