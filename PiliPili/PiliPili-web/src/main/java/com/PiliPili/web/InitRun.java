package com.PiliPili.web;

import com.PiliPili.component.EsSearchComponent;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InitRun implements ApplicationRunner {
    @Resource
    EsSearchComponent esSearchComponent;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        esSearchComponent.CreateIndex();
    }
}
