package com.lifeflow

import android.content.Context
import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.data.store.EncryptedModuleStore
import com.lifeflow.security.EncryptionPortAdapter

internal class LifeFlowModuleRepositoryBindings private constructor(
    val diaryRepository: LocalDiaryRepository,
    val memoryRepository: LocalMemoryRepository,
    val connectionRepository: LocalConnectionRepository,
    val shoppingRepository: LocalShoppingRepository
) {
    companion object {
        fun create(
            applicationContext: Context,
            encryptionPort: EncryptionPortAdapter
        ): LifeFlowModuleRepositoryBindings {
            return LifeFlowModuleRepositoryBindings(
                diaryRepository = LocalDiaryRepository(
                    EncryptedModuleStore(applicationContext, "lifeflow_diary", encryptionPort)
                ),
                memoryRepository = LocalMemoryRepository(
                    EncryptedModuleStore(applicationContext, "lifeflow_memory", encryptionPort)
                ),
                connectionRepository = LocalConnectionRepository(
                    EncryptedModuleStore(applicationContext, "lifeflow_connection", encryptionPort)
                ),
                shoppingRepository = LocalShoppingRepository(
                    EncryptedModuleStore(applicationContext, "lifeflow_shopping", encryptionPort)
                )
            )
        }
    }
}
