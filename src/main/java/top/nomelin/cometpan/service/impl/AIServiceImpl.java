package top.nomelin.cometpan.service.impl;

import top.nomelin.cometpan.service.AIService;
import top.nomelin.cometpan.util.LangChainSDK;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author June
 * @version 1.0.0
 * @title AIServiceImpl
 * @description
 **/
@Service
public class AIServiceImpl implements AIService {
    @Autowired
    private LangChainSDK langChainSDK;


    @Override
    public String chatWithAI(String content) {
        return langChainSDK.chatWithAI(content).block();
    }
}
