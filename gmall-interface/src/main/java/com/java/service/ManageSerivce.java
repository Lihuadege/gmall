package com.java.service;

import com.java.gmall.bean.*;

import java.util.List;

public interface ManageSerivce {

    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);

    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    BaseAttrInfo getAttrValueList(String attrId);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseSaleAttr> getBaseSaleAttrList();

    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
