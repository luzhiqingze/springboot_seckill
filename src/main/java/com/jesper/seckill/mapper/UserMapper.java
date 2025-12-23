package com.jesper.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jesper.seckill.bean.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by jiangyunxiong on 2018/5/21.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
