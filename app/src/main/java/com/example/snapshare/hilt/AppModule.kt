package com.example.snapshare.hilt

import android.content.Context
import com.example.snapshare.db.EventDao
import com.example.snapshare.db.EventDatabase
import com.example.snapshare.repository.EventRepository
import com.example.snapshare.viewmodel.SnapShareViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideEventDao(@ApplicationContext appContext: Context) : EventDao {
        return EventDatabase.getInstance(appContext).eventDao()
    }
    @Provides
    fun provideEventRepository(eventDao: EventDao) = EventRepository(eventDao)
    @Provides
    fun provideEventViewModel(eventRepository: EventRepository, @ApplicationContext context: Context) = SnapShareViewModel(eventRepository = eventRepository,context)

}
