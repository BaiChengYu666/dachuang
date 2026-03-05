package org.example.backend.repository;

import org.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // 主键已经是手机号，无需额外方法
    // JpaRepository 自带的方法：
    // - findById(String phone)
    // - existsById(String phone)
    // - save(User user)
    // - deleteById(String phone)
}
