package com.nowcoder.community.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader()
                        .getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    // 将敏感词加入前缀树
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i = 0;i < keyword.length();i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c , subNode);
            }

            // 指向子节点进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if(i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *  text 要过滤的文本
     * 返回过滤后的文本
     */
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if(isSymbol(c)) {
                // 如果指针1处于根节点，将此符号计入结果，让指针2向下走
                if(tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null) {
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if(tempNode.isKeywordEnd()) {
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else {
                //检查下一个字符
                if(position < text.length() - 1) {
                    position++;
                }
            }

        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    public boolean isSymbol(Character character) {
        //  0x2e80-0x9fff 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2e80 || character > 0x9fff);
    }

    // 前缀树
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character character, TrieNode node) {
            subNodes.put(character, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character character) {
            return subNodes.get(character);
        }
    }

}
