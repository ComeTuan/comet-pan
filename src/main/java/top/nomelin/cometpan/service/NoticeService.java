package top.nomelin.cometpan.service;

import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.pojo.Notice;
import io.swagger.models.auth.In;

import java.util.List;

public interface NoticeService {

    /**
     * 新增公告
     */
    int add(Notice notice);

    /**
     * 根据ID删除
     */
    void deleteById(Integer id);

    /**
     * 修改公告
     */
    int updateById(Notice notice);

    /**
     * 查询所有公告
     */
    List<Notice> selectAll(Notice notice);

    List<Notice> selectAllNo();

    /**
     * 带标题的分页查询
     */
    PageInfo<Notice> selectPage(String title, Integer pageNum, Integer pageSize);

    PageInfo<Notice> selectFilePage(Notice notice,Integer pageNum, Integer pageSize);
}
