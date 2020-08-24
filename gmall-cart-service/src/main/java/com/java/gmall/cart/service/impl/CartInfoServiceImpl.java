package com.java.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.java.gmall.bean.CartInfo;
import com.java.gmall.bean.SkuInfo;
import com.java.gmall.cart.Const;
import com.java.gmall.cart.mapper.CartInfoMapper;
import com.java.gmall.config.RedisUtil;
import com.java.service.CartInfoService;
import com.java.service.ManageSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CrossOrigin
public class CartInfoServiceImpl implements CartInfoService {
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    ManageSerivce manageSerivce;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获得redis中的key
        String userCheckedKey = Const.USER_KEY_PREFIX + userId + Const.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;

    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        // 更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        // 取得购物车中的信息
        String userCartKey = Const.USER_KEY_PREFIX+userId+Const.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        // 将cartJson 转换成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckdJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);
        // 新增到已选中购物车
        String userCheckedKey = Const.USER_KEY_PREFIX + userId + Const.USER_CHECKED_KEY_SUFFIX;
        if (isChecked.equals("1")){
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else{
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }

    /**
     * 合并cookie中的和数据库中的购物车数据,存入数据库和Redis中
     * @param cookieCartInfoList
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cookieCartInfoList, String userId) {
        //首先从数据库中查询出所有的用户id下的购物车数据
        List<CartInfo> cartInfoDBList = cartInfoMapper.selectCartListWithCurPrice(userId);

        //对比
        for(CartInfo cookieCartInfo :  cookieCartInfoList){
            String cookieCartInfoSkuId = cookieCartInfo.getSkuId();
            boolean flag = false;

            for(CartInfo cartInfoDb : cartInfoDBList){
                if(cartInfoDb.getSkuId().equals(cookieCartInfoSkuId)){

                    cartInfoDb.setSkuNum(cookieCartInfo.getSkuNum()+cartInfoDb.getSkuNum());
                    //更新数据库对应的数据
                    cartInfoMapper.updateByPrimaryKey(cartInfoDb);

                    flag = true;
                    break;
                }
            }
            if(!flag){
                cookieCartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cookieCartInfo);
            }
        }
        List<CartInfo> cartListByDB = getCartListByDB(userId);

        for (CartInfo cartInfo : cartListByDB) {
            for (CartInfo info : cookieCartInfoList) {
                if (cartInfo.getSkuId().equals(info.getSkuId())){
                    // 只有被勾选的才会进行更改
                    if (info.getIsChecked().equals("1")){
                        cartInfo.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                    }
                }
            }
        }
        return cartListByDB;

    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String key = Const.USER_KEY_PREFIX+userId+Const.USER_CART_KEY_SUFFIX;
        List<String> cartInfoJson = jedis.hvals(key);
        if(cartInfoJson != null && cartInfoJson.size() > 0){
            //缓存中有，直接放入集合返回
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartInfoJson) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //排序功能，不做
            return cartInfoList;
        }else{
            //缓存中没有，查数据库
            return getCartListByDB(userId);
        }
    }

    private List<CartInfo> getCartListByDB(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        String userCartKey = Const.USER_KEY_PREFIX+userId+Const.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        try {
            //从数据库中查询最新的购物车数据（主要是价格），然后重新写入到redis中（更新缓存）
            Map<String,String> map = new HashMap<>(cartInfoList.size());
            for (CartInfo cartInfo : cartInfoList) {
                String cartJson = JSON.toJSONString(cartInfo);
                // key 都是同一个，值会产生重复覆盖！
                map.put(cartInfo.getSkuId(),cartJson);
            }
            // 将java list - redis hash
            jedis.hmset(userCartKey,map);
        } finally {
            jedis.close();
        }
        return  cartInfoList;
    }

    /**
     * 登录状态下加入购物车
     * @param skuId 商品id
     * @param userId 用户id
     * @param skuNum 商品数量
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoResult = cartInfoMapper.selectOne(cartInfo);
        if(cartInfoResult != null){
            //从数据库中查询出来的结果不为空，判断有无相同的sku，有的话加数量，无的话新建即可
            Integer cartSkuNum = cartInfoResult.getSkuNum();
            cartInfoResult.setSkuNum(++cartSkuNum);
            cartInfoMapper.updateByPrimaryKey(cartInfoResult);
        }else {
            //从数据库中查询数据是空的，就得新建一个cartInfo对象，并存到数据库中
            //需要先查询出skuId对应的商品的信息，然后再保存
            SkuInfo skuInfo = manageSerivce.getSkuInfo(skuId);
            if(skuInfo != null){
                CartInfo cartInfoToDB = new CartInfo();
                cartInfoToDB.setSkuId(skuId);
                cartInfoToDB.setUserId(userId);
                cartInfoToDB.setSkuNum(skuNum);
                cartInfoToDB.setCartPrice(skuInfo.getPrice());
                cartInfoToDB.setImgUrl(skuInfo.getSkuDefaultImg());
                cartInfoToDB.setSkuName(skuInfo.getSkuName());
                cartInfoToDB.setId(null);
                cartInfoMapper.insertSelective(cartInfoToDB);
                String id = cartInfoToDB.getId();
                System.out.println("cartInfoToDB="+id);
                cartInfo = cartInfoToDB;
            }
        }

        //当然，无论数据库是否有该购物车的商品，都要放到redis中去
        Jedis jedis = redisUtil.getJedis();
        try {
            String key = Const.USER_KEY_PREFIX+cartInfo.getUserId()+Const.USER_CART_KEY_SUFFIX;
            String cartJson = JSON.toJSONString(cartInfo);
            jedis.hset(key,skuId,cartJson);
            //查看过期时间
            Long ttl = jedis.ttl(key);
            jedis.expire(key,ttl.intValue());
        } finally {
            jedis.close();
        }
    }
}
