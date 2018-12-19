package cn.mst.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName WebUtils
 * @Author buchengyin
 * @Date 2018/12/19 15:01
 **/
public class WebUtils {
    private static ServletRequestAttributes getAttributes(){
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    public static HttpServletRequest getRequest(){
        return getAttributes().getRequest();
    }

    public static HttpServletResponse getResponse(){
        return getAttributes().getResponse();
    }

}
