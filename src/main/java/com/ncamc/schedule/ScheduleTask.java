package com.ncamc.schedule;

import com.ncamc.config.ApplicationContextProvider;
import com.ncamc.entity.User;
import com.ncamc.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 测试启动定时任务
 */
@Slf4j
@Component
public class ScheduleTask implements Runnable {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取Bean实例
     */
    public ScheduleTask(){
        userMapper = ApplicationContextProvider.getBean(UserMapper.class);
        passwordEncoder = ApplicationContextProvider.getBean(PasswordEncoder.class);
    }

    @Override
    public void run() {
        System.out.println("我是一个定时任务");
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            String status = user.getStatus();
            if (status.equals("0")){
                System.out.println(user.getUsername()+"账户没有被锁定");
            }
            if (status.equals("1")) {
                System.out.println(user.getUsername()+"账户被锁定");
                System.out.println("正在自动解锁该账户");
                System.out.println("正在重置状态");
                user.setStatus("0");
                userMapper.updateStatusById(user.getStatus(),user.getId());
                System.out.println("正在重置登陆次数");
                user.setNumber(0);
                userMapper.chongZhiNumberById(user.getNumber(),user.getId());
                System.out.println("正在重置密码");
                user.setPassword(passwordEncoder.encode("123"));
                userMapper.updatePasswordById(user.getPassword(),user.getId());
                System.out.println(user.getUsername()+"账户重置成功，欢迎再次使用");
            }
        }
    }
}