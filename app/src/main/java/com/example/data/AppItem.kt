package com.example.data

import android.graphics.drawable.Drawable

enum class AppCategory(val titleAr: String, val titleEn: String) {
    ALL("الكل", "All"),
    FAVORITES("المفضلة", "Favorites"),
    SYSTEM("النظام", "System"),
    SOCIAL("تواصل", "Social"),
    TOOLS("أدوات", "Tools"),
    MEDIA("وسائط", "Media")
}

enum class WallpaperType(val titleAr: String, val descriptionAr: String) {
    COSMIC("كوني ليلي", "تدرج فضائي داكن مع أضواء نيون خافتة"),
    DUNES("كثبان ذهبية", "أمواج رمال صحراوية دافئة عند الغروب"),
    AMOLED_BLACK("أسود AMOLED", "خلفية سوداء نقية توفر طاقة البطارية"),
    GRADIENT_CYBER("نيون سايبر", "تدرج عصري أرجواني مع أزرق سماوي"),
    GRADIENT_EMERALD("زمردي داكن", "تدرج هادئ وجميل بألوان الزمرد")
}

enum class IconStyle(val titleAr: String, val descriptionAr: String) {
    MATERIAL_YOU("Material You", "ألوان ديناميكية متناسقة مع واجهة أندرويد الحديثة"),
    GLASS_NEON("زجاجي نيون", "إطارات شبه شفافة متوهجة مع تأثير زجاجي حديث"),
    MINIMAL_MONO("بسيط مونو", "أيقونات دائرية أحادية اللون بتصميم بسيط وأنيق"),
    ROUNDED_SQUIRCLE("مربع منحني", "تصميم كلاسيكي نظيف ومريح للعين")
}

enum class ThemeMode(val titleAr: String) {
    DARK("داكن"),
    LIGHT("فاتح"),
    AMOLED("AMOLED فائق السواد")
}

data class AppItem(
    val id: String, // packageName/activityName
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable? = null,
    val category: AppCategory = AppCategory.ALL,
    val isFavorite: Boolean = false,
    val isDocked: Boolean = false,
    val installTime: Long = 0L
)
