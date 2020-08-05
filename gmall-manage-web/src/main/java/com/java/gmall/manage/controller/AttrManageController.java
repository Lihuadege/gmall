package com.java.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.bean.*;
import com.java.service.ManageSerivce;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class AttrManageController {

    @Reference
    private ManageSerivce manageSerivce;

    @RequestMapping("/getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return manageSerivce.getCatalog1();
    }

    @RequestMapping("/getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageSerivce.getCatalog2(catalog1Id);
    }

    @RequestMapping("/getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageSerivce.getCatalog3(catalog2Id);
    }

    @RequestMapping("/attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageSerivce.getAttrList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageSerivce.saveAttrInfo(baseAttrInfo);
    }

    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo baseAttrInfo = manageSerivce.getAttrValueList(attrId);
        return baseAttrInfo.getAttrValueList();
    }

}
