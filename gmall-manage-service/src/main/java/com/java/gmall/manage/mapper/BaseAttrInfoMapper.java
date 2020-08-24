package com.java.gmall.manage.mapper;

import com.java.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Integer catalog3Id);

    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String attrValueIds);
}
