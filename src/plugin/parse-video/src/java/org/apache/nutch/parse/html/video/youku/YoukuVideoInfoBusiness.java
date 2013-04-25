package org.apache.nutch.parse.html.video.youku;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.chilicat.m3u8.ParseException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.parse.html.video.Constants;
import org.apache.nutch.parse.html.video.conf.Field;
import org.apache.nutch.parse.html.video.database.dao.AreaCategoryTblMapper;
import org.apache.nutch.parse.html.video.database.dao.ContentSupplierTblMapper;
import org.apache.nutch.parse.html.video.database.dao.FirstlevelCategoryTblMapper;
import org.apache.nutch.parse.html.video.database.dao.OptTblMapper;
import org.apache.nutch.parse.html.video.database.dao.SecondlevelCategoryTblMapper;
import org.apache.nutch.parse.html.video.database.dao.VideoBaseinfoTblMapper;
import org.apache.nutch.parse.html.video.database.dao.VideoCrawlinfoTblMapper;
import org.apache.nutch.parse.html.video.database.dao.VideoPlayinfoTblMapper;
import org.apache.nutch.parse.html.video.database.dao.VideoScRefTblMapper;
import org.apache.nutch.parse.html.video.database.dao.VideoSourceinfoTblMapper;
import org.apache.nutch.parse.html.video.database.model.AreaCategoryTbl;
import org.apache.nutch.parse.html.video.database.model.AreaCategoryTblExample;
import org.apache.nutch.parse.html.video.database.model.ContentSupplierTblExample;
import org.apache.nutch.parse.html.video.database.model.ContentSupplierTblExample.Criteria;
import org.apache.nutch.parse.html.video.database.model.FirstlevelCategoryTbl;
import org.apache.nutch.parse.html.video.database.model.SecondlevelCategoryTbl;
import org.apache.nutch.parse.html.video.database.model.VideoBaseinfoTbl;
import org.apache.nutch.parse.html.video.database.model.VideoBaseinfoTblExample;
import org.apache.nutch.parse.html.video.database.model.VideoCrawlinfoTbl;
import org.apache.nutch.parse.html.video.database.model.VideoPlayinfoTbl;
import org.apache.nutch.parse.html.video.database.model.VideoScRefTbl;
import org.apache.nutch.parse.html.video.database.model.VideoSourceinfoTbl;
import org.apache.nutch.parse.html.video.util.ConfUtil;
import org.apache.nutch.parse.html.video.util.DOMContentUtils;
import org.apache.nutch.parse.html.video.util.HttpUtil;
import org.apache.nutch.parse.html.video.util.PinyinUtil;
import org.apache.nutch.parse.html.video.util.StringUtil;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.xml.xpath.XPathExpressionException;

public class YoukuVideoInfoBusiness {	

	private static final Log LOG = LogFactory.getLog(YoukuVideoInfoBusiness.class.getName());
	@Resource(name="ContentSupplierTblMapper")
	private ContentSupplierTblMapper csMapper;
	/*
	 * AreaCategoryTblMapper
	 *
	 * @Select("select ac_id from area_category_tbl where ac_name=#{area}")
    public Integer getAcIdByName(@Param("area") String area);
    
    @Select("select max(ac_id) from area_category_tbl")
    public Integer getLastAcId();
	 */
	@Resource(name="AreaCategoryTblMapper")
	private AreaCategoryTblMapper areaMapper;
	/*
	 * FirstlevelCategoryTblMapper
	 * @Select("select fc_id from firstlevel_category_tbl where fc_name=#{fcname}")
    public Integer getFcIdByName(@Param("fcname") String fcname);
    
    @Select("select max(fc_id) from firstlevel_category_tbl")
    public Integer getLastFcId();
	 */
	@Resource(name="FirstlevelCategoryTblMapper")
	private FirstlevelCategoryTblMapper flMapper;
	
	/*
	 * SecondlevelCategoryTblMapper
	 * @Select("select sc_id from secondlevel_category_tbl where sc_name=#{scname}")
    public Integer getScIdByName(@Param("scname") String scname);
    
    @Select("select max(sc_id) from secondlevel_category_tbl")
    public Integer getLastScId();
	 */
	@Resource(name="SecondlevelCategoryTblMapper")
	private SecondlevelCategoryTblMapper slMapper;
		
	/*
	 * VideoBaseinfoTblMapper
	 * @Select("select last_insert_id() from video_baseinfo_tbl")
    public Integer getLastVbId();
	 */
	@Resource(name="VideoBaseinfoTblMapper")
	private VideoBaseinfoTblMapper videoBaseInfoMapper;
	
	@Resource(name="VideoScRefTblMapper")
	private VideoScRefTblMapper videoScMapper;
	
	@Resource(name="VideoCrawlinfoTblMapper")
	private VideoCrawlinfoTblMapper videoCrawlinfoMapper;

	@Resource(name="VideoSourceinfoTblMapper")
	private VideoSourceinfoTblMapper vsinfoMapper;
	
	@Resource(name="VideoPlayinfoTblMapper")
	private VideoPlayinfoTblMapper vpinfoMapper;
	
	@Resource(name="OptTblMapper")
	private OptTblMapper optMapper;
	
	int addAreainfo(String area){
		Integer id = optMapper.getAcIdByNameFromAcTbl(area);
		if (id == null){
			AreaCategoryTbl record = new AreaCategoryTbl();
			record.setAcName(area);
			areaMapper.insertSelective(record);
			Integer ret = optMapper.getLastId();
			if (ret == null)
				return -1;
			LOG.info("insert into video_areainfo_tbl value("
					+ret
					+","+area
					+")"
				);
			return ret;
		}
		else
			return id;
	}
	
