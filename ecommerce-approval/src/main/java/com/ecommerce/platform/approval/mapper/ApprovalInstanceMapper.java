package com.ecommerce.platform.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ecommerce.platform.approval.entity.ApprovalInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalInstanceMapper extends BaseMapper<ApprovalInstance> {

    List<ApprovalInstance> selectPendingByUserId(@Param("userId") Long userId);

    List<ApprovalInstance> selectApprovedByUserId(@Param("userId") Long userId);
}
