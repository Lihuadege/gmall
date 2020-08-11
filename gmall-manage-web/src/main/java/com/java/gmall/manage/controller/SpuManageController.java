package com.java.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.gmall.bean.*;
import com.java.service.ListService;
import com.java.service.ManageSerivce;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    ManageSerivce manageSerivce;

    @Reference
    ListService listService;

    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return manageSerivce.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId) {
        return manageSerivce.getSpuImageList(spuId);
    }

    @RequestMapping("spuList")
    public List<SpuInfo> spuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageSerivce.getSpuInfoList(spuInfo);
        return spuInfoList;
    }

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        manageSerivce.saveSpuInfo(spuInfo);
        return "OK";
    }

    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        if (skuInfo != null) {
            manageSerivce.saveSkuInfo(skuInfo);
            return "ok";
        }
        return "not ok";
    }

    @RequestMapping(value = "onSale",method = RequestMethod.GET)
    public void onSale(String skuId){
        SkuInfo skuInfo = manageSerivce.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // 属性拷贝
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }

}
