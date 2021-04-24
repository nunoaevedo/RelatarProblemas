package com.example.relatarproblemas.Retrofit.Type_Point

data class Type_Point (
        val id : Int,
        val type: String,
        val photo : String
        ){
        override fun toString(): String {
                return type
        }
}

