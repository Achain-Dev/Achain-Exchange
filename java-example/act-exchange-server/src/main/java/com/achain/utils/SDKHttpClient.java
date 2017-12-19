package com.achain.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.Base64;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;


/**
 * 工具类http-post方法
 *
 * @author qiangkezhen
 */
@Slf4j
@Service
public class SDKHttpClient {

    @Autowired
    private CloseableHttpClient httpclient;

    /**
     * 专门处理一个参数，是json的广播交易
     */
    public String post(String url, String key, String method, String... params) {
        String temp = "{\"jsonrpc\":\"2.0\",\"params\":" + Arrays.toString(params) +
                      ",\"id\":\"" + new Random().nextInt(1024) + "\",\"method\":\"" +
                      method + "\"}";
        return basePost(url, temp, key);
    }

    public String post(String url, String key, String method, JSONArray jsonArray) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", method);
        jsonObject.put("id", ((int) ((Math.random() * 9 + 1) * 10)));
        jsonObject.put("jsonrpc", "2.0");
        jsonObject.put("params", jsonArray);
        return basePost(url, jsonObject.toString(), key);
    }

    private String basePost(String url, String entity, String key) {
        HttpPost httppost = null;
        String result = null;
        try {
            String rpcAuth = (int) ((Math.random() * 9 + 1) * 100000) + "" + Base64.encodeBytes(key.getBytes());
            httppost = new HttpPost(url);
            httppost.setEntity(new StringEntity(entity, Charset.forName("UTF-8")));
            httppost.setHeader("Content-type", "application/json");
            httppost.setHeader("Authorization", rpcAuth);
            log.info("【SDKHttpClient】｜POST开始：url=[{}]", url);
            CloseableHttpResponse response = httpclient.execute(httppost);
            if (null != response) {
                try {
                    result = EntityUtils.toString(response.getEntity(), "UTF-8");
                    log.info("【SDKHttpClient】｜POST结束 URL:[{}][jsonArray={}],响应结果[response={}][result={}]!", url, entity,
                             response.getStatusLine().getStatusCode(), result);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        result = null;
                    }
                } finally {
                    response.close();
                }
            } else {
                log.info("【SDKHttpClient】｜POST URL:[{}],响应结果为空!", url);
            }
        } catch (Exception e) {
            log.error("【SDKHttpClient】｜POST URL:[{}] 出现异常[{}]!", url, e.getStackTrace());
        } finally {
            try {
                if (null != httppost) {
                    httppost.releaseConnection();
                }
            } catch (Exception e) {
                log.error("【SDKHttpClient】｜POST URL:[{}] 关闭httpclient.close()异常[{}]!", url, e.getStackTrace());
            }
        }
        return result;
    }
}
