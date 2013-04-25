//
// 此文件是由 JavaTM Architecture for XML Binding (JAXB) 引用实现 v2.2.6 生成的
// 请访问 <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// 在重新编译源模式时, 对此文件的所有修改都将丢失。
// 生成时间: 2013.01.31 时间 09:37:43 AM CST 
//


package org.apache.nutch.parse.html.video.conf;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apache.nutch.parse.html.video.conf package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.nutch.parse.html.video.conf
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Crawl }
     * 
     */
    public Crawl createCrawl() {
        return new Crawl();
    }

    /**
     * Create an instance of {@link Web }
     * 
     */
    public Web createWeb() {
        return new Web();
    }

    /**
     * Create an instance of {@link Conf }
     * 
     */
    public Conf createConf() {
        return new Conf();
    }

}
