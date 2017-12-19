package com.achain.service.impl;

import com.achain.service.IActTransactionMapperService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActTransactionMapperServiceImplTest {



    @Autowired
    private IActTransactionMapperService actTransactionMapperService;
    @Test
    public void walletAccountTransactionHistory() {
        Map<String, Object> stringObjectMap = actTransactionMapperService.walletAccountTransactionHistory(2314L, "12","fdsaf");
        System.out.println("fdsaf");
    }
}