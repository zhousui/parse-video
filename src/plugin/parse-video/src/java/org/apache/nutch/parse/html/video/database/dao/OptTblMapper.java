package org.apache.nutch.parse.html.video.database.dao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OptTblMapper {
	
	@Select("select ac_id from area_category_tbl where ac_name=#{area}")
    public Integer getAcIdByNameFromAcTbl(@Param("area") String area);
    
    @Select("select last_insert_id()")
    public Integer getLastId();
	
    @Select("select fc_id from firstlevel_category_tbl where fc_name=#{fcname}")
    public Integer getFcIdByNameFromFcTbl(@Param("fcname") String fcname);
   	
    @Select("select sc_id from secondlevel_category_tbl where sc_name=#{scname}")
    public Integer getScIdByNameFromScTbl(@Param("scname") String scname);
		
    @Select("select vb_id from video_baseinfo_tbl where fc_id=#{type} and vb_name=#{title}")
    public Integer getVbIdByTypeTitleFromVbTbl(@Param("type") Short type,@Param("title") String title );
    
    @Select("select vb_id from video_baseinfo_tbl where fc_id=#{type} and vb_year=#{year} and vb_name=#{title}")
    public Integer getVbIdByTypeYearTitleFromVbTbl(@Param("type") Short type,@Param("year") String year,@Param("title") String title );
	
    @Select("select id from video_crawlinfo_tbl where vb_id=#{vbid}")
    public Integer getIdByVbidFromVcTbl(@Param("vbid") Integer vbid );
	
    @Select("select id from video_sc_ref_tbl where vb_id=#{vbid} limit 1")
    public Integer getIdByVbidFromScRefTbl(@Param("vbid") Integer vbid );
    
    @Select("select id from video_sourceinfo_tbl where vs_subvid=#{subvid}")
    public Integer getIdBySubvidFromVsTbl(@Param("subvid") String subvid );
    
    @Select("select id from video_playinfo_tbl where vs_subvid=#{subvid} and vp_definition=#{def}")
    public Integer getIdBySubvidDefFromVpTbl(@Param("subvid") String subvid, @Param("def") Short def);
}
