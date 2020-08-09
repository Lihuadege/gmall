package com.java.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.gmall.bean.SkuInfo;
import com.java.gmall.bean.SpuImage;
import com.java.gmall.bean.SpuInfo;
import com.java.gmall.bean.SpuSaleAttr;
import com.java.service.ManageSerivce;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageSerivce manageSerivce;

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

}