	int addFc(String fcname){
		Integer id = optMapper.getFcIdByNameFromFcTbl(fcname);
		if (id == null){
			FirstlevelCategoryTbl record = new FirstlevelCategoryTbl();
			record.setFcName(fcname);
			flMapper.insertSelective(record);;
			Integer ret = optMapper.getLastId();
			if (ret == null)
				return -1;
			LOG.info("insert into FirstlevelCategoryTbl value("
					+ret
					+","+fcname
					+")"
				);
			return ret;
		}
		else
			return id; 
	}
	
	int addSc(short fcid,String scname){
		Integer id = optMapper.getScIdByNameFromScTbl(scname);
		if (id == null){
			SecondlevelCategoryTbl record = new SecondlevelCategoryTbl();
			record.setScName(scname);
			record.setFcId(fcid);
			slMapper.insertSelective(record);
			Integer ret = optMapper.getLastId();
			if (ret == null)
				return -1;
			LOG.info("insert into SecondlevelCategoryTbl value("
					+ret
					+","+fcid
					+","+scname
					+")"
				);
			return ret;
		}
		else
			return id;
	}
	
	void addScVideo(short[] sl, int vbid){
		Integer id = optMapper.getIdByVbidFromScRefTbl(vbid);
		if (id != null){
			return;
		}
		for(int i=0;i<sl.length;i++){
			VideoScRefTbl record = new VideoScRefTbl();
			record.setScId(sl[i]);
			record.setVbId(vbid);
			videoScMapper.insertSelective(record);
			LOG.info("insert into VideoScRefTbl value("
					+","+sl[i]
					+","+vbid
					+")"
				);
		}
	}
	
	int addVideoBaseinfo(VideoBaseinfoTbl info){
//		VideoBaseinfoTblExample example = new VideoBaseinfoTblExample();
//		VideoBaseinfoTblExample.Criteria con = example.createCriteria();
//		con.andFcIdEqualTo(info.getFcId());
//		con.andVbYearEqualTo(info.getVbYear());
//		con.andVbNameEqualTo(info.getVbName());
		Integer vbid=-1;
		if (info.getVbYear()!=null){
			vbid=optMapper.getVbIdByTypeYearTitleFromVbTbl(info.getFcId(),info.getVbYear(),info.getVbName());
		}else{
			vbid=optMapper.getVbIdByTypeTitleFromVbTbl(info.getFcId(),info.getVbName());
		}
		if (vbid == null){
			videoBaseInfoMapper.insertSelective(info);
			Integer ret = optMapper.getLastId();
			if (ret == null)
				return -1;
			LOG.info("insert into video_baseinfo_tbl value("
					+ret
					+","+info.getFcId()
					+","+info.getAcId()
					+","+info.getVbName()
					+","+info.getVbActor()
					+","+info.getVbDirector()
					+")"
				);
			return ret;
		}else{
			info.setVbId(vbid);
			videoBaseInfoMapper.updateByPrimaryKeySelective(info);
			LOG.info("update video_baseinfo_tbl value("
					+vbid
					+","+info.getFcId()
					+","+info.getAcId()
					+","+info.getVbName()
					+","+info.getVbActor()
					+","+info.getVbDirector()
					+")"
				);
			return vbid;
		}
	}
	
	void addCrawlinfo(VideoCrawlinfoTbl info){
		int vbid = info.getVbId();
		Integer id = optMapper.getIdByVbidFromVcTbl(vbid);
		if (id == null){
			videoCrawlinfoMapper.insertSelective(info);
			Integer ret = optMapper.getLastId();
			LOG.info("insert into video_crawlinfo_tbl value("
					+ret
					+","+info.getCsId()
					+","+info.getVbId()
					+","+info.getVcOrifc()
					+","+info.getVcOrisc()
					+","+info.getVcVid()
					+","+info.getVcSettotal()
					+","+info.getVcDetailurl()
					+","+info.getVcCrawldate()
					+")"
				);
		}else{
			info.setId(id);
			videoCrawlinfoMapper.updateByPrimaryKeySelective(info);
			LOG.info("update video_crawlinfo_tbl value("
					+","+info.getId()
					+","+info.getCsId()
					+","+info.getVbId()
					+","+info.getVcOrifc()
					+","+info.getVcOrisc()
					+","+info.getVcVid()
					+","+info.getVcSettotal()
					+","+info.getVcDetailurl()
					+","+info.getVcCrawldate()
					+")"
				);
		}
	}
	
	void addSourceinfo(VideoSourceinfoTbl info) {
		String subid = info.getVsSubvid();
		Integer id = optMapper.getIdBySubvidFromVsTbl(subid);
		if (id == null){
			vsinfoMapper.insertSelective(info);
			Integer ret = optMapper.getLastId();
			LOG.info("insert into video_sourceinfo_tbl value("
					+ret
					+","+info.getCsId()
					+","+info.getVbId()
					+","+info.getVsSubvid()
					+","+info.getVsSubtitle()
					+","+info.getVsSetnumber()
					+","+info.getVsSourceurl()
					+")"
				);
		}else{
			info.setId(id);
			vsinfoMapper.updateByPrimaryKeySelective(info);
			LOG.info("update video_sourceinfo_tbl value("
					+id
					+","+info.getCsId()
					+","+info.getVbId()
					+","+info.getVsSubvid()
					+","+info.getVsSubtitle()
					+","+info.getVsSetnumber()
					+","+info.getVsSourceurl()
					+")"
				);
		}
	}
	
