package org.apache.nutch.parse.html.video.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.html.video.Constants;
import org.apache.nutch.util.EncodingDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.ParseException;
import net.chilicat.m3u8.Playlist;

import org.cyberneko.html.parsers.*;

public class HttpUtil {
	private static Log LOG = LogFactory.getLog(HttpUtil.class.getName());	
	private static final String USERAGENT = "Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3";
	private static final String CHARSET = "GBK,utf-8;q=0.7,*;q=0.3";
	private static final String LANGUAGE = "zh-CN,zh;q=0.8";
//	private static final int CHUNK_SIZE = 2000;
//	  private static Pattern metaPattern =
//	    Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>",
//	                    Pattern.CASE_INSENSITIVE);
//	  private static Pattern charsetPattern =
//	    Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)",
//	                    Pattern.CASE_INSENSITIVE);
//
//	  private static String sniffCharacterEncoding(byte[] content) {
//	    int length = content.length < CHUNK_SIZE ? 
//	                 content.length : CHUNK_SIZE;
//
//	    // We don't care about non-ASCII parts so that it's sufficient
//	    // to just inflate each byte to a 16-bit value by padding. 
//	    // For instance, the sequence {0x41, 0x82, 0xb7} will be turned into 
//	    // {U+0041, U+0082, U+00B7}. 
//	    String str = "";
//	    try {
//	      str = new String(content, 0, length,
//	                       Charset.forName("ASCII").toString());
//	                     //Charset.forName("utf-8").toString());
//	    } catch (UnsupportedEncodingException e) {
//	      // code should never come here, but just in case... 
//	      return null;
//	    }
//
//	    Matcher metaMatcher = metaPattern.matcher(str);
//	    String encoding = null;
//	    if (metaMatcher.find()) {
//	      Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
//	      if (charsetMatcher.find()) 
//	        encoding = new String(charsetMatcher.group(1));
//	    }
//
//	    return encoding;
//	  }
	public static byte[] getHttp(String url) throws HttpException, IOException {
		HttpClient httpClient = new HttpClient();  
		List<Header> headers = new ArrayList<Header>();  
        headers.add(new Header("User-Agent", USERAGENT)); 
        headers.add(new Header("Accept-Charset",CHARSET));
        headers.add(new Header("Accept-Language",LANGUAGE));
        httpClient.getHostConfiguration().getParams().setParameter("http.default-headers", headers);  
  
        GetMethod method = new GetMethod(url);  
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,  
                new DefaultHttpMethodRetryHandler(3, false));  
        byte[] ret = null;
        try {  
            int statusCode = httpClient.executeMethod(method);  
  
            if (statusCode != HttpStatus.SC_OK) {  
                System.out.println("Method failed code="+statusCode+": " + method.getStatusLine());  
                LOG.info("Method failed code="+statusCode+": " + method.getStatusLine());
            } else {  
                //System.out.println(new String(method.getResponseBody(), "utf-8"));  
            	ret = method.getResponseBody();
            	LOG.debug("http ret:\n"+new String(ret,"utf-8"));
            }  
        } finally {  
            method.releaseConnection();  
        } 
        return ret;
	}
	
	public static Document getDoc(byte[] contentInOctets)throws IOException{
		Document ret = null;
		try{
		DOMParser parser = new DOMParser();  
	    parser.setFeature("http://cyberneko.org/html/features/augmentations",
              true);
      //parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
      //        "utf-8");
	  parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
//			  conf.get(						 "parser.character.encoding.default", "windows-1252"));
			  "utf-8");
      parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset",
              true);
      parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
              false);
      parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",
              true);
      //byte[] contentInOctets = getHttp(url);
      InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));
	  
//	  EncodingDetector detector = new EncodingDetector(conf);
//  detector.autoDetectClues(content, true);
//  detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
//  String encoding = detector.guessEncoding(content, conf.get("parser.character.encoding.default", "windows-1252"));
//
//  metadata.set(Metadata.ORIGINAL_CHAR_ENCODING, encoding);
//  metadata.set(Metadata.CHAR_ENCODING_FOR_CONVERSION, encoding);
//  System.out.println("encoding:"+encoding);  
//  input.setEncoding(encoding);
      input.setEncoding("utf-8");
      parser.parse(input);    
      ret= parser.getDocument(); 
	}catch (SAXNotRecognizedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXNotSupportedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
//		Document doc = ret;
//		org.w3c.dom.Element root = doc.getDocumentElement();
//		NodeList items = root.getChildNodes();
//		if (items != null){
//			for(int i=0;i<items.getLength();i++){
//				Node item = items.item(i);
//				if (item.getNodeType() == Node.ELEMENT_NODE){
//					for(Node node=item.getFirstChild();node!=null;node=node.getNextSibling()){
//						if(node.getNodeType()==Node.ELEMENT_NODE){
//							if(node.getAttributes().getNamedItem("class").equals("thumb")){
//								LOG.info("thumb:"+node.getFirstChild().getAttributes().getNamedItem("src"));
//							}
//						}
//					}
//				}
//			}
//		}
		return ret;
	}
	
	public static Document getDoc(String url )throws HttpException, IOException {
		Document ret = null;
		byte[] contentInOctets = getHttp(url);
		ret = getDoc(contentInOctets);
		
		return ret;
	}
		
	public static Map getM3U8Map(String vid) throws ParseException,HttpException,IOException{
		HashMap map = new HashMap();
		byte[] m3u8 = getHttp("http://v.youku.com/player/getM3U8/vid/"+vid+"/v.m3u8");
		if (m3u8 != null) {
			java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(m3u8);
			Playlist list = Playlist.parse(input);
			LOG.debug("list:"+list.toString());
			List<Element> elements = list.getElements();
			for(int i=0;i<elements.size();i++){
				if (elements.get(i).getPlayListInfo().getBandWitdh() == 25000){
					map.put(Constants.NORMAL, elements.get(i).getURI().toString());
				}else if(elements.get(i).getPlayListInfo().getBandWitdh() == 50000){
					map.put(Constants.HIGH, elements.get(i).getURI().toString());
				} else if(elements.get(i).getPlayListInfo().getBandWitdh() == 75000){
					map.put(Constants.SUPER, elements.get(i).getURI().toString());
				} 
			}
			return map;
		}
		return null;
	}
}


