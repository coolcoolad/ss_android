package com.github.shadowsocks.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable
import java.util.Date
/**
 * Created by yajie on 4/2/2018.
 */
class User(
        @NonNull
        @JsonProperty("id")
        val id: String,

        @NonNull
        @JsonProperty("username")
        val username: String,

        @Nullable
        @JsonProperty("other_contacts")
        val otherContacts: String?,

        @Nullable
        @JsonProperty("picture")
        val picture: String?,

        @Nullable
        @JsonProperty("email")
        val email: String?,

        @NonNull
        @JsonProperty("service_termination_time")
        val serviceTerminationTime: Date
)