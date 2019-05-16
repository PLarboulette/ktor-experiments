package plarboulette.models

import java.util.*

data class Hero (val id: UUID, val name: String, val age: Int)

data class HeroInput (val name: String, val age: Int)

data class PostHero (val hero: HeroInput)
