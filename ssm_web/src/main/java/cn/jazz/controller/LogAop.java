package cn.jazz.controller;

import cn.jazz.domain.SysLog;
import cn.jazz.service.ISysLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

@Component //容器注入声明，任意类型
@Aspect //切面声明
public class LogAop {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ISysLogService sysLogService;

    private Date visitTime; //开始时间
    private Class clazz; //访问的类
    private Method method; //访问的方法

    @Before("execution(* cn.jazz.controller.*.*(..))")
    public void doBefore(JoinPoint jp) throws NoSuchMethodException {

        visitTime = new Date(); //当前时间就是开始访问的时间

        clazz = jp.getTarget().getClass(); //获取要访问的类对象

        //获取要访问的方法对象
        String methodName = jp.getSignature().getName(); //获取要访问的方法的名称
        Object[] args = jp.getArgs(); //获取访问方法的参数,只有参数类型的集合
        if (args==null || args.length==0){
            method = clazz.getMethod(methodName); //只能获取无参的方法对象
        }else {
            Class[] classArgs = new Class[args.length]; //创建一个空的Class数组
            for (int i = 0; i < args.length; i++) {
                classArgs[i]=args[i].getClass();
            }
            method = clazz.getMethod(methodName,classArgs);
        }

    }

    @After("execution(* cn.jazz.controller.*.*(..))")
    public void doAfter(JoinPoint jp) throws Exception {
        if (clazz!=null && method!=null && clazz!=LogAop.class){
            //获取访问者的的IP地址
            String ip = request.getRemoteAddr();
            //获取访问时长
            long executionTime = new Date().getTime() - visitTime.getTime();

            //获取访问的url
            String url="";
            //获取访问类的RequestMapping注解对象
            RequestMapping classAnnotation = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            if (classAnnotation!=null){
                String classUrl = classAnnotation.value()[0];
                //获取访问方法的RequestMapping注解对象
                RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                if (methodAnnotation!=null){
                    String methodUrl = methodAnnotation.value()[0];
                    //给访问url赋值
                    url=classUrl+methodUrl;
                }
            }

            //获取当前操作的用户名
            String username = "";
            //获取SecurityContext对象
            SecurityContext securityContext = SecurityContextHolder.getContext();
            //获取当前操作的用户Principal对象，可以强制转换为spring-security提供的User对象
            User user = (User) securityContext.getAuthentication().getPrincipal();
            //给访问者的用户名赋值
            username = user.getUsername();

            //将日志相关信息封装到SysLog对象
            SysLog sysLog = new SysLog();
            sysLog.setVisitTime(visitTime);
            sysLog.setExecutionTime(executionTime);
            sysLog.setIp(ip);
            sysLog.setUrl(url);
            sysLog.setUsername(username);
            sysLog.setMethod("[类名]"+clazz.getName()+"[方法名]"+method.getName());

            //调用service完成日志记录操作
            sysLogService.save(sysLog);
        }
    }
}
