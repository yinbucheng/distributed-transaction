package cn.bucheng.common.utils;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author buchengyin
 * @create 2019/8/4 20:28
 * @describe
 */
public abstract class WebUtils {

    private static ServletRequestAttributes getAttributes(){
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static HttpServletResponse getResponse(){
        return getAttributes().getResponse();
    }

    public static HttpServletRequest getRequest(){
        return getAttributes().getRequest();
    }

}
