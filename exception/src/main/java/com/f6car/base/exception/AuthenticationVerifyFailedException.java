/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.f6car.base.exception;


/**
 * Created by qixiaobo on 2018/1/18.
 */
public class AuthenticationVerifyFailedException extends TokenException {


    private static final long serialVersionUID = 1767632765331736789L;

    public AuthenticationVerifyFailedException(String message) {
        super(message);
    }
}
