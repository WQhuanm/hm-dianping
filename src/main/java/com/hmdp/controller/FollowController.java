package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Autowired
    private IFollowService followService;

    @GetMapping("/or/not/{id}")
    public Result ifFollow(@PathVariable("id") long id) {
        return followService.isFollow(id);
    }

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") long id, @PathVariable("isFollow") boolean isFollow) {
        return followService.follow(id, isFollow);
    }
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable long id){
        return followService.followCommons(id);
    }

}
