package com.ecommerce.platform.approval.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.util.DistributedLockUtil;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.approval.entity.*;
import com.ecommerce.platform.approval.mapper.*;
import com.ecommerce.platform.approval.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalProcessMapper processMapper;
    private final ApprovalNodeMapper nodeMapper;
    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalRecordMapper recordMapper;
    private final ApprovalCcMapper ccMapper;
    private final RedisUtil redisUtil;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public ApprovalProcess getProcessByCode(String processCode) {
        LambdaQueryWrapper<ApprovalProcess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProcess::getProcessCode, processCode)
                .eq(ApprovalProcess::getStatus, 1)
                .eq(ApprovalProcess::getDeleted, 0)
                .orderByDesc(ApprovalProcess::getVersion)
                .last("LIMIT 1");
        ApprovalProcess process = processMapper.selectOne(wrapper);
        if (process != null) {
            process.setNodes(nodeMapper.selectByProcessId(process.getId()));
        }
        return process;
    }

    @Override
    public ApprovalProcess getProcessById(Long id) {
        ApprovalProcess process = processMapper.selectById(id);
        if (process != null) {
            process.setNodes(nodeMapper.selectByProcessId(process.getId()));
        }
        return process;
    }

    @Override
    public List<ApprovalProcess> listProcesses() {
        return processMapper.selectList(new LambdaQueryWrapper<ApprovalProcess>()
                .eq(ApprovalProcess::getStatus, 1)
                .eq(ApprovalProcess::getDeleted, 0)
                .orderByAsc(ApprovalProcess::getProcessCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProcess(ApprovalProcess process) {
        process.setVersion(1);
        processMapper.insert(process);
        if (process.getNodes() != null) {
            for (ApprovalNode node : process.getNodes()) {
                node.setProcessId(process.getId());
                nodeMapper.insert(node);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProcess(ApprovalProcess process) {
        ApprovalProcess old = processMapper.selectById(process.getId());
        if (old == null) {
            throw new BusinessException("审批流程不存在");
        }
        process.setVersion(old.getVersion() + 1);
        processMapper.updateById(process);

        if (process.getNodes() != null) {
            nodeMapper.deleteByProcessId(process.getId());
            for (ApprovalNode node : process.getNodes()) {
                node.setId(null);
                node.setProcessId(process.getId());
                nodeMapper.insert(node);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcess(Long id) {
        processMapper.deleteById(id);
        nodeMapper.deleteByProcessId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalInstance startInstance(String processCode, Long businessId, String businessType,
                                          String title, String formData, Long applicantId, String applicantName) {
        ApprovalProcess process = getProcessByCode(processCode);
        if (process == null) {
            throw new BusinessException("审批流程不存在");
        }

        List<ApprovalNode> nodes = process.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            throw new BusinessException("审批流程节点配置异常");
        }

        ApprovalNode firstNode = nodes.stream()
                .min(Comparator.comparing(ApprovalNode::getNodeOrder))
                .orElse(null);

        ApprovalInstance instance = new ApprovalInstance();
        instance.setInstanceNo(generateInstanceNo());
        instance.setProcessId(process.getId());
        instance.setProcessName(process.getProcessName());
        instance.setProcessType(process.getProcessType());
        instance.setBusinessId(businessId);
        instance.setBusinessType(businessType);
        instance.setTitle(title);
        instance.setFormData(formData);
        instance.setApplicantId(applicantId);
        instance.setApplicantName(applicantName);
        instance.setStatus(0);
        instance.setStartTime(LocalDateTime.now());

        if (firstNode != null) {
            instance.setCurrentNodeId(firstNode.getId());
            instance.setCurrentNodeName(firstNode.getNodeName());
        }

        instanceMapper.insert(instance);

        if (firstNode != null) {
            createApprovalRecord(instance.getId(), firstNode, applicantId, applicantName);
        }

        log.info("审批实例创建成功, instanceId: {}, processCode: {}", instance.getId(), processCode);
        return instance;
    }

    @Override
    public ApprovalInstance getInstance(Long instanceId) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance != null) {
            instance.setRecords(recordMapper.selectByInstanceId(instanceId));
        }
        return instance;
    }

    @Override
    public PageResult<ApprovalInstance> pageInstances(PageQuery pageQuery, ApprovalInstance instance) {
        Page<ApprovalInstance> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<ApprovalInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalInstance::getDeleted, 0);

        if (instance.getProcessType() != null) {
            wrapper.eq(ApprovalInstance::getProcessType, instance.getProcessType());
        }
        if (instance.getStatus() != null) {
            wrapper.eq(ApprovalInstance::getStatus, instance.getStatus());
        }
        if (instance.getApplicantId() != null) {
            wrapper.eq(ApprovalInstance::getApplicantId, instance.getApplicantId());
        }
        if (instance.getBusinessType() != null) {
            wrapper.eq(ApprovalInstance::getBusinessType, instance.getBusinessType());
        }

        wrapper.orderByDesc(ApprovalInstance::getCreateTime);
        Page<ApprovalInstance> result = instanceMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public List<ApprovalInstance> listMyPending(Long userId) {
        return instanceMapper.selectPendingByUserId(userId);
    }

    @Override
    public List<ApprovalInstance> listMyApproved(Long userId) {
        return instanceMapper.selectApprovedByUserId(userId);
    }

    @Override
    public List<ApprovalInstance> listMyApplied(Long userId) {
        return instanceMapper.selectList(new LambdaQueryWrapper<ApprovalInstance>()
                .eq(ApprovalInstance::getApplicantId, userId)
                .eq(ApprovalInstance::getDeleted, 0)
                .orderByDesc(ApprovalInstance::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long instanceId, Long approverId, String approverName,
                        Integer result, String opinion) {
        String lockKey = "approval:instance:" + instanceId;
        distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
            ApprovalInstance instance = instanceMapper.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("审批实例不存在");
            }
            if (instance.getStatus() != 0) {
                throw new BusinessException("审批实例状态异常");
            }

            ApprovalNode currentNode = nodeMapper.selectById(instance.getCurrentNodeId());
            if (currentNode == null) {
                throw new BusinessException("审批节点不存在");
            }

            ApprovalRecord record = recordMapper.selectPendingRecord(instanceId, currentNode.getId(), approverId);
            if (record == null) {
                throw new BusinessException("您没有该审批的处理权限");
            }

            record.setApprovalResult(result);
            record.setApprovalOpinion(opinion);
            record.setApprovalTime(LocalDateTime.now());
            record.setStatus(result == 1 ? 2 : 3);
            recordMapper.updateById(record);

            if (result == 2) {
                rejectInstance(instance, currentNode, approverId, approverName, opinion);
                return null;
            }

            List<ApprovalRecord> nodeRecords = recordMapper.selectByNodeId(instanceId, currentNode.getId());
            boolean nodePassed = checkNodePassed(currentNode, nodeRecords);

            if (nodePassed) {
                ApprovalNode nextNode = findNextNode(currentNode, instance.getProcessId());
                if (nextNode != null) {
                    moveToNextNode(instance, nextNode, approverId, approverName);
                } else {
                    completeInstance(instance, approverId, approverName);
                }
            }

            return null;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(List<Long> instanceIds, Long approverId, String approverName,
                             Integer result, String opinion) {
        for (Long instanceId : instanceIds) {
            try {
                approve(instanceId, approverId, approverName, result, opinion);
            } catch (Exception e) {
                log.error("批量审批失败, instanceId: {}", instanceId, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegate(Long instanceId, Long fromUserId, Long toUserId, String reason) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null || instance.getStatus() != 0) {
            throw new BusinessException("审批实例状态异常");
        }

        ApprovalRecord record = recordMapper.selectPendingRecord(instanceId, instance.getCurrentNodeId(), fromUserId);
        if (record == null) {
            throw new BusinessException("您没有该审批的处理权限");
        }

        record.setApproverId(toUserId);
        record.setRemark("由" + fromUserId + "转交: " + reason);
        recordMapper.updateById(record);

        log.info("审批转交成功, instanceId: {}, from: {}, to: {}", instanceId, fromUserId, toUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long instanceId, Long fromUserId, Long toUserId, String reason) {
        delegate(instanceId, fromUserId, toUserId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInstance(Long instanceId, Long operatorId, String reason) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }

        instance.setStatus(4);
        instance.setCancelReason(reason);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        log.info("审批取消成功, instanceId: {}, operator: {}", instanceId, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restartInstance(Long instanceId, Long operatorId) {
        ApprovalInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException("审批实例不存在");
        }

        ApprovalProcess process = getProcessById(instance.getProcessId());
        if (process == null || process.getNodes() == null || process.getNodes().isEmpty()) {
            throw new BusinessException("审批流程不存在");
        }

        recordMapper.deleteByInstanceId(instanceId);

        ApprovalNode firstNode = process.getNodes().stream()
                .min(Comparator.comparing(ApprovalNode::getNodeOrder))
                .orElse(null);

        instance.setStatus(0);
        instance.setCurrentNodeId(firstNode != null ? firstNode.getId() : null);
        instance.setCurrentNodeName(firstNode != null ? firstNode.getNodeName() : null);
        instance.setStartTime(LocalDateTime.now());
        instance.setEndTime(null);
        instanceMapper.updateById(instance);

        if (firstNode != null) {
            createApprovalRecord(instance.getId(), firstNode, instance.getApplicantId(), instance.getApplicantName());
        }

        log.info("审批重启成功, instanceId: {}, operator: {}", instanceId, operatorId);
    }

    @Override
    public Map<String, Object> getApprovalStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", listMyPending(userId).size());
        stats.put("approvedCount", listMyApproved(userId).size());
        stats.put("appliedCount", listMyApplied(userId).size());
        return stats;
    }

    private void createApprovalRecord(Long instanceId, ApprovalNode node, Long applicantId, String applicantName) {
        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(instanceId);
        record.setNodeId(node.getId());
        record.setNodeName(node.getNodeName());
        record.setNodeOrder(node.getNodeOrder());
        record.setApprovalType(node.getApprovalType());
        record.setStatus(1);
        recordMapper.insert(record);
    }

    private boolean checkNodePassed(ApprovalNode node, List<ApprovalRecord> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }

        long approvedCount = records.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 2)
                .count();
        long rejectedCount = records.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 3)
                .count();

        if (rejectedCount > 0 && node.getPassCondition() != 2) {
            return false;
        }

        if (node.getPassCondition() == null || node.getPassCondition() == 0) {
            return approvedCount > 0;
        } else if (node.getPassCondition() == 1) {
            int passCount = node.getPassCount() != null ? node.getPassCount() : 1;
            return approvedCount >= passCount;
        } else if (node.getPassCondition() == 2) {
            double passPercent = node.getPassPercent() != null ? node.getPassPercent() : 50;
            return (double) approvedCount / records.size() * 100 >= passPercent;
        }

        return approvedCount > 0;
    }

    private ApprovalNode findNextNode(ApprovalNode currentNode, Long processId) {
        List<ApprovalNode> nodes = nodeMapper.selectByProcessId(processId);
        return nodes.stream()
                .filter(n -> n.getNodeOrder() > currentNode.getNodeOrder())
                .min(Comparator.comparing(ApprovalNode::getNodeOrder))
                .orElse(null);
    }

    private void moveToNextNode(ApprovalInstance instance, ApprovalNode nextNode,
                                Long approverId, String approverName) {
        instance.setCurrentNodeId(nextNode.getId());
        instance.setCurrentNodeName(nextNode.getNodeName());
        instanceMapper.updateById(instance);

        createApprovalRecord(instance.getId(), nextNode, approverId, approverName);

        log.info("审批流转到下一节点, instanceId: {}, nextNode: {}", instance.getId(), nextNode.getNodeName());
    }

    private void completeInstance(ApprovalInstance instance, Long approverId, String approverName) {
        instance.setStatus(1);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        log.info("审批完成, instanceId: {}, approver: {}", instance.getId(), approverName);
    }

    private void rejectInstance(ApprovalInstance instance, ApprovalNode currentNode,
                                Long approverId, String approverName, String opinion) {
        instance.setStatus(2);
        instance.setEndTime(LocalDateTime.now());
        instanceMapper.updateById(instance);

        log.info("审批驳回, instanceId: {}, approver: {}, reason: {}", instance.getId(), approverName, opinion);
    }

    private String generateInstanceNo() {
        return "AP" + System.currentTimeMillis() + IdUtil.randomNumbers(4);
    }
}
