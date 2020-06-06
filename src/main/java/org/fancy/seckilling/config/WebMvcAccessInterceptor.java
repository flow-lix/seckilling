package org.fancy.seckilling.config;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.fancy.seckilling.annotation.AccessLimitAnnotation;
import org.fancy.seckilling.entity.Account;
import org.fancy.seckilling.enums.ResultStatus;
import org.fancy.seckilling.redis.RedisManager;
import org.fancy.seckilling.vo.ResponseVo;
import org.redisson.api.RBucket;
import org.redisson.api.RLongAdder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author lix
 */
@Aspect
public class WebMvcAccessInterceptor implements WebMvcConfigurer, HandlerInterceptor {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hMethod = (HandlerMethod) handler;
        AccessLimitAnnotation accessLimitAnno = hMethod.getMethodAnnotation(AccessLimitAnnotation.class);
        if (null == accessLimitAnno) {
            return true;
        }

        String key = request.getRequestURI();
        if (accessLimitAnno.needLogin()) {
            // toDO cookie、 token、 session
            Account account = new Account();
            if (null == account) {
                render(response, ResponseVo.failOf(ResultStatus.SESSION_ERROR));
                return false;
            }
            key += ('_' + account.getUsername());
        }

        RBucket<Integer> value = RedisManager.getInstance().getBucket(key);
        if (!value.isExists()) {
            value.set(accessLimitAnno.maxLimit(), accessLimitAnno.timeoutSeconds(), TimeUnit.SECONDS);
        } else if (value.get() <= accessLimitAnno.maxLimit()) {
            value.set(value.get() + 1);
        } else {
            render(response, ResponseVo.failOf(ResultStatus.ACCESS_LIMIT_REACHED));
            return false;
        }
        return true;
    }

    private void render(HttpServletResponse response, ResponseVo responseVo)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        try (OutputStream out = response.getOutputStream()) {
            String str = JSON.toJSONString(responseVo);
            out.write(str.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

    /*    @Around(value = "@annotation(accessLimitAnno)")
    public Object doAround(ProceedingJoinPoint pjp, AccessLimitAnnotation accessLimitAnno) {
        accessLimitAnno.needLogin();

        accessLimitAnno.timeoutSeconds();

        ServletRequestAttributes servletRequestAttrs = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (servletRequestAttrs != null) {
            HttpServletRequest request = servletRequestAttrs.getRequest();

            String key = request.getRequestURI();
            RBucket<Integer> value = RedisManager.getInstance().getBucket(key);
            if (value > accessLimitAnno.maxLimit()) {
                return
            }
            return null;
        }
    }*/
}
