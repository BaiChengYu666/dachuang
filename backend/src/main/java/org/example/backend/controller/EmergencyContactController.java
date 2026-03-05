package org.example.backend.controller;

import org.example.backend.dto.ApiResponse;
import org.example.backend.entity.EmergencyContact;
import org.example.backend.repository.EmergencyContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guardian")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactRepository contactRepository;

    /** 查询用户的所有紧急联系人 */
    @GetMapping("/{userPhone}")
    public ApiResponse<List<EmergencyContact>> listContacts(@PathVariable String userPhone) {
        return ApiResponse.success(
            contactRepository.findByUserPhoneOrderByIsPrimaryDescCreatedAtAsc(userPhone)
        );
    }

    /** 添加紧急联系人 */
    @PostMapping
    public ApiResponse<EmergencyContact> addContact(@RequestBody EmergencyContact contact) {
        if (contact.getUserPhone() == null || contact.getName() == null || contact.getContactPhone() == null) {
            return ApiResponse.error("用户手机号、姓名和联系电话不能为空");
        }
        // 若标记为主要联系人，先取消其他主要联系人
        if (Boolean.TRUE.equals(contact.getIsPrimary())) {
            clearPrimary(contact.getUserPhone());
        }
        EmergencyContact saved = contactRepository.save(contact);
        return ApiResponse.success("联系人添加成功", saved);
    }

    /** 更新联系人信息 */
    @PutMapping("/{id}")
    public ApiResponse<EmergencyContact> updateContact(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return contactRepository.findById(id).map(c -> {
            if (body.containsKey("name"))         c.setName((String) body.get("name"));
            if (body.containsKey("contactPhone")) c.setContactPhone((String) body.get("contactPhone"));
            if (body.containsKey("relationship")) c.setRelationship((String) body.get("relationship"));
            if (body.containsKey("isPrimary")) {
                boolean primary = Boolean.TRUE.equals(body.get("isPrimary"));
                if (primary) clearPrimary(c.getUserPhone());
                c.setIsPrimary(primary);
            }
            return ApiResponse.success("更新成功", contactRepository.save(c));
        }).orElse(ApiResponse.error("联系人不存在"));
    }

    /** 删除联系人 */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> deleteContact(
            @PathVariable Long id,
            @RequestParam String userPhone) {
        if (!contactRepository.existsById(id)) {
            return ApiResponse.error("联系人不存在");
        }
        contactRepository.deleteByIdAndUserPhone(id, userPhone);
        return ApiResponse.success("删除成功", null);
    }

    private void clearPrimary(String userPhone) {
        contactRepository.findByUserPhoneOrderByIsPrimaryDescCreatedAtAsc(userPhone)
            .stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsPrimary()))
            .forEach(c -> {
                c.setIsPrimary(false);
                contactRepository.save(c);
            });
    }
}
