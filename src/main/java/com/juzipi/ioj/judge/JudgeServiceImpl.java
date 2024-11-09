package com.juzipi.ioj.judge;

import cn.hutool.json.JSONUtil;
import com.juzipi.ioj.common.ErrorCode;
import com.juzipi.ioj.exception.BusinessException;
import com.juzipi.ioj.judge.condesandbox.CodeSandbox;
import com.juzipi.ioj.judge.condesandbox.CodeSandboxFactory;
import com.juzipi.ioj.judge.condesandbox.CodeSandboxProxy;
import com.juzipi.ioj.judge.condesandbox.model.ExecuteCodeRequest;
import com.juzipi.ioj.judge.condesandbox.model.ExecuteCodeResponse;
import com.juzipi.ioj.judge.strategy.JudgeContext;
import com.juzipi.ioj.judge.strategy.JudgeManager;
import com.juzipi.ioj.model.dto.question.JudgeCase;
import com.juzipi.ioj.judge.condesandbox.model.JudgeInfo;
import com.juzipi.ioj.model.entity.Question;
import com.juzipi.ioj.model.entity.QuestionSubmit;
import com.juzipi.ioj.model.enums.JudgeInfoMessageEnum;
import com.juzipi.ioj.model.enums.QuestionSubmitStatusEnum;
import com.juzipi.ioj.service.QuestionService;
import com.juzipi.ioj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: JudgeServiceImpl
 * @Description:
 * @Author: 橘子皮
 * @Date: 2024-11-04 20:43
 * @Version: 1.0
 **/
@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Value("${codesandbox.type:remote}")
    private String type;

    @Resource
    private JudgeManager judgeManager;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        //4) 调用代码沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.WAITING;

        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        //todo 策略模式使用 问题：怎么切换别的策略
//        DefaultJudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
//        JudgeInfo judgeInfo = judgeStrategy.doJudge(judgeContext);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionId);
        return questionSubmitResult;
    }
}
