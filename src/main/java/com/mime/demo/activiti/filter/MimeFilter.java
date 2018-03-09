package com.mime.demo.activiti.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author SlumDuck
 * @create 2018-03-09 10:47
 * @desc
 */
@WebFilter
public class MimeFilter implements Filter{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        String ignorePath = httpServletRequest.getServletPath();
        filterChain.doFilter(servletRequest,servletResponse);
        System.out.println(httpServletRequest.getContextPath());
        /*
        if(ignorePath.contains("model.html") || ignorePath.contains("index")){
            return;
        }else {
            filterChain.doFilter(servletRequest,servletResponse);
        }
        */
    }

    @Override
    public void destroy() {

    }
}
