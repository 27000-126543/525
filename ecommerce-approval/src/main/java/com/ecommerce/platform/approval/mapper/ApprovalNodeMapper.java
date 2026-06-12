package com.ecommerce.platform.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.platform.approval.entity.ApprovalNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalNodeMapper extends BaseMapper<ApprovalNode> {

    List<ApprovalNode> selectByProcessId(@Param("processId") Long processId);

    int deleteByProcessId(@Param("processId") Long processId);
}
