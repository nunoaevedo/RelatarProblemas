package com.example.relatarproblemas.Retrofit.Type_Point

import java.io.Serializable

data class Type_Point (
        val id : Int,
        val type: String,
        val photo : String
        ) : Serializable {
        override fun toString(): String {
                return type
        }
}

