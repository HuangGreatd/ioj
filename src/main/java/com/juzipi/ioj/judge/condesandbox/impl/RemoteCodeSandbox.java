package com.juzipi.ioj.judge.condesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.juzipi.ioj.common.ErrorCode;
import com.juzipi.ioj.exception.BusinessException;
import com.juzipi.ioj.judge.condesandbox.CodeSandbox;
import com.juzipi.ioj.judge.condesandbox.model.ExecuteCodeRequest;
import com.juzipi.ioj.judge.condesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName: ExampleCodeSandbox
 * @Description: 远程调用代码沙箱
 * @Author: 橘子皮
 * @Date: 2024-11-04 15:46
 * @Version: 1.0
 **/
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox {
    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://192.168.112.128:8089/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
