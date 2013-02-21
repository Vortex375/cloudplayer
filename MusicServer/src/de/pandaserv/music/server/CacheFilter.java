package de.pandaserv.music.server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * set filter headers according to file names
 *
 * if a file name contains "nocache" set no-cache headers
 * if a file name contains "cache" set cache header to cache file forever
 * otherwise do nothing
 */
public class CacheFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            if (req.getPathInfo() != null && req.getPathInfo().contains(".cache.")) {
                res.setHeader("Cache-Control", "max-age=31556926");

            } else if (req.getPathInfo() != null && req.getPathInfo().contains(".nocache.")) {
                res.setHeader("Cache-Control", "no-cache");
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
