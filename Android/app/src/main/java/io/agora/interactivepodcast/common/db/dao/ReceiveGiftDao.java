package io.agora.interactivepodcast.common.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import java.util.List;

import io.agora.interactivepodcast.common.db.entity.ReceiveGiftEntity;

@Dao
public interface ReceiveGiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(ReceiveGiftEntity... entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<ReceiveGiftEntity> entities);

    @Query("select count(distinct `from`) from em_receive_gift where `to`=:chatRoomId")
    LiveData<Integer> loadSenders(String chatRoomId);

    @Query("select * from em_receive_gift where `to`=:chatRoomId order by timestamp desc")
    LiveData<List<ReceiveGiftEntity>> loadAll(String chatRoomId);

    @Query("select * from em_receive_gift where `to`=:chatRoomId order by timestamp desc")
    List<ReceiveGiftEntity> loadAllGift(String chatRoomId);

    @Query("select sum(gift_num) from em_receive_gift where `to`=:chatRoomId")
    int loadGiftTotalNum(String chatRoomId);

    @Query("delete from em_receive_gift where `to`=:chatRoomId")
    int clearData(String chatRoomId);
}
