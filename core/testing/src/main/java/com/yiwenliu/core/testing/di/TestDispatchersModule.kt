package com.yiwenliu.core.testing.di

import com.yiwenliu.core.common.di.Dispatcher
import com.yiwenliu.core.common.di.DispatchersModule
import com.yiwenliu.core.common.di.TmdbDispatchers.Default
import com.yiwenliu.core.common.di.TmdbDispatchers.IO
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatchersModule::class],
)
object TestDispatchersModule {
    @Provides
    @Dispatcher(IO)
    fun providesIODispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher

    @Provides
    @Dispatcher(Default)
    fun providesDefaultDispatcher(testDispatcher: TestDispatcher): CoroutineDispatcher = testDispatcher
}
