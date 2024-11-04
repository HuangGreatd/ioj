package com.juzipi.ioj.judge.condesandbox;

import com.juzipi.ioj.judge.condesandbox.impl.ExampleCodeSandbox;
import com.juzipi.ioj.judge.condesandbox.impl.RemoteCodeSandbox;
import com.juzipi.ioj.judge.condesandbox.impl.ThridPartyCodeSandbox;
import org.springframework.beans.factory.annotation.Value;

/**
 * @ClassName: CodeSandboxFactory
 * @Description: 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例）
 * @Author: 橘子皮
 * @Date: 2024-11-04 15:57
 * @Version: 1.0
 **/
public class CodeSandboxFactory {

    @Value("${codeSandbox.value.type}}")
    private String type;
    /**
     * 创建代码沙箱示例 静态工厂模式
     * @param type
     * @return
     */
    public static CodeSandbox newInstance(String type){
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThridPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
