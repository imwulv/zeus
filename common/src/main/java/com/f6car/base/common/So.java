/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.common;


import java.util.ArrayList;
import java.util.List;

/**
 * @author qixiaobo
 */
//非线程安全

public class So implements java.io.Serializable, Pagable, Sortable {

    private static final long serialVersionUID = -828171499994153332L;

    private int currentPage;
    private int pageSize;
    private boolean enableCount;
    private List<Sort> sorts;


    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public boolean isEnableCount() {
        return enableCount;
    }

    @Override
    public void setEnableCount(boolean enableCount) {
        this.enableCount = enableCount;
    }

    @Override
    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(List<Sort> sorts) {
        this.sorts = sorts;
    }

    @Override
    public void addSort(Sort sort) {
        if (sorts != null) {
            sorts = new ArrayList<>();
        }
        sorts.add(sort);
    }
}
