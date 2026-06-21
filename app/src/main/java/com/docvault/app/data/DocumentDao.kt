package com.docvault.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<Document>>

    @Query(
        """
        SELECT * FROM documents 
        WHERE customName LIKE '%' || :query || '%' 
        OR tag LIKE '%' || :query || '%'
        ORDER BY dateAdded DESC
        """
    )
    fun search(query: String): Flow<List<Document>>

    @Insert
    suspend fun insert(document: Document): Long

    @Update
    suspend fun update(document: Document)

    @Delete
    suspend fun delete(document: Document)
}
