package cn.jazz.dao;

import cn.jazz.domain.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ISysLogDao {

    @Insert("insert into sysLog(visitTime,ip,url,username,executionTime,method) values(#{visitTime},#{ip},#{url},#{username},#{executionTime},#{method})")
    public void save(SysLog sysLog) throws Exception;

    @Select("select * from sysLog")
    public List<SysLog> findAll() throws Exception;
}
