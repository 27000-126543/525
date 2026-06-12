package com.ecommerce.platform.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.approval.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    List<ApprovalRecord> selectByInstanceId(@Param("instanceId") Long instanceId);

    List<ApprovalRecord> selectByNodeId(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    ApprovalRecord selectPendingRecord(@Param("instanceId") Long instanceId,
                                        @Param("nodeId") Long nodeId,
                                        @Param("approverId") Long approverId);

    int deleteByInstanceId(@Param("instanceId") Long instanceId);
}
