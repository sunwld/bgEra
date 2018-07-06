package com.collie.bgEra.commons.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * Spring  AOP工具类
 */
public class AspectUtils {
  //用于获取Mthod的参数名列表
  private static LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
  //使用SPEL进行解析
  private static ExpressionParser parser = new SpelExpressionParser();

  public static Method getMethod(JoinPoint jp){
    MethodSignature methodSignature = (MethodSignature)jp.getSignature();
    return methodSignature.getMethod();
  }

  /**
   * 解析提供的spEL表达式，获取Method和此Method的具体传参args中，符合spEL表达式的值
   * @param spEL
   * @param method Method对象
   * @param args Method对象的具体传参
   * @return 解析的结果值,String类型
   */
  public static String parseKey(String spEL, Method method, Object[] args){
    //获取被拦截方法参数名列表(使用Spring支持类库)
    String [] paraNameArr=u.getParameterNames(method);
    return parseKey(spEL,paraNameArr,args);
  }

  /**
   * 解析提供的spEL表达式，获取Method和此Method的具体传参args中，符合spEL表达式的值
   * @param spEL
   * @param method
   * @param args
   * @param clazz
   * @param <T> clazz
   * @return
   */
  public static <T> T parseKey(String spEL, Method method, Object[] args,Class<T> clazz){
    //获取被拦截方法参数名列表(使用Spring支持类库)
    String [] paraNameArr=u.getParameterNames(method);
    return parseKey(spEL,paraNameArr,args,clazz);
  }

  /**
   *
   * @param spEL
   * @param paraNameArr
   * @param args
   * @return
   */
  public static String parseKey(String spEL, String[] paraNameArr, Object[] args){
    return parseKey(spEL,paraNameArr,args,String.class);
  }

  /**
   *
   * @param spEL
   * @param paraNameArr
   * @param args
   * @return
   */
  public static <T> T parseKey(String spEL, String[] paraNameArr, Object[] args, Class<T> clazz){
    //SPEL上下文
    StandardEvaluationContext context = new StandardEvaluationContext();
    //把方法参数放入SPEL上下文中
    for(int i=0;i<paraNameArr.length;i++){
      context.setVariable(paraNameArr[i], args[i]);
    }
    return parser.parseExpression(spEL).getValue(context,clazz);
  }
}
