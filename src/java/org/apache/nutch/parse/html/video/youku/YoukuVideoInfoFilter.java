package org.apache.nutch.parse.html.video.youku;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.*;
import org.apache.nutch.protocol.Content;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.apache.nutch.protocol.Content; 
import org.w3c.dom.Document;  
import org.apache.nutch.parse.html.video.conf.Crawl;
import org.apache.nutch.parse.html.video.database.dao.VideoBaseinfoTblMapper;
import org.apache.nutch.parse.html.video.database.model.VideoBaseinfoTbl;
import org.apache.nutch.parse.html.video.database.model.VideoScRefTbl;
import org.apache.nutch.parse.html.video.util.BeanUtil;
import org.apache.nutch.parse.html.video.util.ConfUtil;
import org.apache.nutch.parse.html.video.util.DOMContentUtils;
import org.apache.nutch.parse.html.video.util.StringUtil;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

public class YoukuVideoInfoFilter implements VideoFilter {
	
	private YoukuVideoInfoBusiness youkuVideoinfoBean;
	
	private static final Log LOG = LogFactory.getLog(YoukuVideoInfoFilter.class.getName());
	  
	private	org.apache.nutch.parse.html.video.util.DOMContentUtils utils;
	  private Configuration conf;
	  private Document doc;
	  private Crawl crawlconf;
	  
	
	@Override	
	public
	ParseResult filter(Content content, ParseResult parseResult, HTMLMetaTags metaTags, Document doc, Object crawlconfig){
		Node node ;
		String text;
		StringBuffer sb = new StringBuffer();
		XPathExpression expr;
		boolean ret = false;
		this.doc = doc;
		this.crawlconf = (Crawl)crawlconfig;
		try {
			expr = XPathFactory.newInstance().newXPath().compile("//*[@id=\"showInfo_wrap\"]");
			node = (Node)expr.evaluate(doc, XPathConstants.NODE);
			utils = new org.apache.nutch.parse.html.video.util.DOMContentUtils(conf);
			if (node != null){
				System.out.println("node is not null");
				utils.getText(sb, node);
			}
			else{
				System.out.println("node is null");
				return parseResult; 
			}         // extract text
		    text = sb.toString();
		    sb.setLength(0);
		    Map map = ConfUtil.getFieldValue(doc, crawlconf);
			
			ApplicationContext ctx = BeanUtil.getBeanContext();
			if (ctx == null)
				LOG.info("ApplicationContext is null");
			else{			
				youkuVideoinfoBean = (YoukuVideoInfoBusiness)BeanUtil.getBean("YoukuVideoInfoBusiness");
				ret = youkuVideoinfoBean.inputVideoInfo(map);//title, alias, pub, type, score, douban_score, area, sl_type, actor, director, host, guest, img, ititle, simpledesc, detaildesc, link, totalvodi, durationi, ding, cai, setnumberi, lastseti);
				//ret = true;
				if (ret)
					return ParseResult.createParseResult(content.getUrl(), 
				            new ParseImpl(text, parseResult.get(content.getUrl()).getData()));
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//return ParseResult.createParseResult(content.getUrl(), 
	    //        new ParseImpl("", parseResult.get(content.getUrl()).getData()));
		return parseResult;
	}

	@Override
	public Configuration getConf() {
		// TODO Auto-generated method stub
		return conf;
	}

	@Override
	public void setConf(Configuration arg0) {
		// TODO Auto-generated method stub
		this.conf = conf;
	}



}