	void addPlayinfo(VideoPlayinfoTbl info) {
		String subid = info.getVsSubvid();
		Short def = info.getVpDefinition();
		Integer id = optMapper.getIdBySubvidDefFromVpTbl(subid,def);
		if (id == null){
			vpinfoMapper.insertSelective(info);
			Integer ret = optMapper.getLastId();
			LOG.info("insert into video_playinfo_tbl value("
					+ret
					+","+info.getVsSubvid()
					+","+info.getVpDefinition()
					+","+info.getVpPlayurl()
					+")"
				);
		}else{
			info.setId(id);
			vpinfoMapper.updateByPrimaryKeySelective(info);
			LOG.info("update video_playinfo_tbl value("
					+id
					+","+info.getVsSubvid()
					+","+info.getVpDefinition()
					+","+info.getVpPlayurl()
					+")"
				);
		}
	}
	private void clearEmptyDocMap(Map ret) {

		List keyset = new ArrayList<String>();
		Iterator ite = ret.keySet().iterator();
		while(ite.hasNext()){
			String key = (String) ite.next();
			List list = (ArrayList<String>)ret.get(key);
			if (list.isEmpty()){
				keyset.add(key);
			}else{
				Iterator listite = list.iterator();
				boolean allnull = true;
				while(listite.hasNext()){
					Object item = listite.next();
					if (item != null){
						allnull = false;
						break;
					}
				}
				if (allnull){
					keyset.add(key);
				}
			}
		}
		ite = keyset.iterator();
		while(ite.hasNext()){
			ret.remove(ite.next());
		}
	}
	/*
	 <div class="link"><a charset="419-4-7"  title="不一样的"范儿" 120915" target="_blank" href="http://v.youku.com/v_show/id_XNDUwODA4MDE2.html"></a></div>
		<div class="thumb"><img alt="不一样的"范儿" 120915" src="http://g2.ykimg.com/01270F1F4650E5FCAC906F0123193CFA054824-205C-46E7-AB7F-53455C4CEB43"></div>
		<div class="title"><a charset="419-4-2"  title="不一样的"范儿" 120915" target="_blank" href="http://v.youku.com/v_show/id_XNDUwODA4MDE2.html">不一样的"范儿" 120915</a></div>
	 */
	private Map getDocInfoNew(String id, /*Map xpathmap,*/ List reload)throws HttpException, IOException{
		Map ret = new HashMap<String, ArrayList<String>>();
//		Iterator ite = xpathmap.keySet().iterator();
//		while(ite.hasNext()){
//			ret.put(ite.next(), new ArrayList<String>());
//		}
		ret.put("times", new ArrayList<String>());
		ret.put("links", new ArrayList<String>());
		ret.put("thumbs", new ArrayList<String>());
		ret.put("titles", new ArrayList<String>());
		ret.put("descs", new ArrayList<String>());
		ret.put("guests", new ArrayList<String>());
		ret.put("setnumbers", new ArrayList<String>());
		for(int l=0;l<reload.size();l++){
			String reloadstr = (String) reload.get(l);
			int reload_setnumber = Integer.parseInt(reloadstr.substring(7));
			String http = new String("http://www.youku.com/show_point_id_"+id+".html?dt=json&divid=point_"+reloadstr+"&tab=0");
			LOG.debug(http);
			byte[] content = HttpUtil.getHttp(http);
			Document doc = HttpUtil.getDoc(new String("<html>"+new String(content,"utf-8")+"</html>").getBytes());
			Element root=doc.getDocumentElement();
			NodeList items=root.getChildNodes();
//			boolean timevalid = false;
//			boolean linkvalid = false;
//			boolean titlevalid = false;
//			boolean thumbvalid =  false;
//			boolean descvalid = false;
//			boolean guestvalid = false;
			
			if(items!=null){
				 for(int i=0;i<items.getLength();i++){
					 Node itemroot = items.item(i);
					 NodeList values = itemroot.getChildNodes();
					 if (values != null){
						 for(int j=0;j<values.getLength();j++){
							 Node value = values.item(j);
							 if (value.getNodeType() == Node.ELEMENT_NODE){		
								 LOG.debug("-----------------------------------------");
//								 linkvalid = false;
//								 timevalid = false;
//								 titlevalid = false;
//								 thumbvalid =  false;
//								 descvalid = false;
//								 guestvalid = false;
								 String time=null,link=null,title=null,thumb=null,desc=null,guest=null;
								 NodeList nl = value.getChildNodes();
								 for(int k=0;k<nl.getLength();k++){
									 Node n = nl.item(k);
									 if (n.hasAttributes()){
										 if (n.getAttributes().getNamedItem("class")!=null){
											 if (n.getAttributes().getNamedItem("class").getNodeValue().equals("thumb")){
												 Node subnode = n.getFirstChild();
												 if (subnode.getAttributes().getNamedItem("src") != null){
													 //thumbvalid = true;
													 thumb = subnode.getAttributes().getNamedItem("src").getNodeValue();
													 //((ArrayList<String>)ret.get("thumbs")).add(subnode.getAttributes().getNamedItem("src").getNodeValue());
													 LOG.debug("thumb:"+thumb);
												 }
//												 else{
//													 ((ArrayList<String>)ret.get("thumbs")).add("");
//												 }
											 }else if (n.getAttributes().getNamedItem("class").getNodeValue().equals("link")){
												 Node subnode = n.getFirstChild();
												 if (subnode.getAttributes().getNamedItem("href") != null){
													 //linkvalid = true;
													 link = subnode.getAttributes().getNamedItem("href").getNodeValue();
													 LOG.debug("link:"+link);
													 //((ArrayList<String>)ret.get("links")).add(subnode.getAttributes().getNamedItem("href").getNodeValue());
												 }
											 }else if (n.getAttributes().getNamedItem("class").getNodeValue().equals("time")){
												 Node subnode = n.getFirstChild();
												 if (subnode.getFirstChild() != null){
													 //timevalid = true;
													 time = subnode.getFirstChild().getNodeValue();
													 LOG.debug("time:"+time);
													 //((ArrayList<String>)ret.get("times")).add(subnode.getFirstChild().getNodeValue());
												 }
											 }else if (n.getAttributes().getNamedItem("class").getNodeValue().equals("title")){
												 Node subnode = n.getFirstChild();
												 if (subnode.getFirstChild() != null){
													 //titlevalid = true;
													 title = subnode.getFirstChild().getNodeValue();
													 LOG.debug("title:"+title);
													 //((ArrayList<String>)ret.get("titles")).add(subnode.getFirstChild().getNodeValue());
												 }else {
													 //titlevalid = true;
													 title = subnode.getNodeValue();
													 LOG.debug("title:"+title);
													 //((ArrayList<String>)ret.get("titles")).add(subnode.getNodeValue());
												 }
											 }else if (n.getAttributes().getNamedItem("class").getNodeValue().equals("desc")){
												 Node subnode = n.getFirstChild();
												 if (subnode != null){
													 //descvalid =  true;
													 desc = subnode.getNodeValue();
													 LOG.debug("desc:"+desc);
													 //((ArrayList<String>)ret.get("descs")).add(subnode.getNodeValue());
												 }
											 } else if (n.getAttributes().getNamedItem("class").getNodeValue().equals("guestlist")){
												 DOMContentUtils utils = new DOMContentUtils(null);
												    StringBuffer sb = new StringBuffer();
												    utils.getText(sb, n);
												    String text = sb.toString();
												    sb.setLength(0);
												    if (text.indexOf(":")>0)
												    text = text.substring(text.indexOf(":")+1);
												    text = text.replaceAll("\\s+", "");
											        text = text.trim();
											        text = text.replace("|", "/");
											        text = text.replaceAll("\\(\\d+:\\d+\\)", "");
											        //guestvalid = true;
											        guest = text;
											        LOG.debug("guest:"+guest); 
											        //((ArrayList<String>)ret.get("guests")).add(text);
											        
											      //if (!linkvalid){
											        if (link == null){
											    	  URL u = utils.getBase(n);
											    	  if (u != null){
											    	  String url = u.toString();
											    	  url = url.substring(0,url.indexOf("?"));
											    	  link = url;
												        LOG.debug("link1:"+link); 
											    	  }
											      }
											 } 
										 }
									 }
								 }
								 if (link == null){
									 continue;
								 }else{
									 ((ArrayList<String>)ret.get("links")).add(link);
									 if (title == null){
										 ((ArrayList<String>)ret.get("titles")).add(null);
										 List list = (ArrayList<String>)ret.get("setnumbers");
										 Integer setnumber = reload_setnumber;
											 String numstr = null;											 
											 if (!list.isEmpty()){
												 numstr = (String) list.get(list.size()-1);
												 int temp = Integer.parseInt(numstr)+1;
												 setnumber = temp+1;
											 }
											 else{
												 setnumber = reload_setnumber;
											 }
										 list.add(Integer.toString(setnumber));
										 //((ArrayList<String>)ret.get("setnumbers")).add(Integer.toString(reload_setnumber));
									 }else{
										 ((ArrayList<String>)ret.get("titles")).add(title);
										 List list = (ArrayList<String>)ret.get("setnumbers");
										 Integer setnumber = getSetnumberFromTitle(title); 
										 if (setnumber == null){
											 String numstr = null;											 
											 if (!list.isEmpty()){
												 numstr = (String) list.get(list.size()-1);
												 int temp = Integer.parseInt(numstr)+1;
												 setnumber = temp+1;
											 }
											 else{
												 setnumber = reload_setnumber;
											 }
										 }
										 list.add(Integer.toString(setnumber));
									 }
									 ((ArrayList<String>)ret.get("thumbs")).add(thumb);
									 ((ArrayList<String>)ret.get("guests")).add(guest);
									 ((ArrayList<String>)ret.get("descs")).add(desc);
									 ((ArrayList<String>)ret.get("times")).add(time);
								 }
								 
								 reload_setnumber++;
							 }						 
						 }
					 }
				 }
			}
		}
		clearEmptyDocMap(ret);
		return ret;
	}
	
