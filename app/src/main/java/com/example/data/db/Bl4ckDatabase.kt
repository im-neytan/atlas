package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.HistoricoItem
import com.example.data.model.PedidoFila
import com.example.data.model.SimCard

@Database(
    entities = [PedidoFila::class, HistoricoItem::class, SimCard::class],
    version = 2,
    exportSchema = false
)
abstract class Bl4ckDatabase : RoomDatabase() {
    abstract fun pedidoFilaDao(): PedidoFilaDao
    abstract fun historicoDao(): HistoricoDao
    abstract fun simCardDao(): SimCardDao

    companion object {
        @Volatile
        private var INSTANCE: Bl4ckDatabase? = null

        fun getInstance(context: Context): Bl4ckDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Bl4ckDatabase::class.java,
                    "bl4ck_system.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
