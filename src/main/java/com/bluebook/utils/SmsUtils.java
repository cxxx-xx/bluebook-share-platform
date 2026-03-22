package com.bluebook.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class SmsUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsUtils.class);
    
    @Value("${sms.appCode}")
    private String appCode;

    @Value("${sms.templateId}")
    private String templateId;

    @Value("${sms.signId}")
    private String signId;

    public boolean sendSms(String phone, String code, int minute) throws Exception {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        
        String paramValue = "**code**:" + code + ",**minute**:" + minute;
        
        String queryParams = "mobile=" + phone +
                            "&param=" + URLEncoder.encode(paramValue, StandardCharsets.UTF_8) +
                            "&smsSignId=" + signId +
                            "&templateId=" + templateId;
        
        String urlStr = host + path + "?" + queryParams;
        
        logger.info("发送短信请求: phone={}, code={}", phone, code);
        logger.info("param值: {}", paramValue);
        logger.info("请求URL: {}", urlStr);
        logger.info("AppCode: {}", appCode.substring(0, Math.min(8, appCode.length())) + "...");
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "APPCODE " + appCode);
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int statusCode = conn.getResponseCode();
        logger.info("短信API响应状态码: {}", statusCode);
        
        StringBuilder sb = new StringBuilder();
        String line;
        
        BufferedReader br;
        if (statusCode >= 200 && statusCode < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            if (conn.getErrorStream() != null) {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            }
        }
        
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        
        String response = sb.toString();
        logger.info("短信API响应内容: {}", response);
        
        if (statusCode != 200) {
            logger.error("短信发送失败, 状态码: {}, 响应: {}", statusCode, response);
            return false;
        }
        
        return true;
    }
}