	private Map getDocInfo(String id, Map xpathmap, List reload)throws XPathExpressionException, HttpException, IOException{
		Map ret = new HashMap<String, ArrayList<String>>();
		Iterator ite = xpathmap.keySet().iterator();
		while(ite.hasNext()){
			ret.put(ite.next(), new ArrayList<String>());
		}
		for(int i=0;i<reload.size();i++){
			String reloadstr = (String) reload.get(i);
			String http = new String("http://www.youku.com/show_point_id_"+id+".html?dt=json&divid=point_"+reloadstr+"&tab=0");
			LOG.debug(http);
			Map docmap = ConfUtil.getDocElements(HttpUtil.getDoc(http), xpathmap);
			ite = xpathmap.keySet().iterator();
			while(ite.hasNext()){
				String key = (String) ite.next();
				((ArrayList<String>)ret.get(key)).addAll((ArrayList<String>)docmap.get(key));
			}
		}

		clearEmptyDocMap(ret);
		
		return ret;
	}
	
	private Integer getSetnumberFromTitle(String title){
		List regexs = new ArrayList<String>();
		//37 爱回家
		regexs.add("^(\\d+)\\s\\S+$");
		//第027话 机器少女我爱你
		regexs.add("^第(\\d+)[\u4e00-\u9fa5]\\s\\S+$");
		//爱·回家 34
		regexs.add("^\\S+\\s(\\d+)$");
		//那英 李敏镐 120218
		regexs.add("^\\D+\\s(\\d+)$");
		//第021话 喜欢 喜欢 我喜欢你
		regexs.add("^第(\\d+)[\u4e00-\u9fa5]\\s.+$");
		//理论01 机动车
		regexs.add("^[\u4e00-\u9fa5]*(\\d+)[\u4e00-\u9fa5]*\\s.+$");
		//场地81限速通过限宽门引导驾驶模拟练习
		//regexs.add("^[\u4e00-\u9fa5]+(\\d+)[\u4e00-\u9fa5]+.+$");
		String[] numarray = null;
		Iterator regexite = regexs.iterator();
		while(regexite.hasNext()){
			String regex = (String) regexite.next();
			numarray = StringUtil.splitStringPattern(title, regex);
			if (numarray != null){
				break;
			}
		}
		if (numarray == null){
			LOG.error("getSetnumberFromTitle error,title:"+title);
			return null;
		}
		int num = 0;
		try{
			num = Integer.parseInt(numarray[0]);
		}catch(NumberFormatException e){
			LOG.error("getSetnumberInfo error:"+e.getMessage());
			return null;
		}
		return num;
	}
	
