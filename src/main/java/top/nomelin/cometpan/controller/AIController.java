package top.nomelin.cometpan.controller;

import org.springframework.web.bind.annotation.*;
import top.nomelin.cometpan.common.Result;
import top.nomelin.cometpan.service.AIService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/ai")
@Api(tags = "AI助手模块")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    @ApiOperation(value = "和本地知识库对话")
    public Result chatWithAI(@RequestBody String content) {
        String s = aiService.chatWithAI(content);
        return Result.success(s);
    }
}
