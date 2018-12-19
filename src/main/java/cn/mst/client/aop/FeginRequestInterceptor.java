package cn.mst.client.aop;

import cn.mst.client.base.MstAttributeHolder;
import cn.mst.client.constant.SystemConstant;
import cn.mst.common.WebUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @ClassName FeginRequestInterceptor
 * @Author buchengyin
 * @Date 2018/12/19 14:56
 **/
public class FeginRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = MstAttributeHolder.getMstToken();
        if (token != null) {
            requestTemplate.header(SystemConstant.MST_TOKEN, token);
        }

        HttpServletRequest request = WebUtils.getRequest();
        if (request == null)
            return;
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = WebUtils.getRequest().getHeader(name);
                requestTemplate.header(name, value);
            }
        }
    }
}
