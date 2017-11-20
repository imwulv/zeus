/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.service.impl.user;

import com.f6car.base.dao.user.TbUserMapper;
import com.f6car.base.po.user.TbUser;
import com.f6car.base.vo.user.TbUserVo;
import com.f6car.base.so.user.TbUserSo;
import com.f6car.base.service.user.TbUserService;
import com.f6car.base.core.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
 * Created by CodeGenerator on 2017-11-20.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TbUserServiceImpl extends AbstractService<TbUser, TbUserVo, TbUserSo> implements TbUserService {
    @Resource
    private TbUserMapper tbUserMapper;
}
