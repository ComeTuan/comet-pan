package top.nomelin.cometpan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.cache.CurrentUserCache;
import top.nomelin.cometpan.mapper.NoticeMapper;
import top.nomelin.cometpan.pojo.FileMeta;
import top.nomelin.cometpan.pojo.Notice;
import top.nomelin.cometpan.pojo.User;
import top.nomelin.cometpan.service.NoticeService;
import org.aspectj.weaver.ast.Not;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {

    private final static Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private CurrentUserCache currentUserCache;

    /**
     * 新增公告
     */
    @Override
    public int add(Notice notice) {
        User currentUser = currentUserCache.getCurrentUser();
        Date date = new Date(System.currentTimeMillis());
        notice.setTime(date);
        notice.setUserId(currentUser.getId());
        logger.info("新增公告成功:{}",notice.getTitle());
        return noticeMapper.insert(notice);
    }

    /**
     * 根据ID删除
     */
    @Override
    public void deleteById(Integer id) {
        noticeMapper.deleteById(id);
    }

    /**
     * 修改公告
     */
    @Override
    public int updateById(Notice notice) {
        User currentUser = currentUserCache.getCurrentUser();
        Date date = new Date(System.currentTimeMillis());
        notice.setTime(date);
        notice.setUserId(currentUser.getId());
        return noticeMapper.updateById(notice);
    }

    /**
     * 查询所有公告
     */
    @Override
    public List<Notice> selectAll(Notice notice) {
        return noticeMapper.selectAll(notice);
    }

    @Override
    public List<Notice> selectAllNo() {
        return noticeMapper.searchAll();
    }

    /**
     * 带标题查询公告
     */
    @Override
    public PageInfo<Notice> selectPage(String title, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notice> noticeList = noticeMapper.selectByTitle(title);
        return PageInfo.of(noticeList);
    }

    @Override
    public PageInfo<Notice> selectFilePage(Notice notice, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notice> noticeList = noticeMapper.selectAll(notice);
        return PageInfo.of(noticeList);
    }

/*    @Override
    public PageInfo<Notice> selectFilePage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<Notice>(noticeMapper.searchAll());
    }*/
}
