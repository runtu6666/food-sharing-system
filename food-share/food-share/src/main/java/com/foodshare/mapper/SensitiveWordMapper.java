package com.foodshare.mapper;

import com.foodshare.entity.SensitiveWord;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 敏感词库 Mapper 接口
 * 负责敏感词的增删改查，为 DFA 热更新提供底层数据支持
 */
@Mapper
public interface SensitiveWordMapper {

    // 查询数据库中所有的敏感词字符串（用于构建 DFA 字典树）
    @Select("SELECT word FROM sensitive_word")
    List<String> findAllWords();

    // 后台管理：分页或列表查询敏感词实体
    @Select("SELECT * FROM sensitive_word ORDER BY id DESC")
    List<SensitiveWord> findAll();

    // 后台管理：添加敏感词（忽略重复插入报错）
    @Insert("INSERT IGNORE INTO sensitive_word(word) VALUES(#{word})")
    int insert(@Param("word") String word);

    // 后台管理：删除敏感词
    @Delete("DELETE FROM sensitive_word WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}