package top.keithfeng.mapper;

import top.keithfeng.domain.DynamicHistory;

import java.util.List;

/**
* @author cisdi
* @description 针对表【dynamic_history】的数据库操作Mapper
* @createDate 2023-06-05 14:32:18
* @Entity top.keithfeng.domain.DynamicHistory
*/
public interface DynamicHistoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(DynamicHistory record);

    int insertSelective(DynamicHistory record);

    DynamicHistory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DynamicHistory record);

    int updateByPrimaryKey(DynamicHistory record);

    List<DynamicHistory> selectAll();
}
