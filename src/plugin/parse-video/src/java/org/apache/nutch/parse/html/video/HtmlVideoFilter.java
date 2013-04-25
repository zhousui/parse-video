package org.apache.nutch.parse.html.video;

// JDK imports
import java.io.ByteArrayInputStream;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.nio.charset.Charset;
import java.util.regex.*;
import org.apache.nutch.metadata.Metadata;
import java.io.*;
import org.apache.nutch.parse.html.video.util.BeanUtil;
import org.apache.nutch.parse.html.video.util.DOMContentUtils;
import org.springframework.context.ApplicationContext;

// Nutch imports
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.html.video.conf.Conf;
import org.apache.nutch.parse.html.video.conf.Crawl;
import org.apache.nutch.parse.html.video.conf.ObjectFactory;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;
import org.apache.nutch.protocol.Content;
import org.cyberneko.html.parsers.*;
import org.apache.nutch.util.*;
import org.apache.nutch.parse.VideoFilter;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// W3C imports
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class HtmlVideoFilter implements HtmlParseFilter {
	private org.apache.nutch.parse.html.video.util.DOMContentUtils utils;

  private static final Log LOG = LogFactory.getLog(HtmlVideoFilter.class.getName());
  
  private Configuration conf;

  private static final int CHUNK_SIZE = 2000;
  private static Pattern metaPattern =
    Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>",
                    Pattern.CASE_INSENSITIVE);
  private static Pattern charsetPattern =
    Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)",
                    Pattern.CASE_INSENSITIVE);

  private static String sniffCharacterEncoding(byte[] content) {
    int length = content.length < CHUNK_SIZE ? 
                 content.length : CHUNK_SIZE;

    // We don't care about non-ASCII parts so that it's sufficient
    // to just inflate each byte to a 16-bit value by padding. 
    // For instance, the sequence {0x41, 0x82, 0xb7} will be turned into 
    // {U+0041, U+0082, U+00B7}. 
    String str = "";
    try {
      str = new String(content, 0, length,
                       Charset.forName("ASCII").toString());
                     //Charset.forName("utf-8").toString());
    } catch (UnsupportedEncodingException e) {
      // code should never come here, but just in case... 
      return null;
    }

    Matcher metaMatcher = metaPattern.matcher(str);
    String encoding = null;
    if (metaMatcher.find()) {
      Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
      if (charsetMatcher.find()) 
        encoding = new String(charsetMatcher.group(1));
    }

    return encoding;
  }
  
  static Conf videoconf = null;
  static HashMap<String, VideoFilter> filterMap = null;
  
  
  private Document getDocument(Content content) throws SAXNotRecognizedException, SAXException,IOException{
	  Metadata metadata = new Metadata();
	  DOMParser parser = new DOMParser();  
	    parser.setFeature("http://cyberneko.org/html/features/augmentations",
            true);
    //parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
    //        "utf-8");
	  parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
			  conf.get(						 "parser.character.encoding.default", "windows-1252"));
    parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset",
            true);
    parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
            false);
    parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",
            true);

    byte[] contentInOctets = content.getContent();
    InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));
