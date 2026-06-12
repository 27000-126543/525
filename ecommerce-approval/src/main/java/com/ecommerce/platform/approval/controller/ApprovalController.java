package com.ecommerce.platform.approval.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.approval.entity.ApprovalInstance;
import com.ecommerce.platform.approval.entity.ApprovalProcess;
import com.ecommerce.platform.approval.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审批管理")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "获取审批流程列表")
    @GetMapping("/process/list")
    public Result<List<ApprovalProcess>> listProcesses() {
        return Result.success(approvalService.listProcesses());
    }

    @Operation(summary = "获取审批流程详情")
    @GetMapping("/process/{id}")
    public Result<ApprovalProcess> getProcess(@PathVariable Long id) {
        return Result.success(approvalService.getProcessById(id));
    }

    @Operation(summary = "新增审批流程")
    @PostMapping("/process")
    public Result<Void> addProcess(@RequestBody ApprovalProcess process) {
        approvalService.saveProcess(process);
        return Result.success();
    }

    @Operation(summary = "修改审批流程")
    @PutMapping("/process")
    public Result<Void> updateProcess(@RequestBody ApprovalProcess process) {
        approvalService.updateProcess(process);
        return Result.success();
    }

    @Operation(summary = "删除审批流程")
    @DeleteMapping("/process/{id}")
    public Result<Void> deleteProcess(@PathVariable Long id) {
        approvalService.deleteProcess(id);
        return Result.success();
    }

    @Operation(summary = "发起审批")
    @PostMapping("/start")
    public Result<ApprovalInstance> startApproval(
            @RequestParam String processCode,
            @RequestParam Long businessId,
            @RequestParam String businessType,
            @RequestParam String title,
            @RequestParam(required = false) String formData) {
        Long userId = UserContext.getUserId();
        String userName = UserContext.getUsername();
        return Result.success(approvalService.startInstance(
                processCode, businessId, businessType, title, formData, userId, userName));
    }

    @Operation(summary = "获取审批实例详情")
    @GetMapping("/instance/{id}")
    public Result<ApprovalInstance> getInstance(@PathVariable Long id) {
        return Result.success(approvalService.getInstance(id));
    }

    @Operation(summary = "分页查询审批实例")
    @GetMapping("/instance/page")
    public Result<PageResult<ApprovalInstance>> pageInstances(PageQuery pageQuery, ApprovalInstance instance) {
        return Result.success(approvalService.pageInstances(pageQuery, instance));
    }

    @Operation(summary = "我的待审批")
    @GetMapping("/my/pending")
    public Result<List<ApprovalInstance>> myPending() {
        Long userId = UserContext.getUserId();
        return Result.success(approvalService.listMyPending(userId));
    }

    @Operation(summary = "我的已审批")
    @GetMapping("/my/approved")
    public Result<List<ApprovalInstance>> myApproved() {
        Long userId = UserContext.getUserId();
        return Result.success(approvalService.listMyApproved(userId));
    }

    @Operation(summary = "我的申请")
    @GetMapping("/my/applied")
    public Result<List<ApprovalInstance>> myApplied() {
        Long userId = UserContext.getUserId();
        return Result.success(approvalService.listMyApplied(userId));
    }

    @Operation(summary = "审批操作")
    @PostMapping("/approve")
    public Result<Void> approve(
            @RequestParam Long instanceId,
            @RequestParam Integer result,
            @RequestParam(required = false) String opinion) {
        Long userId = UserContext.getUserId();
        String userName = UserContext.getUsername();
        approvalService.approve(instanceId, userId, userName, result, opinion);
        return Result.success();
    }

    @Operation(summary = "批量审批")
    @PostMapping("/batchApprove")
    public Result<Void> batchApprove(
            @RequestParam List<Long> instanceIds,
            @RequestParam Integer result,
            @RequestParam(required = false) String opinion) {
        Long userId = UserContext.getUserId();
        String userName = UserContext.getUsername();
        approvalService.batchApprove(instanceIds, userId, userName, result, opinion);
        return Result.success();
    }

    @Operation(summary = "转交通知")
    @PostMapping("/delegate")
    public Result<Void> delegate(
            @RequestParam Long instanceId,
            @RequestParam Long toUserId,
            @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId();
        approvalService.delegate(instanceId, userId, toUserId, reason);
        return Result.success();
    }

    @Operation(summary = "撤销审批")
    @PostMapping("/cancel")
    public Result<Void> cancel(
            @RequestParam Long instanceId,
            @RequestParam(required = false) String reason) {
        Long userId = UserContext.getUserId();
        approvalService.cancelInstance(instanceId, userId, reason);
        return Result.success();
    }

    @Operation(summary = "重新发起")
    @PostMapping("/restart")
    public Result<Void> restart(@RequestParam Long instanceId) {
        Long userId = UserContext.getUserId();
        approvalService.restartInstance(instanceId, userId);
        return Result.success();
    }

    @Operation(summary = "获取审批统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Long userId = UserContext.getUserId();
        return Result.success(approvalService.getApprovalStats(userId));
    }
}
