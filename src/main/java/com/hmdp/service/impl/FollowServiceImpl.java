package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private IUserService userService;

    @Override
    public Result isFollow(long id) {
        Integer count = query().eq("user_id", UserHolder.getUser().getId()).eq("follow_user_id", id).count();
        return Result.ok(count > 0);
    }

    @Override
    public Result follow(long id, boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        if (isFollow) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(id);
            if (save(follow)) return Result.ok("关注成功");
            return Result.fail("关注失败");
        } else {
            boolean success = remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", id));
            if (success) return Result.ok("取关成功");
            return Result.fail("取关失败");
        }
    }

    @Override
    public Result followCommons(long id) {
        Long userId = UserHolder.getUser().getId();
        List<Follow> users = query().eq("user_id", userId).list();
        Set<Long> collect = users.stream().map(u -> u.getFollowUserId()).distinct().collect(Collectors.toSet());
        List<Follow> followUsers = query().eq("user_id", id).list();
        List<Long> ids = followUsers.stream().
                map(u -> u.getFollowUserId()).filter(collect::contains).distinct().collect(Collectors.toList());
        if (ids == null || ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<UserDTO> commons = userService.listByIds(ids).stream()
                .map(u -> BeanUtil.copyProperties(u, UserDTO.class)).collect(Collectors.toList());
        return Result.ok(commons);
    }
}
