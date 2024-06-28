package top.nomelin.cometpan.controller;

import com.github.pagehelper.PageInfo;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.listener.MessagePublisher;
import top.nomelin.cometpan.pojo.Notice;
import top.nomelin.cometpan.service.NoticeService;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    public static final Logger logger = LoggerFactory.getLogger(NoticeController.class);

    private final MessagePublisher messagePublisher;

    @Autowired
    public NoticeController() {
        // 初始化MessagePublisher
        this.messagePublisher = new MessagePublisher("192.168.10.100", "admin", "58dc706e", "test");
    }


    /**
     * 新增公告
     */
    @PostMapping("/add")
    public Result add(@RequestBody Notice notice) {
        noticeService.add(notice);
        // 发布消息到RabbitMQ
        messagePublisher.publishMessage("新增公告：" + notice.getContent());
        return Result.success(notice);
    }

    /**
     * 查询公告
     */
    @GetMapping("/selectPage")
    public Result selectNotice(Notice notice, @RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<Notice> noticePageInfo = noticeService.selectFilePage(notice, pageNum, pageSize);
        return Result.success(noticePageInfo);
    }

    /**
     * 查询所有公告
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Notice> noticeList = noticeService.selectAllNo();
        return Result.success(noticeList);
    }


    /**
     * 更新公告
     */
    @PutMapping("/update")
    public Result update(@RequestBody Notice notice) {
        noticeService.updateById(notice);
        return Result.success("更新成功");
    }


    /**
     * 删除公告
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        noticeService.deleteById(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除公告
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        for (Integer id : ids) {
            noticeService.deleteById(id);
        }
        return Result.success();
    }
}
