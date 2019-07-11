package cn.mst.aop;

import cn.mst.client.holder.UUIDHolder;
import cn.mst.utils.WebUtils;
import cn.mst.constant.TransferConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @ClassName FeginRequestInterceptor
 * @Author buchengyin
 * @Date 2018/12/19 14:56
 **/
@Component
public class FeignRequestAop implements RequestInterceptor {
    private Logger logger = LoggerFactory.getLogger(FeignRequestAop.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = UUIDHolder.getUUID();
        if (token != null) {
            logger.debug(" send remote service token:"+token);
            requestTemplate.header(TransferConstant.MST_TOKEN, token);
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
