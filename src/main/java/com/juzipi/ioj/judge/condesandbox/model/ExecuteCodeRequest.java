package com.juzipi.ioj.judge.condesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName: ExecuteCodeRequest
 * @Description: 代码沙箱输入
 * @Author: 橘子皮
 * @Date: 2024-11-04 15:42
 * @Version: 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    private List<String> inputList;

    private String code;

    private String language;
}
