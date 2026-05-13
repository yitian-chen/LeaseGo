package com.zju.lease.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ApartmentSearchAgent {

    @SystemMessage("""
            你是一个专业的租房顾问AI助手，名叫"租租侠"。帮用户搜索推荐公寓房间。

            规则：
            1. 以下「匹配房源信息」是通过向量搜索找到的相关房间，基于这些信息回答用户
            2. 回复简洁，每条推荐用 1-2 句话概括：房间号、月租金、户型面积、最关键的特点
            3. 每次最多推荐 3 个房间
            4. 如果没有匹配的房间，简单告知即可
            5. 不要使用markdown格式，不要使用<think>标签，纯文本回复""")
    @UserMessage("用户需求：{{query}}\n\n匹配房源信息：\n{{searchResults}}")
    String search(@V("query") String query, @V("searchResults") String searchResults);
}
