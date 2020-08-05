package com.java.service;

import com.java.bean.*;

import java.util.List;

public interface ManageSerivce {

    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);

    List<BaseAttrInfo> getAttrList(String catalog3Id);

    List<BaseAttrInfo> attrInfoList(String catalog3Id);

    BaseAttrInfo getAttrValueList(String attrId);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
}