	private List getSetnumberInfo(List titles){
//		List regexs = new ArrayList<String>();
//		//37 爱回家
//		regexs.add("^(\\d+)\\s\\S+$");
//		//第027话 机器少女我爱你
//		regexs.add("^第(\\d+)[\u4e00-\u9fa5]\\s\\S+$");
//		//爱·回家 34
//		regexs.add("^\\S+\\s(\\d+)$");
//		//那英 李敏镐 120218
//		regexs.add("^\\D+\\s(\\d+)$");
//		//第021话 喜欢 喜欢 我喜欢你
//		regexs.add("^第(\\d+)[\u4e00-\u9fa5]\\s.+$");
//		//理论01 机动车
//		regexs.add("^[\u4e00-\u9fa5]*(\\d+)[\u4e00-\u9fa5]*\\s.+$");
//		//场地81限速通过限宽门引导驾驶模拟练习
//		regexs.add("^[\u4e00-\u9fa5]+(\\d+)[\u4e00-\u9fa5]+.+$");
		List ret = new ArrayList<String>();
		Iterator ite = titles.iterator();
		while(ite.hasNext()){
			String title = (String) ite.next();
			//37 爱回家
//			String[] numarray = StringUtil.splitStringPattern(title, "^(\\d+)\\s\\S+$");
//			if (numarray == null){
//				//第027话 机器少女我爱你
//				numarray = StringUtil.splitStringPattern(title, "^第(\\d+)[\u4e00-\u9fa5]\\s\\S+$");
//			}if (numarray == null){
//				//爱·回家 34
//				numarray = StringUtil.splitStringPattern(title, "^\\S+\\s(\\d+)$");
//			}
//			if (numarray == null){
//				//那英 李敏镐 120218
//				numarray = StringUtil.splitStringPattern(title, "^\\D+\\s(\\d+)$");
//			}
//			if (numarray == null){
//				//第021话 喜欢 喜欢 我喜欢你
//				numarray = StringUtil.splitStringPattern(title, "^第(\\d+)[\u4e00-\u9fa5]\\s.+$");
//			}
//			if (numarray == null){
//				//理论01 机动车
//				numarray = StringUtil.splitStringPattern(title, "^[\u4e00-\u9fa5]*(\\d+)[\u4e00-\u9fa5]*\\s.+$");
//			}
//			String[] numarray = null;
//			Iterator regexite = regexs.iterator();
//			while(regexite.hasNext()){
//				String regex = (String) regexite.next();
//				numarray = StringUtil.splitStringPattern(title, regex);
//				if (numarray != null){
//					break;
//				}
//			}
//			if (numarray == null){
//				LOG.error("getSetnumberInfo error,title:"+title);
//				return null;
//			}
//			int num = 0;
//			try{
//				num = Integer.parseInt(numarray[0]);
//			}catch(NumberFormatException e){
//				LOG.error("getSetnumberInfo error:"+e.getMessage());
//				return null;
//			}
			Integer num = getSetnumberFromTitle(title);
			if (num == null){
				return null;
			}
			ret.add(Integer.toString(num));
		}
		return ret;
	}
	
	private String getVid(String link) {
		String vid = link.substring(link.indexOf("id_")+3);
		vid = vid.substring(0, vid.length()-5);
		return vid;
	}
	