//	  
	  EncodingDetector detector = new EncodingDetector(conf);
    detector.autoDetectClues(content, true);
    detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
    String encoding = detector.guessEncoding(content, conf.get("parser.character.encoding.default", "windows-1252"));

    metadata.set(Metadata.ORIGINAL_CHAR_ENCODING, encoding);
    metadata.set(Metadata.CHAR_ENCODING_FOR_CONVERSION, encoding);
    LOG.debug("encoding:"+encoding);
    //input.setEncoding("utf-8");
    input.setEncoding(encoding);
    parser.parse(input);    
    Document doc1 = parser.getDocument(); 
    return doc1;
  }
  /**
   * Scan the HTML document looking for a recommended meta tag.
   */
  @Override
  public ParseResult filter(Content content, ParseResult parse, 
    HTMLMetaTags metaTags, DocumentFragment doc) {
    // Trying to find the document's recommended term
	  //Node node ;
	  //String text;
    //Metadata metadata = new Metadata();
	
    try {	    
     
			  //FileReader fr = new FileReader("conf/conf.xml"); 
			  //BeanUtil.init();
			  //ApplicationContext ctx = BeanUtil.getBeanContext();
			//if (ctx == null)
			//	System.out.println("ApplicationContext is null");
			if (videoconf == null){
				InputStream is = HtmlVideoFilter.class.getResourceAsStream("/config/conf.xml");
				BufferedReader fr=new BufferedReader(new InputStreamReader(is));
		      	JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
		      	Unmarshaller um = context.createUnmarshaller();
		      	videoconf = (Conf)um.unmarshal(fr);
			}
			if (filterMap == null){
		    	  ExtensionPoint point = PluginRepository.get(conf).getExtensionPoint(
		              VideoFilter.X_POINT_ID);
		          if (point == null)
		            throw new RuntimeException(VideoFilter.X_POINT_ID + " not found.");
		          Extension[] extensions = point.getExtensions();
		          filterMap =
		            new HashMap<String, VideoFilter>();
		          for (int i = 0; i < extensions.length; i++) {
		            Extension extension = extensions[i];
		            VideoFilter filter = (VideoFilter) extension
		                .getExtensionInstance();
		            LOG.debug("Adding " + filter.getClass().getName());
					filter.setConf(conf);
		            filterMap.put(filter.getClass().getName(), filter);
		          }
		    } 
	      	//System.out.println("web urlpattern:"+videoconf.getWeb().get(0).getCrawl().get(0).getUrlpattern());
	      	
//	 	    DOMParser parser = new DOMParser();  
//		    parser.setFeature("http://cyberneko.org/html/features/augmentations",
//	              true);
//	      //parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
//	      //        "utf-8");
//		  parser.setProperty("http://cyberneko.org/html/properties/default-encoding",
//				  conf.get(						 "parser.character.encoding.default", "windows-1252"));
//	      parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset",
//	              true);
//	      parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
//	              false);
//	      parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",
//	              true);
//      
//	      byte[] contentInOctets = content.getContent();
//	      InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));
////		  
//		  EncodingDetector detector = new EncodingDetector(conf);
//	      detector.autoDetectClues(content, true);
//	      detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
//	      String encoding = detector.guessEncoding(content, conf.get("parser.character.encoding.default", "windows-1252"));
//	
//	      metadata.set(Metadata.ORIGINAL_CHAR_ENCODING, encoding);
//	      metadata.set(Metadata.CHAR_ENCODING_FOR_CONVERSION, encoding);
//	      LOG.debug("encoding:"+encoding);
//	      //input.setEncoding("utf-8");
//	      input.setEncoding(encoding);
//	      parser.parse(input);    
//	      Document doc1 = parser.getDocument(); 

			//	LOG.info("web url11111111111111111:"+ content.getUrl());
	      if (videoconf != null && filterMap != null){
	      	for(int i=0;i<videoconf.getWeb().size();i++)
	      		for(int j=0;j<videoconf.getWeb().get(i).getCrawl().size();j++){
      				Crawl crawl = videoconf.getWeb().get(i).getCrawl().get(j);
	      			if (Pattern.matches(crawl.getUrlpattern(), content.getUrl())){
	      				LOG.info("web url:"+ content.getUrl());
	      				LOG.info("web:"+videoconf.getWeb().get(i).getDesc()+",url:"+videoconf.getWeb().get(i).getUrl());
	      				LOG.info("Crawl info:"+crawl.getUrlpattern());
						Date start = new Date();
						LOG.info("Start filter url:"+content.getUrl()+"         "+start.toString());
						Document doc1 = getDocument(content);
						ParseResult ret =  filterMap.get(crawl.getExtendclass()).filter(content, parse, metaTags, doc1,crawl);
						Date end = new Date();
						LOG.info("End filter url "+content.getUrl()+", consume "+(end.getTime()-start.getTime())/1000+" seconds      "+end.toString());
						return ret;
	      			}
	      		}
	      }
	    
	    
	} 
    catch (SAXNotRecognizedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		LOG.error(e.getMessage());
	}
//    catch (SAXNotSupportedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		LOG.error(e.getMessage());
//	} 
    catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		LOG.error(e.getMessage());
	} 
    catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		LOG.error(e.getMessage());
	} 
    catch (JAXBException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		LOG.error(e.getMessage());
	} 
    catch (PluginRuntimeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		LOG.error(e.getMessage());
	}
    
    //return ParseResult.createParseResult(content.getUrl(), 
	//            new ParseImpl("", parse.get(content.getUrl()).getData()));
	            return parse;
  }
  
  
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Configuration getConf() {
    return this.conf;
  }


}