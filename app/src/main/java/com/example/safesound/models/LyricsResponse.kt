package com.example.safesound.models

data class LyricsResponse (
    val lyrics: String? //-> un string nullable por si la API de lyrics.ovh no devuelve letras
)
