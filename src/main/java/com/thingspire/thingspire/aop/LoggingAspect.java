//package com.thingspire.thingspire.aop;
//
//import lombok.RequiredArgsConstructor;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.AfterThrowing;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.hibernate.cfg.Environment;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.*;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.stream.Stream;
//
//
//
//
//@Aspect
//@Component
//public class LoggingAspect {
//
//    // 특정 패키지 내의 특정 클래스의 메서드 선택
//    @Pointcut("execution(* com.thingspire.thingspire.user.*.*(..)) && within(com.thingspire.thingspire.user.MemberController)")
//    public void onRequest() {
//
//    }
//
//    @Around("onRequest()")
//    public Object logAction(ProceedingJoinPoint joinPoint) throws Throwable {
//        Class clazz = joinPoint.getTarget().getClass();
//        Logger logger = LoggerFactory.getLogger(clazz);
//        Object result = null;
//        try {
//            result = joinPoint.proceed(joinPoint.getArgs());
//            return result;
//        } finally {
//            logger.info(getRequestUrl(joinPoint, clazz));
//
////            logger.info("parameters" + JSON.toJSONString(params(joinPoint)));
////            logger.info("response: " + JSON.toJSONString(result, true));
//        }
//    }
//    private String getRequestUrl(JoinPoint joinPoint, Class clazz) {
//        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//        Method method = methodSignature.getMethod();
//        RequestMapping requestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
//        String baseUrl = requestMapping.value()[0];
//
//        String url = Stream.of( GetMapping.class, PutMapping.class, PostMapping.class,
//                        PatchMapping.class, DeleteMapping.class, RequestMapping.class)
//                .filter(mappingClass -> method.isAnnotationPresent(mappingClass))
//                .map(mappingClass -> getUrl(method, mappingClass, baseUrl))
//                .findFirst().orElse(null);
//        return url;
//    }
//
//    /* httpMETHOD + requestURI 를 반환 */
//    private String getUrl(Method method, Class<? extends Annotation> annotationClass, String baseUrl){
//        Annotation annotation = method.getAnnotation(annotationClass);
//        String[] value;
//        String httpMethod = null;
//        try {
//            value = (String[])annotationClass.getMethod("value").invoke(annotation);
//            httpMethod = (annotationClass.getSimpleName().replace("Mapping", "")).toUpperCase();
//        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
//            return null;
//        }
//        return String.format("%s %s%s", httpMethod, baseUrl, value.length > 0 ? value[0] : "") ;
//    }
//
//    private final Environment env;
//
//    @Autowired
//    public LoggingAspect(Environment env) {this.env = env;}
//
//    @Pointcut(
//            "within(@org.springframework.stereotype.Repository *)" +
//                    " || within(@org.springframework.stereotype.Service *)" +
//                    " || within(@org.springframework.web.bind.annotation.RestController *)"
//    )
//    public void springBeanPointCut() {
//
//    }
//    @Pointcut("within(com.thingspire.thingspire.user..*)"
////            + " || within(com.thingspire.thingspire.service..*)" + " || within(com.thingspire.thingspire.web.rest..*)"
//    )
//    public void applicationPackagePointcut() {
//        // Method is empty as this is just a Pointcut, the implementations are in the advices.
//    }
//
//    private Logger logger(JoinPoint joinPoint) {
//        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
//    }
//
//    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
//    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
//        logger(joinPoint)
//                .error(
//                        "Exception in {}() with cause = '{}' and exception = '{}'",
//                        joinPoint.getSignature().getName(),
//                        e.getCause() != null ? e.getCause() : "NULL",
//                        e.getMessage(),
//                        e
//                );
//    }
//
//    @Around("applicationPackagePointcut() && springBeanPointcut()")
//    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
//        Logger log = logger(joinPoint);
//        log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
//        if (log.isDebugEnabled()) {
//            log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
//        }
//        try {
//            Object result = joinPoint.proceed();
//            return result;
//        } catch (IllegalArgumentException e) {
//            log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getName());
//            throw e;
//        }
//    }
//
//}
