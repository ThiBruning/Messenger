package com.ifpr.thiago.messenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUri: String): Parcelable {
    constructor() : this("", "", "")
}