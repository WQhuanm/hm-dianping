package com.hmdp.Vo;

import lombok.Data;

import java.util.List;

@Data
public class ScrollVo {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
