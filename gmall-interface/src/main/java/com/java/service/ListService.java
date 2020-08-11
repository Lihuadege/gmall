package com.java.service;

import com.java.gmall.bean.SkuLsInfo;
import com.java.gmall.bean.SkuLsParams;
import com.java.gmall.bean.SkuLsResult;

public interface ListService {

    void saveSkuInfo(SkuLsInfo skuLsInfo);

    SkuLsResult search(SkuLsParams skuLsParams);

}
