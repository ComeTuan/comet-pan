package top.nomelin.cometpan.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.nomelin.cometpan.pojo.DiskFile;

import java.util.List;


@Mapper
public interface DiskMapper {
    /**
     * 新增
     */
    int insert(DiskFile diskFile);

    /**
     * 删除文件
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(DiskFile diskFiles);

    /**
     * 根据ID查询
     */
    DiskFile selectById(Integer id);

    /**
     * 查询所有
     */
    List<DiskFile> selectAll(DiskFile diskFile);
}
