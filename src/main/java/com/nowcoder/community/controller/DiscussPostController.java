package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {

        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录哦!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model,
                                 Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount" , likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId() , ENTITY_TYPE_POST,
                discussPostId);
        model.addAttribute("likeStatus" , likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());


        // 评论：给帖子的评论
        // 回复：给评论的评论

        // 评论列表
        List<Comment> commentlist = commentService.findCommentsByEntity(ENTITY_TYPE_POST,
                post.getId(),
                page.getOffset(),
                page.getLimit());

        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentlist != null) {
            for (Comment comment : commentlist) {
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));

                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount" , likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId() , ENTITY_TYPE_COMMENT,
                                comment.getId());
                commentVo.put("likeStatus" , likeStatus);

                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT ,
                        comment.getId() ,
                        0 ,
                        Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String,Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user" , userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null :
                                userService.findUserById(reply.getTargetId());
                        replyVo.put("target" , target);

                        // 点赞
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,
                                reply.getId());
                        replyVo.put("likeCount" , likeCount);
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId() , ENTITY_TYPE_COMMENT,
                                        reply.getId());
                        replyVo.put("likeStatus" , likeStatus);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replys" , replyVoList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,
                        comment.getId());
                commentVo.put("replyCount" , replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments" , commentVoList);

        return "/site/discuss-detail";
    }

}
