package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，每个会话显示最新的私信
    List<Message> selectConversation(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话的全部内容
    List<Message> selectLetter(String conversationId, int offset, int limit);

    // 查询某个会话的全部私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信数量
    int selectLetterUnreadCount(int userId, String conversationId);

    //新增消息
    int insertMessage(Message message);

    // 更改消息状态
    int updateStatus(List<Integer> ids, int status);

}
