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
    fun clearAll() {
        diaryRepository.clearAll()
        memoryRepository.clearAll()
        connectionRepository.clearAll()
        shoppingRepository.clearAll()
    }

    companion object {
        fun create(
            applicationContext: Context,
            encryptionPort: EncryptionPortAdapter,
            deviceBindingIdProvider: () -> String
        ): LifeFlowModuleRepositoryBindings {
            return LifeFlowModuleRepositoryBindings(
                diaryRepository = LocalDiaryRepository(
                    EncryptedModuleStore(
                        context = applicationContext,
                        prefsName = "lifeflow_diary",
                        encryption = encryptionPort,
                        deviceBindingContextProvider = deviceBindingIdProvider
                    )
                ),
                memoryRepository = LocalMemoryRepository(
                    EncryptedModuleStore(
                        context = applicationContext,
                        prefsName = "lifeflow_memory",
                        encryption = encryptionPort,
                        deviceBindingContextProvider = deviceBindingIdProvider
                    )
                ),
                connectionRepository = LocalConnectionRepository(
                    EncryptedModuleStore(
                        context = applicationContext,
                        prefsName = "lifeflow_connection",
                        encryption = encryptionPort,
                        deviceBindingContextProvider = deviceBindingIdProvider
                    )
                ),
                shoppingRepository = LocalShoppingRepository(
                    EncryptedModuleStore(
                        context = applicationContext,
                        prefsName = "lifeflow_shopping",
                        encryption = encryptionPort,
                        deviceBindingContextProvider = deviceBindingIdProvider
                    )
                )
            )
        }
    }
}
