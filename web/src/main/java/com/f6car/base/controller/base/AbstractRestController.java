/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.controller.base;

import com.f6car.base.common.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author qixiaobo
 */
public abstract class AbstractRestController<V extends Vo, S extends So> {

    @Autowired
    private Service<V, S> service;


    @PostMapping()
    @ApiOperation(value = "新建实体", notes = "")
    public Result add(@RequestBody V vo) {
        service.save(vo);
        return ResultGenerator.genSuccessResult();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除实体", notes = "")
    public Result delete(@PathVariable Serializable id) {
        service.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }


    @PutMapping
    @ApiOperation(value = "更新实体", notes = "")
    public Result update(@RequestBody V vo) {
        service.update(vo);
        return ResultGenerator.genSuccessResult();
    }

    @GetMapping
    @ApiOperation(value = "获取实体列表", notes = "")
    public Result list(S so) {
        PageHelper.startPage(so.getCurrentPage(), so.getPageSize());
        List<V> list = service.findAll();
        PageInfo pageInfo = new PageInfo(list);
        return ResultGenerator.genSuccessResult(pageInfo);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取单个实体", notes = "")
    public Result detail(@PathVariable Serializable id) {
        V vo = service.findById(id);
        return ResultGenerator.genSuccessResult(vo);
    }

    @DeleteMapping("/batch")
    @ApiOperation(value = "批量删除实体", notes = "")
    public Result batchDelete(@RequestParam String ids) {
        service.deleteByIds(ids);
        return ResultGenerator.genSuccessResult();
    }

    @GetMapping("/batch")
    @ApiOperation(value = "批量获取实体", notes = "")
    public Result batchDetail(@RequestParam String ids) {
        List<V> vos = service.findByIds(ids);
        return ResultGenerator.genSuccessResult(vos);
    }

    @PostMapping("/batch")
    @ApiOperation(value = "批量新建实体", notes = "")
    public Result add(@RequestBody List<V> vos) {
        service.save(vos);
        return ResultGenerator.genSuccessResult();
    }

}
