package com.school.community.controller;

import com.school.community.annotation.LoginRequired;
import com.school.community.entity.User;
import com.school.community.service.FollowService;
import com.school.community.service.LikeService;
import com.school.community.service.UserService;
import com.school.community.util.CommunityConstant;
import com.school.community.util.CommunityUtil;
import com.school.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 生成上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if(StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }


//    @LoginRequired
//    @RequestMapping(path = "/upload", method = RequestMethod.POST)
//    public String uploadHeader(MultipartFile headerImage, Model model) {
//        if (headerImage == null) {
//            model.addAttribute("error", "您还没有选择图片");
//            return "/site/setting";
//        }
//
//        String fileName = headerImage.getOriginalFilename();
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        if (StringUtils.isBlank(suffix)) {
//            model.addAttribute("error", "文件格式不正确");
//            return "/site/setting";
//        }
//
//        //生成文件名
//        fileName = CommunityUtil.generateUUID() + suffix;
//        //存放路径
//        File dest = new File(uploadPath + "/" + fileName);
//        try {
//            headerImage.transferTo(dest);
//        } catch (IOException e) {
//            logger.error("上传文件失败: " + e.getMessage());
//            throw new RuntimeException("上传文件失败，服务器发送异常", e);
//        }
//
//        //更新当前用户图像路径
//        User user = hostHolder.getUser();
//        String headerUrl = domain + contextPath + "/user/header/" + fileName;
//        userService.updateHeader(user.getId(), headerUrl);
//
//        return "redirect:/index";
//
//    }

//    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
//    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
//        // 服务器存放路径
//        fileName = uploadPath + "/" + fileName;
//        // 文件后缀
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        // 响应图片
//        response.setContentType("image/" + suffix);
//        try (
//                FileInputStream fis = new FileInputStream(fileName);
//                OutputStream outputStream = response.getOutputStream();
//        ) {
//            byte[] buffer = new byte[1024];
//            int b = 0;
//            while ((b = fis.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, b);
//            }
//        } catch (IOException e) {
//            logger.error("读取头像失败: " + e.getMessage());
//        }
//
//    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        // 检查原密码是否错误
        if (!user.getPassword().equals(oldPassword)) {
            model.addAttribute("passwordMsg", "原始密码错误");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(), newPassword);
        return "redirect:/logout";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否对某用户已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),
                    ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

}
