package com.java.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.java.bean.*;
import com.java.gmall.manage.mapper.*;
import com.java.service.ManageSerivce;
import org.apache.tomcat.util.modeler.BaseAttributeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManageSeriveImpl implements ManageSerivce {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalogMapper3;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1s = baseCatalog1Mapper.selectAll();
        return baseCatalog1s;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalogMapper3.select(baseCatalog3);
    }

    @Override
    public BaseAttrInfo getAttrValueList(String attrId) {
        // 创建属性对象
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        // 给属性对象中的属性值集合赋值
        attrInfo.setAttrValueList(attrValueList);
        // 将属性对象返回
        return attrInfo;

    }

    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断是否是修改操作，是更新这个attrInfo，否是新增操作，直接插入一个新的
        if(baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
            //修改操作，无论是不是只改一个名字，都要先清空其旗下的所有属性值
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.delete(baseAttrValue);

            //无论是否是新增还是修改，都需要遍历baseAttrInfo的bean中的BaseAttrValue集合，然后添加数据库baseAttrValue表中
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            if(baseAttrInfo.getAttrValueList()!=null && baseAttrInfo.getAttrValueList().size()>0) {
                for (BaseAttrValue value : attrValueList) {
                    value.setId(null);
                    value.setAttrId(baseAttrInfo.getId());
                    baseAttrValueMapper.insertSelective(value);
                }
            }
        }else{
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
            String insertAttrInfoId = baseAttrInfo.getId();
            //遍历baseAttrInfo的bean中的BaseAttrValue集合，然后添加数据库baseAttrValue表中
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            if(baseAttrInfo.getAttrValueList()!=null && baseAttrInfo.getAttrValueList().size()>0) {
                for (BaseAttrValue value : attrValueList) {
                    value.setId(null);
                    value.setAttrId(insertAttrInfoId);
                    baseAttrValueMapper.insertSelective(value);
                }
            }
        }
    }
}
