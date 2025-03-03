package com.bockerl.snailmember.infrastructure.config

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionalConfig(
    _txAdvice: TxAdvice,
) {
    init {
        txAdvice = _txAdvice
    }

    companion object {
        private lateinit var txAdvice: TxAdvice

        fun <T> run(function: () -> T): T = txAdvice.run(function)
    }

    @Component
    class TxAdvice {
        @Transactional
        fun <T> run(function: () -> T): T = function()
    }
}