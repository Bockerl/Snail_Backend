package com.bockerl.snailmember.member.command.domain.repository

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class TempMemberRepository(private val redisTemplate: RedisTemplate<String, TempMember>) {

    companion object {
        private const val EXPIRE_MINUTES = 30L
    }

    // 새로운 임시 회원 생성
    fun save(tempMember: TempMember): String {
        val key = TempMember.createRedisKey(tempMember.redisId)
        redisTemplate.opsForValue().set(key, tempMember, Duration.ofMinutes(EXPIRE_MINUTES))
        return tempMember.redisId
    }

    // 회원 가입 과정 거치면서 TTL 늘려주는 메서드
    fun update(redisId: String, tempMember: TempMember) {
        val key = TempMember.createRedisKey(redisId)
        val remainingTtl = redisTemplate.getExpire(key)
        if (remainingTtl > 0) {
            redisTemplate.opsForValue().set(key, tempMember, Duration.ofMinutes(EXPIRE_MINUTES))
        } else {
            throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        }
    }

    fun find(redisId: String): TempMember? {
        val key = TempMember.createRedisKey(redisId)
        val tempMember =
            redisTemplate.opsForValue().get(key)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        return tempMember
    }

    fun delete(redisId: String) {
        val key = TempMember.createRedisKey(redisId)
        redisTemplate.delete(key)
    }
}
