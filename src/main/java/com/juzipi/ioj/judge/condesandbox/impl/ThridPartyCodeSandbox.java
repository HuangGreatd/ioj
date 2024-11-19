package com.juzipi.ioj.judge.condesandbox.impl;

import com.juzipi.ioj.judge.condesandbox.CodeSandbox;
import com.juzipi.ioj.judge.condesandbox.model.ExecuteCodeRequest;
import com.juzipi.ioj.judge.condesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName: ExampleCodeSandbox
 * @Description: 第三方代码沙箱
 * @Author: 橘子皮
 * @Date: 2024-11-04 15:46
 * @Version: 1.0
 **/
@Slf4j
public class ThridPartyCodeSandbox implements CodeSandbox {

    @Value("${auth.request.header}")
    private String AUTH_REQUEST_HEADER;
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("AUTH_REQUEST_HEADER = " + AUTH_REQUEST_HEADER);

        log.info("执行代码请求参数：{}", executeCodeRequest);
        System.out.println("调用第三方代码沙箱");
        return null;
    }
}
