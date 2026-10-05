package com.example.data.room

import androidx.room.TypeConverter

/** Room type converters shared by every entity in [AppDatabase]. */
class Converters {

  @TypeConverter fun fromTaskStatus(status: TaskStatus): String = status.storageValue

  @TypeConverter fun toTaskStatus(value: String?): TaskStatus = TaskStatus.fromStorage(value)
}
