package org.apache.nutch.parse.html.video.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.parse.html.video.conf.Crawl;
import org.apache.nutch.parse.html.video.conf.Field;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfUtil {
	private static Log LOG = LogFactory.getLog(ConfUtil.class.getName());	
	public static Field getCrawlField(Crawl conf, String field){
		for(int i=0;i<conf.getField().size();i++){
			if (conf.getField().get(i).getName().equals(field)){
				return conf.getField().get(i);
			}
		}
		return null;
	}
	public static String getCrawlXpath(Crawl conf, String field){
		for(int i=0;i<conf.getField().size();i++){
			if (conf.getField().get(i).getName().equals(field)){
				return conf.getField().get(i).getXpath();
			}
		}
		return null;
	}
	public static NodeList getXPathNodes(Document doc,String path) throws XPathExpressionException{
		XPathExpression expr;
		expr = XPathFactory.newInstance().newXPath().compile(path);
		NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		return nodes;
	}
	public static Node getXPathNode(Document doc,String path) throws XPathExpressionException{
		XPathExpression expr;
		Node node = null;
		expr = XPathFactory.newInstance().newXPath().compile(path);
		node = (Node)expr.evaluate(doc, XPathConstants.NODE);
		return node;
	}
	
	public static Map getFieldValue(Document doc,Crawl conf)throws XPathExpressionException {
		Map map = new HashMap();
		for(int i=0;i<conf.getField().size();i++){
			Object value = getFieldValue(doc,conf.getField().get(i));
			if (value != null){
				map.put(conf.getField().get(i).getName(), value);
			}
		}
		return map;
	}
	
	public static Object getFieldValue(Document doc, Field f)throws XPathExpressionException {
		if (f.getOption() != null && f.getOption().endsWith("set")){
			NodeList nodes = getXPathNodes(doc,f.getXpath());
			List<String> list = new ArrayList<String>();
			//HashMap map = new HashMap();
			for (int i = 0, n = nodes.getLength(); i < n; i++) {
			      Node node = nodes.item(i);
			      //map.put(i+1, node.getNodeValue());
			      list.add(node.getNodeValue());
			}
			//return map;
			if (list.size()!=0)
				return list;
		}else{
			Node node = getXPathNode(doc,f.getXpath());
			if (node == null){
				if (f.getXpath1() != null)
				node = getXPathNode(doc,f.getXpath1());
			}
			if (node != null){
				if (f.getOption() == null){
					String text = node.getNodeValue();
				    text = text.replaceAll("\\s+", "");
			        text = text.trim();
			        return text;
				}
				else if (f.getOption().endsWith("walk")){
					DOMContentUtils utils = new DOMContentUtils(null);
				    StringBuffer sb = new StringBuffer();
				    utils.getText(sb, node);
				    String text = sb.toString();
				    sb.setLength(0);
				    if (text.indexOf(":")>0)
				    text = text.substring(text.indexOf(":")+1);
				    text = text.replaceAll("\\s+", "");
			        text = text.trim();
				    return text;
				}
				else if (f.getOption().endsWith("node")){
					return node;
				}
			}
		}
		return null;
	}
	
	public static Object getFieldValue(Document doc,Crawl crawlconf,String field )throws XPathExpressionException {
		Field f = getCrawlField(crawlconf, field);
		
		return getFieldValue(doc,f);
	}

	public static HashMap<String, ArrayList<String>> getDocElements(Document doc, Map xpath)throws XPathExpressionException, HttpException, IOException {
		//Document doc = getDoc(url);
		HashMap map = new HashMap();
		Iterator ite = xpath.keySet().iterator();
		while(ite.hasNext()){
			String key = (String) ite.next();
			//String path = (String) xpath.get(key);
			Field f = (Field) xpath.get(key);
			String path = f.getXpath();
			String opt = f.getOption();
			NodeList nodes = ConfUtil.getXPathNodes(doc,path);
			List<String> list = new ArrayList<String>();
			for (int i = 0, n = nodes.getLength(); i < n; i++) {
			      Node node = nodes.item(i);
			      if (opt == null){
				      LOG.debug(key+":"+node.getNodeValue());
				      list.add(node.getNodeValue());
			      }else if (opt.endsWith("walk")){
			    	  DOMContentUtils utils = new DOMContentUtils(null);
					    StringBuffer sb = new StringBuffer();
					    utils.getText(sb, node);
					    String text = sb.toString();
					    sb.setLength(0);
					    if (text.indexOf(":")>0)
					    text = text.substring(text.indexOf(":")+1);
					    text = text.replaceAll("\\s+", "");
				        text = text.trim();
				        text = text.replace("|", "/");
				        text = text.replaceAll("\\(\\d+:\\d+\\)", "");
				        LOG.debug(key+":"+text);
				        list.add(text);
			      }
			}
			map.put(key, list);
		}
		return map;
	}
	
//	public static  String getSingleValue(Document doc,Crawl crawlconf,String field )throws XPathExpressionException {
//		//System.out.println("getSingleValue field:"+field);
//		String xpath = ConfUtil.getCrawlXpath(crawlconf, field);
//		//System.out.println(field + " xpath:"+xpath);
//		Node node = getXPathNode(doc,xpath);
//		/*NodeList currentChildren = node.getChildNodes();
//	    int childLen = (currentChildren != null) ? currentChildren.getLength() : 0;
//	    
//	    // put the children node on the stack in first to last order
//    	StringBuffer sb1 = new StringBuffer();
//	    for (int i = childLen - 1; i >= 0; i--) {
//	    	System.out.println("Name:"+currentChildren.item(i).getNodeName());
//			System.out.println("type:"+currentChildren.item(i).getNodeType());
//	    	if (currentChildren.item(i).getNodeType() == Node.TEXT_NODE){
//				String text = currentChildren.item(i).getNodeValue();
//				text = text.replaceAll("\\s+", " ");
//	        text = text.trim();
//				if (text.length() > 0) {
//					System.out.println("fdfdfdfffff");
//	          if (sb1.length() > 0) sb1.append(' ');
//	        	sb1.append(text);				
//					}
//				String tt = sb1.toString();
//				sb1.setLength(0);
//	    		System.out.println("value:"+text);
//				System.out.println("");
//				
//					}
//			else{
//				if (currentChildren.item(i).getChildNodes() != null){
//								String text1 = currentChildren.item(i).getChildNodes().item(0).getNodeValue();
//								System.out.println("child value:"+text1);
//
//				}
//	    	}
//	    }*/
//		/*DOMContentUtils utils = new DOMContentUtils(null);
//	    StringBuffer sb = new StringBuffer();
//	    utils.getText(sb, node);
//	    String text = sb.toString();
//	    sb.setLength(0);*/
//	    //System.out.println("dddd:"+text);
//	    return node.getNodeValue();	
//	}

}
