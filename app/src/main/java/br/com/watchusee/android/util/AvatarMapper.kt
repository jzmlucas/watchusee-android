package br.com.watchusee.android.util

object AvatarMapper {
    private const val BASE_URL = "https://api.iconify.design"
    private const val ICON_COLOR = "%23FFB800"

    private val iconMap = mapOf(
        "POPCORN" to "lucide:popcorn",
        "CLAPPERBOARD" to "lucide:clapperboard",
        "FILM_REEL" to "ph:film-reel-bold",
        "TICKET" to "lucide:ticket",
        "DIRECTOR_CHAIR" to "iconoir:director-chair",
        "STAR" to "lucide:star",
        "COMEDY_MASK" to "lucide:masks",
        "TRAGEDY_MASK" to "ph:mask-sad-bold",
        "GHOST" to "lucide:ghost",
        "ROBOT" to "lucide:bot",
        "ALIEN" to "ph:alien-bold",
        "ASTRONAUT" to "ph:rocket-launch-bold"
    )

    fun getIconUrl(id: String?): String {
        val cleanId = id?.trim()?.uppercase()
        val identifier = iconMap[cleanId] ?: "lucide:user"
        val parts = identifier.split(":")
        
        return if (parts.size == 2) {
            "$BASE_URL/${parts[0]}/${parts[1]}.svg?color=$ICON_COLOR"
        } else {
            "$BASE_URL/lucide/user.svg?color=$ICON_COLOR"
        }
    }
}
