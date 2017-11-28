package com.f6car.base.service.impl.user;

import com.f6car.base.dao.user.TbUserMapper;
import com.f6car.base.po.user.TbUser;
import com.f6car.base.vo.user.TbUserVo;
import com.f6car.base.so.user.TbUserSo;
import com.f6car.base.service.user.TbUserService;
import com.f6car.base.core.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;

import javax.annotation.Resource;


/**
 * Created by qixiaobo on 2017-11-28.
 */
 @Service
 @Transactional(rollbackFor = Exception.class,timeout=1)
public class TbUserServiceImpl extends AbstractService<TbUser,TbUserVo,TbUserSo,BigInteger> implements TbUserService {
    @Resource
    private TbUserMapper tbUserMapper;
}
