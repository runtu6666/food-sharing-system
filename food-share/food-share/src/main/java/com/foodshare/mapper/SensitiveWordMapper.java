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

    // 查询所有敏感词字符串（type=0），用于构建 DFA 字典树
    @Select("SELECT word FROM sensitive_word WHERE type = 0")
    List<String> findAllWords();

    // 查询所有否定词字符串（type=1），用于否定语境判断
    @Select("SELECT word FROM sensitive_word WHERE type = 1")
    List<String> findAllNegationWords();

    // 后台管理：列表查询（返回全部类型）
    @Select("SELECT * FROM sensitive_word ORDER BY id DESC")
    List<SensitiveWord> findAll();

    // 后台管理：添加词条，type=0 敏感词 / type=1 否定词
    @Insert("INSERT IGNORE INTO sensitive_word(word, type) VALUES(#{word}, #{type})")
    int insert(@Param("word") String word, @Param("type") int type);

    // 后台管理：删除敏感词
    @Delete("DELETE FROM sensitive_word WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}