package com.example.model

enum class ExecutiveRole(
    val title: String,
    val subtitle: String,
    val description: String,
    val hiringCost: Double,
    val dailySalary: Double,
    val buffSummary: String
) {
    CFO(
        title = "CFO - Finans Direktörü",
        subtitle = "Kemal Özkan (Eski Banka Genel Müdürü)",
        description = "Vergi optimizasyonu sağlar, şirket kâr vergilerini %20'den %15'e düşürür ve banka kredi faizlerinde %15 indirim kazandırır.",
        hiringCost = 350_000.0,
        dailySalary = 12_000.0,
        buffSummary = "Vergiler %20 ➔ %15, Kredi faizi -%15"
    ),
    COO(
        title = "COO - Operasyon Direktörü",
        subtitle = "Selin Yılmaz (Uluslararası Tedarik Uzmanı)",
        description = "Tedarik zincirini optimize eder ve tüm şirketlerin hammadde ile operasyon maliyetlerini %18 oranında düşürür.",
        hiringCost = 450_000.0,
        dailySalary = 15_000.0,
        buffSummary = "Tüm işletme maliyetleri -%18"
    ),
    CMO(
        title = "CMO - Pazarlama Direktörü",
        subtitle = "Barış Demir (Dijital Büyüme & Marka Gurusu)",
        description = "Marka değerini artırır, reklam kampanyalarının etkisini %50 uzatır ve holdingin tüm şirket satış cirolarını %15 yükseltir.",
        hiringCost = 400_000.0,
        dailySalary = 14_000.0,
        buffSummary = "Tüm şirket ciroları +%15"
    ),
    CTO(
        title = "CTO - Teknoloji Direktörü",
        subtitle = "Dr. Caner Tekin (Yapay Zeka & Sistem Mimarı)",
        description = "Holding şirketlerinde otomasyon geliştirme maliyetini yarıya indirir ve teknoloji şirketlerinin değerleme çarpanını %30 artırır.",
        hiringCost = 600_000.0,
        dailySalary = 20_000.0,
        buffSummary = "Otomasyon -%50 indirimli, Teknoloji çarpanı +%30"
    )
}

data class ExecutiveState(
    val role: ExecutiveRole,
    val isHired: Boolean = false,
    val hiredDay: Int = 0
)