	private int getDocMapSize(Map docmap) {
		Iterator docmapkeyite = docmap.keySet().iterator();
		int cursize=0,size=-1;
		while(docmapkeyite.hasNext()){
			String docmapkey = (String) docmapkeyite.next();
			cursize = ((ArrayList<String>)docmap.get(docmapkey)).size();
			if (size == -1){
				size = cursize;
			}
			if (size != cursize){
				LOG.error("Anylyse error, docmap size not match, curkey:"+docmapkey+",cursize:"+cursize+",size:"+size);
				return -1;
			}
		}
		return cursize;
	}
	private int addVideoInfo(
			String type,
			String sl_type,
			String area,
			String title,
			String alias,
			String score,
			String pub,
			String actor,
			String director,
			String duration,
			String simpledesc,
			String detaildesc,
			String douban_score,
			Integer durationi,
			Integer ding,
			Integer cai,
			Integer totalvodi
			){
		VideoBaseinfoTbl info = new VideoBaseinfoTbl();
		short fcid = (short)addFc(type);	
		short acid = -1;
		if (area == null){
			acid = (short)addAreainfo("未知");
		}else{
			acid = (short)addAreainfo(area);
		}
		info.setVbName(title);
		info.setVbYear(pub);
		info.setFcId(fcid);
		info.setVbAvescore(score);
		info.setVbActor(actor);
		info.setAcId(acid);
		info.setVbAlias(alias);
		info.setVbDirector(director);
		info.setVbDoubanscore(douban_score);
		if (durationi != null)
		info.setVbDuration(durationi.shortValue());
		else
			info.setVbDuration(null);
		info.setVbFirstsource((short)1);
		info.setVbLongdesc(detaildesc);
		info.setVbSimpledesc(simpledesc);
		info.setVbTotalfavor(ding);
		info.setVbTotalstep(cai);
		info.setVbTotalvod(totalvodi);
		info.setVbPinyin(PinyinUtil.getPinYin(title));
		info.setVbFirstletter(PinyinUtil.getPinYinHeadChar(title));
		int vbid = addVideoBaseinfo(info);
		String[] sltypeArray = sl_type.split("/");
		short[] scidArray = new short[sltypeArray.length];
		for(int i=0;i<sltypeArray.length;i++){
			short scid = (short)addSc(fcid,sltypeArray[i]);
			scidArray[i] = scid;
		}
		addScVideo(scidArray, vbid);
		return vbid;
	}
	private void addCrawlInfo(
			int vbid, 
			String vid, 
			String type, 
			String sl_type,
			String detailurl,
			String score,
			Integer ding,
			Integer cai,
			Integer settotali,
			Integer totalvodi,
			Integer lastseti
			){
		VideoCrawlinfoTbl crawlinfo = new VideoCrawlinfoTbl();
	    crawlinfo.setVbId(vbid);
	    crawlinfo.setCsId((short)1);
	    crawlinfo.setVcVid(vid);
	    crawlinfo.setVcOrifc(type);
	    crawlinfo.setVcOrisc(sl_type);
	    crawlinfo.setVcDetailurl(detailurl);
	    crawlinfo.setVcSetlast(lastseti);
	    crawlinfo.setVcSettotal(settotali);
	    crawlinfo.setVcFavor(ding);
	    crawlinfo.setVcStep(cai);
	    crawlinfo.setVcScore(score);
	    crawlinfo.setVcVod(totalvodi);
	    crawlinfo.setVcCrawldate(new Date());
	    addCrawlinfo(crawlinfo);
	}
	private void addEpisodeSource(Map episodesource){
		Set episodesourceset = episodesource.entrySet();
		Iterator episodesourceite = episodesourceset.iterator();
		while(episodesourceite.hasNext()){
			Entry entry = (Entry) episodesourceite.next();
			String key = (String) entry.getKey();
			Map playmap = (Map) entry.getValue();
			Set playset = playmap.entrySet();
			Iterator playsetite = playset.iterator();
			while(playsetite.hasNext()){
				Entry playentry = (Entry)playsetite.next();
				VideoPlayinfoTbl playinfo = new VideoPlayinfoTbl();
				playinfo.setVsSubvid(key);
				if (playentry.getKey().equals(Constants.NORMAL)){
					playinfo.setVpDefinition(Constants.NORMAL_DEFINITION);
				}else if (playentry.getKey().equals(Constants.HIGH)){
					playinfo.setVpDefinition(Constants.HIGH_DEFINITION);
				}else if (playentry.getKey().equals(Constants.SUPER)){
					playinfo.setVpDefinition(Constants.SUPER_DEFINITION);
				}
				playinfo.setVpPlayurl((String)playentry.getValue());
				addPlayinfo(playinfo);
			}			
		}
	}
	private void addDocMap(Map docmap, int vbid, String host){
		int size = getDocMapSize(docmap);	    
		String subvid=null,sublink=null,subduration=null,subtitle=null,subdesc=null,subthumb=null,subsetnumber=null,subguest=null;
		if (size != -1){
			List sublinks = (List) docmap.get("links");
			List subtimes =  (List) docmap.get("times");
			List subtitles = (List) docmap.get("titles");
			List subdescs = (List) docmap.get("descs");
			List subthumbs = (List) docmap.get("thumbs");
			List subguests = (List) docmap.get("guests");
			List subsetnumbers = (List) docmap.get("setnumbers");
			
			for(int i=0;i<size;i++){
				VideoSourceinfoTbl sourceinfo = new VideoSourceinfoTbl();
				if (sublinks != null){
					sublink = (String) sublinks.get(i);
				}
				if (subtimes != null){
					subduration = (String) subtimes.get(i);
				}
				if (subtitles != null){
					subtitle = (String) subtitles.get(i);
				}
				if (subdescs != null){
					subdesc = (String) subdescs.get(i);
				}
				if (subsetnumbers != null){
					subsetnumber = (String) subsetnumbers.get(i);
				}
				if (subguests != null){
					subguest = (String) subguests.get(i);
				}
				if (subthumbs != null){
					subthumb = (String) subthumbs.get(i);
				}
				if(sublink != null){
					subvid = getVid(sublink);
				}
				sourceinfo.setVsGuest(subguest);
				sourceinfo.setVsHost(host);
				sourceinfo.setVsSetnumber(subsetnumber);
				sourceinfo.setVsSourceurl(sublink);
				sourceinfo.setVsSubdesc(subdesc);
				sourceinfo.setVsSubduration(subduration);
				sourceinfo.setVsSubtitle(subtitle);
				sourceinfo.setVsSubthumb(subthumb);
				sourceinfo.setVsSubvid(subvid);
				sourceinfo.setVbId(vbid);
				sourceinfo.setCsId((short)1);
				addSourceinfo(sourceinfo);
			}	
		}
	}
		
	private void checkSetNumber(Map docmap, String type, List orisetnumbers){
		List setnumbers = new ArrayList<String>();
		if (type.equals("教育") || type.equals("记录片")){
			for(int i=0;i<orisetnumbers.size();i++){
				setnumbers.add(Integer.toString(i));
			}
			LOG.info("rebuild docmap setnumber list, for '教育' and '纪录片' type");
			docmap.put("setnumbers", setnumbers);
		}else {
			docmap.put("setnumbers", orisetnumbers);
		}
	}
	
	public boolean inputPlayInfo(Map map){
		String type  = (String) map.get("type");
		if (!(
				type.equals("电影")||
				type.equals("电视剧")||
				type.equals("动漫")||
				type.equals("教育")||
				type.equals("纪录片")
				)){
			LOG.info("Not anylyse this page,for it is not '电影','电视剧','动漫','教育','纪录片' type");
			return false;
		}
		return false;
	}
	
