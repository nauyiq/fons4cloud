package com.fons.cloud.util;

import com.fons.cloud.util.concurrent.AbstractIExecutorService;
import com.fons.cloud.util.concurrent.IExecutorService;
import com.fons.cloud.util.concurrent.IExecutorsRepository;

/**
 * 通过的线程池工具类
 * @author qiyuan.hong
 * @version 1.0
 * @date 2023/12/11 16:24
 */
public class ProjectExecutors extends AbstractIExecutorService {
    private static final String NAME = "fons4cloud";

    private ProjectExecutors() {
        super(NAME);
    }

    public static IExecutorService getInstance() {
        IExecutorService executor = IExecutorsRepository.getExecutor(NAME);
        if (executor == null) {
            executor = new ProjectExecutors();
            IExecutorsRepository.setExecutor(NAME, executor);
        }
        return executor;
    }



}
