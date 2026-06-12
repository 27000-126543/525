package com.ecommerce.platform.approval.service;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.approval.entity.ApprovalInstance;
import com.ecommerce.platform.approval.entity.ApprovalProcess;

import java.util.List;
import java.util.Map;

public interface ApprovalService {

    ApprovalProcess getProcessByCode(String processCode);

    ApprovalProcess getProcessById(Long id);

    List<ApprovalProcess> listProcesses();

    void saveProcess(ApprovalProcess process);

    void updateProcess(ApprovalProcess process);

    void deleteProcess(Long id);

    ApprovalInstance startInstance(String processCode, Long businessId, String businessType,
                                   String title, String formData, Long applicantId, String applicantName);

    ApprovalInstance getInstance(Long instanceId);

    PageResult<ApprovalInstance> pageInstances(PageQuery pageQuery, ApprovalInstance instance);

    List<ApprovalInstance> listMyPending(Long userId);

    List<ApprovalInstance> listMyApproved(Long userId);

    List<ApprovalInstance> listMyApplied(Long userId);

    void approve(Long instanceId, Long approverId, String approverName,
                 Integer result, String opinion);

    void batchApprove(List<Long> instanceIds, Long approverId, String approverName,
                      Integer result, String opinion);

    void delegate(Long instanceId, Long fromUserId, Long toUserId, String reason);

    void transfer(Long instanceId, Long fromUserId, Long toUserId, String reason);

    void cancelInstance(Long instanceId, Long operatorId, String reason);

    void restartInstance(Long instanceId, Long operatorId);

    Map<String, Object> getApprovalStats(Long userId);
}
