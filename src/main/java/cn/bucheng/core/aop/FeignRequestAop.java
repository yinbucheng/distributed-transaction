package cn.bucheng.core.aop;

import cn.bucheng.common.utils.WebUtils;
import cn.bucheng.core.holder.XIDHolder;
import cn.bucheng.constant.TransferConstant;
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
@SuppressWarnings("all")
public class FeignRequestAop implements RequestInterceptor {
    private Logger logger = LoggerFactory.getLogger(FeignRequestAop.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = XIDHolder.getXID();
        if (token != null) {
            logger.debug(" send remote service token:"+token);
            requestTemplate.header(TransferConstant.XID_TOKEN, token);
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
