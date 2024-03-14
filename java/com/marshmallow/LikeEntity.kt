package com.marshmallow
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "LikeEntity")
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: Boolean,
    val save_path: String,
    val org_path: String,
    val speak_str: String,
    val txt_title: String,
    val picName: String,
    val picURL: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeByte(if (type) 1 else 0)
        parcel.writeString(save_path)
        parcel.writeString(org_path)
        parcel.writeString(speak_str)
        parcel.writeString(txt_title)
        parcel.writeString(picName)
        parcel.writeString(picURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LikeEntity> {
        override fun createFromParcel(parcel: Parcel): LikeEntity {
            return LikeEntity(parcel)
        }

        override fun newArray(size: Int): Array<LikeEntity?> {
            return arrayOfNulls(size)
        }
    }
}