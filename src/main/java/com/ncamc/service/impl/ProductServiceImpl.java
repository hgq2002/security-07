package com.ncamc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ncamc.entity.Product;
import com.ncamc.entity.User;
import com.ncamc.internal.Constant;
import com.ncamc.mapper.ProductMapper;
import com.ncamc.service.ProductService;
import com.ncamc.utils.RedisCache;
import com.wisdge.cloud.dto.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @Author: hugaoqiang
 * @CreateTime: 2022-07-08 09:55
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    public static final String CACHE_KEY_USER = "user:";

    @Autowired
    private RedisCache redisCache;

    /**
     * 多表分页模糊条件查询
     */
    @Override
    public ApiResult getProductList(Page<Map<String, Object>> page, Map<String, Object> map) {
        Page<Map<String, Object>> res = null;

        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();

        String username = MapUtils.getString(map, "username");
        String id = MapUtils.getString(map, "id");

        if (Strings.isNotBlank(username) && !(Strings.isNotBlank(id))) {
            productQueryWrapper.lambda().like(Product::getPrdName, username);
            res = this.baseMapper.page(page, productQueryWrapper);
        }
        if (Strings.isNotBlank(id) && !(Strings.isNotBlank(username))) {
            userQueryWrapper.lambda().eq(User::getId, id);
            res = this.baseMapper.pages(page, userQueryWrapper);
        }
        if (Strings.isNotBlank(id) && Strings.isNotBlank(username)) {
            productQueryWrapper.lambda().like(Product::getPrdName, username);
            res = this.baseMapper.pageByIdAndLikeName(page, productQueryWrapper,Integer.parseInt(id));
        }
        return ApiResult.ok(HttpStatus.OK.value(),Constant.STR_EMPTY,res);
    }

    /**
     * 查询分页信息
     */
    @Override
    public ApiResult listPage(Page<Product> page, Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params.get("prdIns").toString())) {
            return ApiResult.ok(Constant.STR_EMPTY,productMapper.selectPage(page, null));
        } else {
            String prdIns = MapUtils.getString(params, "prdIns");
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.isNotBlank(prdIns), Product::getPrdName, prdIns).or().like(StringUtils.isNotBlank(prdIns), Product::getInsName, prdIns);
            return ApiResult.ok(Constant.STR_EMPTY,productMapper.selectPage(page, wrapper));
        }
    }

    /**
     * 根据ID查询该产品信息
     */
    @Override
    public ApiResult selectByPrimaryKey(Integer id) {
        return ApiResult.ok(Constant.STR_EMPTY,productMapper.selectById(id));
    }

    /**
     * 根据ID查询该产品信息
     */
    @Override
    public ApiResult findById(Long id) {
        Product product = null;
        String key = CACHE_KEY_USER + id;
        product = redisCache.getCacheObject(key);
        if (Objects.isNull(product)) {
            product = productMapper.selectById(id);
            if (Objects.isNull(product)) {
                return null;
            } else {
                redisCache.setCacheObject(key, product);
            }
        }
        return ApiResult.ok(Constant.STR_EMPTY,product);
    }

    /**
     * 根据ID删除该产品
     */
    @Override
    public ApiResult deleteByPrimaryKey(Long id) {
        if (id != null) {
            return ApiResult.ok(Constant.STR_EMPTY,productMapper.deleteById(id));
        }
        return ApiResult.ok(Constant.STR_EMPTY);
    }

}