	public boolean inputVideoInfo(
			Map map
			)
	{
		String type  = (String) map.get("type");
		if (!(
				type.equals("电影")||
				type.equals("电视剧")||
				type.equals("动漫")||
				type.equals("教育")||
				type.equals("纪录片")
				)){
			LOG.info("Not anylyse this page,for it is not '电影','电视剧','动漫','教育','纪录片' type");
			return false;
		}
		String title = (String) map.get("title");
	    String link = (String) map.get("link");
	    String alias = (String) map.get("alias");
		String pub  = (String) map.get("pub");
		String score  = (String) map.get("score");
		String douban_score  = (String) map.get("douban_score");
		String favor  = (String) map.get("favor");
		String area  = (String) map.get("area");
		String sl_type  = (String) map.get("sl_type");
		String actor  = (String) map.get("actor");
		String  director=(String) map.get( "director");
		String totalvod=(String) map.get("totalvod");
		String settotal=(String) map.get("settotal");
		String duration = (String) map.get("duration");
		String host = (String)map.get("host");
		String img = (String)map.get("img");
		String simpledesc = (String)map.get("simpledesc");
		String detaildesc = (String)map.get("detaildesc");
		String head_script = (String)map.get("head-script");
		List<String> SeriesTab = (List<String>)map.get("zySeriesTab");
		Object episode = map.get("episode");		

		Integer ding=null,cai=null,totalvodi=null,settotali=null,lastseti=null,durationi=null;
		if (favor != null){
			Integer[] ret2 = StringUtil.getDoubleInt(favor.replace(",", ""), "有(\\d+)人顶过有(\\d+)人踩过");
				ding = ret2[0];
				cai = ret2[1];
		}
		if (settotal != null){
			if (settotal.indexOf("/") != -1) {
				Integer[] ret2 = StringUtil.getDoubleInt(settotal, "更新至(\\d+)/共(\\d+)集");
				lastseti = ret2[0];
				settotali = ret2[1];
			}else{
				settotali = StringUtil.getSingleInt(settotal, "共(\\d+)集");
				lastseti = StringUtil.getSingleInt(settotal, "更新至(\\d+)");
			}
		}
		if (totalvod != null){
			totalvodi = Integer.parseInt(totalvod.replace(",", ""));
		}
		if (duration != null){
			durationi = StringUtil.getSingleInt(duration, "(\\d+)分钟");
				if (durationi == null)
					durationi = Integer.parseInt(duration.replace(",", ""));
		}				
		
		String vid=null;
		if (link != null){
			vid = getVid(link);
		}
		String id = null;
		if (head_script != null){
			id = head_script.substring(head_script.indexOf("id:'")+4);
			id = id.substring(0, id.indexOf("'"));
		}
		String detailurl = new String("http://www.youku.com/show_page/id_"+id+".html");

		LOG.info(
				"title:"+title+//ConfUtil.getFieldValue(doc, crawlconf, "title")+
				",alias:"+alias+
				",pub:"+pub+//ConfUtil.getFieldValue(doc, crawlconf, "pub")+
				",type:"+type+//ConfUtil.getFieldValue(doc, crawlconf, "type")+
				",score:"+score+//ConfUtil.getFieldValue(doc, crawlconf, "score")+
				",douban_score:"+douban_score+//ConfUtil.getFieldValue(doc, crawlconf, "douban_score")+
				",favor:"+favor+//ConfUtil.getFieldValue(doc, crawlconf, "favor")+
				",ding:"+ding+
				",cai:"+cai+
				",area:"+area+//ConfUtil.getFieldValue(doc, crawlconf, "area")+
				",sl_type:"+sl_type+//ConfUtil.getFieldValue(doc, crawlconf, "sl_type")+
				",actor:"+actor+//ConfUtil.getFieldValue(doc, crawlconf, "actor")+
				",director:"+director+//ConfUtil.getSingleValue(doc, crawlconf, "director")+
				",totalvod:"+totalvodi+//ConfUtil.getFieldValue(doc, crawlconf, "totalvod")+
				",settotal:"+settotali+//ConfUtil.getFieldValue(doc, crawlconf, "setnumber")//+
				",lastset:"+lastseti+
				",duration:"+durationi+//ConfUtil.getFieldValue(doc, crawlconf, "duration")
				",host:"+host+
				//",guest:"+guest+
				",img:"+img+
				//",ititle:"+ititle+
				",simpledesc:"+simpledesc+
				",detaildesc:"+detaildesc+
				",link:"+link+
				",vid:"+vid+
				",id:"+id+
				",detailurl:"+detailurl
				);
		
		Map sourcemap = null,docmap = null; 
		Map episodesource = new HashMap<String,HashMap<String, String>>();
		Map xpathmap = new HashMap();
		Field thumbs = new Field(),times = new Field(),titles = new Field(),links = new Field(),descs = new Field(),guests = new Field();
		thumbs.setXpath("//DIV[@class='thumb']/IMG/@src");
		times.setXpath("//DIV[@class='time']/SPAN[@class='num']/text()");
		titles.setXpath("//DIV[@class='title']/A/text()");
		links.setXpath("//DIV[@class='title']/A/@href");
		descs.setXpath("//DIV[@class='desc']/text()");
		guests.setXpath("//DIV[@class='guestlist']");
		guests.setOption("walk");
		xpathmap.put("thumbs", thumbs);
		xpathmap.put("times", times);
		xpathmap.put("titles", titles);
		xpathmap.put("links", links);
		xpathmap.put("descs", descs);
		xpathmap.put("guests", guests);
		try{
//			sourcemap = HttpUtil.getM3U8Map(vid);
//			Iterator ite = sourcemap.keySet().iterator();
//			while(ite.hasNext()){
//				String key = (String)ite.next();
//				LOG.debug(vid+"---"+key+":"+sourcemap.get(key));
//			}
//			episodesource.put(vid, sourcemap);
			if (episode != null){
				if (SeriesTab == null){
					List reload = new ArrayList<String>();
					reload.add("reload_1");
					docmap = getDocInfoNew(id,reload);
					List setnumbers =  (List) docmap.get("setnumbers");
					checkSetNumber(docmap,type,setnumbers);
				}
				else{
					for(int i=0;i<SeriesTab.size();i++){
						LOG.debug(SeriesTab.get(i));
					}
					docmap = getDocInfoNew(id,SeriesTab);
					List setnumbers =  (List) docmap.get("setnumbers");
					checkSetNumber(docmap,type,setnumbers);
				}
				Iterator keyite = docmap.keySet().iterator();
				while(keyite.hasNext()){
					String key = (String) keyite.next();
					ArrayList<String> list = (ArrayList<String>) docmap.get(key);
					if (list != null){
						Iterator<String > strite = list.iterator();
						while(strite.hasNext()){
							LOG.debug(key+":"+strite.next());
						}
					}
				}
				episodesource = null;
				episodesource = new HashMap<String,HashMap<String, String>>();
				ArrayList<String> episodelinklist = (ArrayList<String>) docmap.get("links");
				Iterator episodelinklistite = episodelinklist.iterator();
				while(episodelinklistite.hasNext()){
					String episodelink = (String) episodelinklistite.next();
					String linkid = getVid(episodelink);
					Map episodemap = HttpUtil.getM3U8Map(linkid);
					Iterator ite1 = episodemap.keySet().iterator();
					while(ite1.hasNext()){
						String key = (String)ite1.next();
						LOG.debug(linkid+"---"+key+":"+episodemap.get(key));
					}
					episodesource.put(linkid,episodemap);
				}
			}
			else{
				docmap = new HashMap<String, ArrayList<String>>();
				ArrayList<String> sigle_times = new ArrayList<String>();
				sigle_times.add(duration);
				ArrayList<String> sigle_titles = new ArrayList<String>();
				sigle_titles.add(title);
				ArrayList<String> sigle_links = new ArrayList<String>();
				sigle_links.add(link);
				ArrayList<String> sigle_descs = new ArrayList<String>();
				sigle_descs.add(detaildesc);
				ArrayList<String> sigle_setnumbers = new ArrayList<String>();
				sigle_setnumbers.add(Integer.toString(1));
				docmap.put("times", sigle_times);
				docmap.put("titles", sigle_titles);
				docmap.put("links", sigle_links);
				docmap.put("descs", sigle_descs);
				docmap.put("setnumbers", sigle_setnumbers);

				sourcemap = HttpUtil.getM3U8Map(vid);
				Iterator ite = sourcemap.keySet().iterator();
				while(ite.hasNext()){
					String key = (String)ite.next();
					LOG.debug(vid+"---"+key+":"+sourcemap.get(key));
				}
				episodesource.put(vid, sourcemap);
			}
		}catch(ParseException e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		}catch(HttpException e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		}catch(IOException e){
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		
//		VideoBaseinfoTbl info = new VideoBaseinfoTbl();
//		short fcid = (short)addFc(type);	
//		short acid = -1;
//		if (area == null){
//			acid = (short)addAreainfo("未知");
//		}else{
//			acid = (short)addAreainfo(area);
//		}
//		info.setVbName(title);
//		info.setVbYear(pub);
//		info.setFcId(fcid);
//		info.setVbAvescore(score);
//		info.setVbActor(actor);
//		info.setAcId(acid);
//		info.setVbAlias(alias);
//		info.setVbDirector(director);
//		info.setVbDoubanscore(douban_score);
//		if (durationi != null)
//		info.setVbDuration(durationi.shortValue());
//		else
//			info.setVbDuration(null);
//		info.setVbFirstsource((short)1);
//		info.setVbLongdesc(detaildesc);
//		info.setVbSimpledesc(simpledesc);
//		info.setVbTotalfavor(ding);
//		info.setVbTotalstep(cai);
//		info.setVbTotalvod(totalvodi);
//		info.setVbPinyin(PinyinUtil.getPinYin(title));
//		info.setVbFirstletter(PinyinUtil.getPinYinHeadChar(title));
//		int vbid = addVideoBaseinfo(info);
//		String[] sltypeArray = sl_type.split("/");
//		short[] scidArray = new short[sltypeArray.length];
//		for(int i=0;i<sltypeArray.length;i++){
//			short scid = (short)addSc(fcid,sltypeArray[i]);
//			scidArray[i] = scid;
//		}
//		addScVideo(scidArray, vbid);
		int vbid = addVideoInfo(
				type,
				sl_type,
				area,
				title,
				alias,
				score,
				pub,
				actor,
				director,
				duration,
				simpledesc,
				detaildesc,
				douban_score,
				durationi,
				ding,
				cai,
				totalvodi
				);
//	    VideoCrawlinfoTbl crawlinfo = new VideoCrawlinfoTbl();
//	    crawlinfo.setVbId(vbid);
//	    crawlinfo.setCsId((short)1);
//	    crawlinfo.setVcVid(vid);
//	    crawlinfo.setVcOrifc(type);
//	    crawlinfo.setVcOrisc(sl_type);
//	    crawlinfo.setVcDetailurl(detailurl);
//	    crawlinfo.setVcSetlast(lastseti);
//	    crawlinfo.setVcSettotal(settotali);
//	    crawlinfo.setVcFavor(ding);
//	    crawlinfo.setVcStep(cai);
//	    crawlinfo.setVcScore(score);
//	    crawlinfo.setVcVod(totalvodi);
//	    crawlinfo.setVcCrawldate(new Date());
//	    addCrawlinfo(crawlinfo);
		addCrawlInfo(
				vbid, 
				vid, 
				type, 
				sl_type,
				detailurl,
				score,
				ding,
				cai,
				settotali,
				totalvodi,
				lastseti
				);
	    addDocMap(docmap, vbid, host);
	    int size = getDocMapSize(docmap);	
		if (size != -1){
			addEpisodeSource(episodesource);
		}	
		return true;
	}
}
