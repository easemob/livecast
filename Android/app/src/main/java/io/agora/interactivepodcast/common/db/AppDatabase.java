package io.agora.interactivepodcast.common.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.agora.interactivepodcast.common.db.converter.DateConverter;
import io.agora.interactivepodcast.common.db.dao.ReceiveGiftDao;
import io.agora.interactivepodcast.common.db.entity.ReceiveGiftEntity;


@Database(entities = {ReceiveGiftEntity.class}, version = 2)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ReceiveGiftDao receiveGiftDao();

}
