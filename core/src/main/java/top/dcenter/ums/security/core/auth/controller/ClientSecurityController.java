package top.dcenter.ums.security.core.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.dcenter.ums.security.core.api.controller.BaseSecurityController;
import top.dcenter.ums.security.common.enums.ErrorCodeEnum;
import top.dcenter.ums.security.core.exception.IllegalAccessUrlException;
import top.dcenter.ums.security.core.auth.properties.ClientProperties;
import top.dcenter.ums.security.core.util.MvcUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static top.dcenter.ums.security.core.util.MvcUtil.getServletContextPath;

/**
 * 客户端 url 认证与授权的路由控制.<br><br>
 * 如果要自定义客户端 url 认证与授权的路由控制，请实现 {@link BaseSecurityController} 接口，并注入 IOC 容器即可
 *
 * @author zhailiang
 * @author zyw
 * @version V1.0  Created by 2020/5/3 17:43
 */
@Slf4j
@ResponseBody
public class ClientSecurityController implements BaseSecurityController, InitializingBean {

    /**
     * url regex
     */
    public static final String URL_REGEX = "^.*://[^/]*(/.*$)";
    /**
     * 相对于 URL_REGEX 的括号部分
     */
    public static final String URI_$1 = "$1";

    private final RequestCache requestCache;
    private final RedirectStrategy redirectStrategy;
    private final ClientProperties clientProperties;
    private final AntPathMatcher pathMatcher;
    /**
     * Map&#60;requestUri, loginUri&#62;
     */
    private final Map<String, String> authRedirectUrls;

    @Autowired
    private GenericApplicationContext applicationContext;

    public ClientSecurityController(ClientProperties clientProperties) {
        this.clientProperties = clientProperties;
        this.requestCache = new HttpSessionRequestCache();
        this.redirectStrategy = new DefaultRedirectStrategy();
        authRedirectUrls = clientProperties
                .getAuthRedirectSuffixCondition()
                .stream()
                .map(pair -> pair.split("="))
                .collect(toMap(arr -> arr[0], arr -> arr[1]));
        pathMatcher = new AntPathMatcher();
    }

    @Override
    @RequestMapping(value = {"${ums.client.loginUnAuthenticationRoutingUrl}"})
    public void requireAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try
        {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null)
            {
                String targetUrl = savedRequest.getRedirectUrl();

                if (StringUtils.isNotBlank(targetUrl))
                {
                    targetUrl = targetUrl.replaceFirst(URL_REGEX, URI_$1);
                    String contextPath = request.getServletContext().getContextPath();
                    targetUrl = StringUtils.substringAfter(targetUrl, contextPath);

                    Iterator<Map.Entry<String, String>> it = authRedirectUrls.entrySet().iterator();

                    Map.Entry<String, String> entry;
                    while (it.hasNext())
                    {
                        entry = it.next();
                        if (pathMatcher.match(entry.getKey(), targetUrl))
                        {
                            redirectStrategy.sendRedirect(request, response, entry.getValue());
                            return;
                        }
                    }
                }
            }
            redirectStrategy.sendRedirect(request, response, clientProperties.getLoginPage());
        }
        catch (Exception e)
        {
            String requestUri = request.getRequestURI();
            String ip = request.getRemoteAddr();
            String msg = String.format("IllegalAccessUrlException: ip=%s, uri=%s, sid=%s, error=%s",
                                      ip,
                                      getServletContextPath() + requestUri,
                                      request.getSession(true).getId(),
                                      e.getMessage());
            log.error(msg, e);
            throw new IllegalAccessUrlException(ErrorCodeEnum.SERVER_ERROR, requestUri, ip);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (clientProperties.getOpenAuthenticationRedirect())
        {
            // 1. 动态注入 requireAuthentication() requestMapping 的映射 uri
            String methodName = "requireAuthentication";

            MvcUtil.setRequestMappingUri(methodName,
                                         clientProperties.getLoginUnAuthenticationRoutingUrl(),
                                         this.getClass(),
                                         HttpServletRequest.class, HttpServletResponse.class);

            // 2. 在 mvc 中做 Uri 映射等动作
            MvcUtil.registerController("clientSecurityController", applicationContext, BaseSecurityController.class);
        }

    }
}
