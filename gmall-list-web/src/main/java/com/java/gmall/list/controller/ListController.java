package com.java.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.java.gmall.bean.BaseAttrInfo;
import com.java.gmall.bean.BaseAttrValue;
import com.java.gmall.bean.SkuLsParams;
import com.java.gmall.bean.SkuLsResult;
import com.java.service.ListService;
import com.java.service.ManageSerivce;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
@CrossOrigin
public class ListController {

    @Reference
    ListService listService;

    @Reference
    ManageSerivce manageSerivce;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, Model model){

        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult  = listService.search(skuLsParams);

        model.addAttribute("totalPages", skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());


        List<String> attrValueIdList = skuLsResult .getAttrValueIdList();
        List<BaseAttrInfo> attrInfoList = manageSerivce.getAttrList(attrValueIdList);

        String urlParam = mkUrlParam(skuLsParams);

        List<BaseAttrValue> baseAttrValues = new ArrayList<>();

        for (Iterator<BaseAttrInfo> iterator = attrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo =  iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值
                        if(valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            // 去除重复数据
                            String makeUrlParam = mkUrlParam(skuLsParams, valueId);
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValues.add(baseAttrValueSelected);
                        }
                    }
                }
            }
        }

        model.addAttribute("baseAttrValuesList",baseAttrValues);
        model.addAttribute("keyword",skuLsParams.getKeyword());

        model.addAttribute("skuLsInfoList",skuLsResult .getSkuLsInfoList());

        model.addAttribute("urlParam",urlParam);
        model.addAttribute("attrInfoList",attrInfoList);

        return "list";
    }

    private String mkUrlParam(SkuLsParams skuLsParams, String... excludeValueIds) {
        String url = "";

        //判断关键字是否存在
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            url += "keyword=" +skuLsParams.getKeyword();
        }

        //拼接catalog3Id，首先需要判断url中是否存在有关键字，有的话才拼接"&"
        if (skuLsParams.getCatalog3Id()!=null){
            if (url.length()>0){
                url +="&";
            }
            url +="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        //拼接平台属性值id
        String[] valueIds = skuLsParams.getValueId();
        if (valueIds != null && valueIds.length > 0) {
            for (int i = 0; i < valueIds.length; i++) {
                if (excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueIds[i])){
                        // 跳出代码，后面的参数则不会继续追加【后续代码不会执行】
                        // 不能写break；如果写了break；其他条件则无法拼接！
                        continue;
                    }
                }

                if(url.length() > 0){
                    url += "&";
                }
                String id = valueIds[i];
                url += "valueId=" +id;
            }
        }
        return url;
    }
}





















