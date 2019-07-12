package cn.bucheng.registry;

import java.util.Map;

public interface ServiceInstance {

	/**
	 * @return the service id as registered.
	 */
	String getServiceId();

	/**
	 * @return the hostname of the registered ServiceInstance
	 */
	String getHost();

	/**
	 * @return the port of the registered ServiceInstance
	 */
	int getPort();


	/**
	 * @return the key value pair metadata associated with the service instance
	 */
	Map<String, String> getMetadata();

	/**
	 * @return the scheme of the instance
	 */
	default String getScheme() {
		return null;
	}
}