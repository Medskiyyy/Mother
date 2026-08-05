package com.mother.app.data.local

import androidx.room.TypeConverter
import com.mother.app.data.model.AttachmentType
import com.mother.app.data.model.Priority
import com.mother.app.data.model.RepeatType
import com.mother.app.data.model.SessionSource
import com.mother.app.data.model.StatusSchedule
import com.mother.app.data.model.StatusTask
import com.mother.app.data.model.Theme

/** Room type converters for the enum values stored as strings. */
class Converters {

    @TypeConverter
    fun priorityToString(value: Priority): String = value.name

    @TypeConverter
    fun stringToPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun statusTaskToString(value: StatusTask): String = value.name

    @TypeConverter
    fun stringToStatusTask(value: String): StatusTask = StatusTask.valueOf(value)

    @TypeConverter
    fun statusScheduleToString(value: StatusSchedule): String = value.name

    @TypeConverter
    fun stringToStatusSchedule(value: String): StatusSchedule = StatusSchedule.valueOf(value)

    @TypeConverter
    fun repeatTypeToString(value: RepeatType): String = value.name

    @TypeConverter
    fun stringToRepeatType(value: String): RepeatType = RepeatType.valueOf(value)

    @TypeConverter
    fun themeToString(value: Theme): String = value.name

    @TypeConverter
    fun stringToTheme(value: String): Theme = Theme.valueOf(value)

    @TypeConverter
    fun attachmentTypeToString(value: AttachmentType): String = value.name

    @TypeConverter
    fun stringToAttachmentType(value: String): AttachmentType = AttachmentType.valueOf(value)

    @TypeConverter
    fun sessionSourceToString(value: SessionSource): String = value.name

    @TypeConverter
    fun stringToSessionSource(value: String): SessionSource = SessionSource.valueOf(value)
}