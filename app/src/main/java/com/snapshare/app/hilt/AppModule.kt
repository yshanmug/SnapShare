package com.snapshare.app.hilt

import android.content.Context
import com.snapshare.app.db.EventDao
import com.snapshare.app.db.EventDatabase
import com.snapshare.app.repository.EventRepository
import com.snapshare.app.viewmodel.SnapShareViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideEventDao(@ApplicationContext appContext: Context): EventDao {
        return EventDatabase.getInstance(appContext).eventDao()
    }

    @Provides
    fun provideEventRepository(eventDao: EventDao, @ApplicationContext context: Context) =
        EventRepository(eventDao, context)

    @Provides
    fun provideEventViewModel(
        eventRepository: EventRepository,
        @ApplicationContext context: Context,
    ) = SnapShareViewModel(eventRepository = eventRepository, context)

}
