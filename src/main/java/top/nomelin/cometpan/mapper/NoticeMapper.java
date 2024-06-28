package top.nomelin.cometpan.mapper;

import top.nomelin.cometpan.pojo.Notice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoticeMapper {

    /**
     * 新增公告
     */
    int insert(Notice notice);

    /**
     * 删除公告
     */
    @Delete("delete from notice where id = #{id}")
    int deleteById(Integer id);

    /**
     * 修改公告
     */
    int updateById(Notice notice);

    /**
     * 查询所有
     */
    List<Notice> selectAll(Notice notice);

    /**
     * 带标题进行查询
     */
    @Select("select * from notice where title LIKE CONCAT('%', #{title}, '%')")
    List<Notice> selectByTitle(String title);

    /**
     * 无条件查询所有
     */
    @Select("select * from notice")
    List<Notice> searchAll();
}
