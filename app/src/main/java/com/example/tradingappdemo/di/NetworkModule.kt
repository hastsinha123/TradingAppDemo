package com.example.tradingappdemo.di

import com.example.tradingappdemo.data.network.BackoffPolicy
import com.example.tradingappdemo.data.network.ExponentialBackoffPolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .build()

    @Provides
    fun provideBackoffPolicy(): BackoffPolicy = ExponentialBackoffPolicy()
}
