package org.apache.nutch.parse.html.video;

import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.protocol.Content;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

import java.util.Properties;
import java.io.*;
import java.net.URL;

import junit.framework.TestCase;

/*
 * Loads test page recommended.html and verifies that the recommended 
 * meta tag has recommended-content as its value.
 *
 */
public class TestHtmlVideoFilter extends TestCase {

  private static final File testDir =
    new File(System.getProperty("test.data"));

  public void testPages() throws Exception {
    pageTest(new File(testDir, "test.html"), "http://foo.com/");

  }


  public void pageTest(File file, String url)
    throws Exception {

    String contentType = "text/html";
    InputStream in = new FileInputStream(file);
    ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length());
    byte[] buffer = new byte[1024];
    int i;
    while ((i = in.read(buffer)) != -1) {
      out.write(buffer, 0, i);
    }
    in.close();
    byte[] bytes = out.toByteArray();
    Configuration conf = NutchConfiguration.create();

    Content content =
      new Content(url, url, bytes, contentType, new Metadata(), conf);
    Parse parse = new ParseUtil(conf).parseByExtensionId("parse-html",content);

    Metadata metadata = parse.getData().getContentMeta();
    //assertEquals(recommendation, metadata.get("Recommended"));
    //assertTrue("somesillycontent" != metadata.get("Recommended"));
  }
}
