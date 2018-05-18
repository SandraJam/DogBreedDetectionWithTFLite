package com.dupre.sandra.dogbreeddetectorwithtflite

interface DogView {
    fun displayDogBreed(dogBreed: String, winPercent: Float)
    fun displayError()
}