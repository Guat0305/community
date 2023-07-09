package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("ZHUGELIANG");
        user.setPassword("112233");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int i = userMapper.insertUser(user);
        System.out.println(i + ":" + user.getId());
    }

    @Test
    public void testUpdate() {
        int i = userMapper.updateStatus(150, 1);
        System.out.println(i);

        i = userMapper.updateHeader(150,"http://www.nowcoder.com/102.png");
        System.out.println(i);

        i = userMapper.updatePassword(150,"hello ");
        System.out.println(i);

    }

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket () {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("abc");
        loginTicket.setUserId(101);
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date());
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc" , 0);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversation(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int row = messageMapper.selectConversationCount(111);
        System.out.println(row);

        list = messageMapper.selectLetter("111_112" , 0 , 10);
        for (Message message : list) {
            System.out.println(message);
        }

        row = messageMapper.selectLetterCount("111_112");
        System.out.println(row);

        row = messageMapper.selectLetterUnreadCount(131 , "111_131");
        System.out.println(row);

    }

}
