package com.maximovich.planner.aspects;

import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    @Around("execution(public * com.maximovich.planner.services..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        boolean completed = false;
        try {
            Object result = joinPoint.proceed();
            completed = true;
            return result;
        } finally {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            long durationInMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            LOG.info(
                "{}.{} {} in {} ms",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                completed ? "completed" : "failed",
                durationInMillis
            );
        }
    }
}
