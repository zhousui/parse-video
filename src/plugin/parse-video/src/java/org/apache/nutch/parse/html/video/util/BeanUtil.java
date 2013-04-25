package org.apache.nutch.parse.html.video.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanUtil {
  private static ApplicationContext context = null;

  private static void init(){
    context = new ClassPathXmlApplicationContext("config/applicationContext.xml");
  }
  public static ApplicationContext getBeanContext(){
    if (context == null)
      init();
    return context;
  }
  public static Object getBean(String name) {
    if (context == null)
      init();
    return context.getBean(name);
  }
}