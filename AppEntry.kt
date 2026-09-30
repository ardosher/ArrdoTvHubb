package uz.ardo.tvhub

data class AppEntry(
    val name: String,
    val packages: List<String>,
    val color: String,
    val action: String? = null,
    val url: String? = null,
    val key: String = name
)

data class Category(
    val title: String,
    val apps: List<AppEntry>
)
