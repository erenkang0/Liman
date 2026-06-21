package com.liman.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime

/* ---- Nefes & Topraklanma ---- */

/** Bir nefes evresi. */
enum class BreathPhase(val label: String) {
    INHALE("Nefes al"),
    HOLD("Tut"),
    EXHALE("Ver"),
    HOLD_AFTER("Bekle"),
}

/** Nefes tekniği — her evrenin saniyesi. 0 saniye = evre atlanır. */
enum class BreathingTechnique(
    val displayName: String,
    val description: String,
    val inhale: Int,
    val hold: Int,
    val exhale: Int,
    val holdAfter: Int,
) {
    FOUR_SEVEN_EIGHT("4-7-8", "Uykuya ve sakinliğe", 4, 7, 8, 0),
    BOX("Kutu nefesi", "Odak ve denge", 4, 4, 4, 4),
    CALMING("Sakinleştirici", "Hızlı gevşeme", 4, 0, 6, 0);

    fun phases(): List<Pair<BreathPhase, Int>> = buildList {
        add(BreathPhase.INHALE to inhale)
        if (hold > 0) add(BreathPhase.HOLD to hold)
        add(BreathPhase.EXHALE to exhale)
        if (holdAfter > 0) add(BreathPhase.HOLD_AFTER to holdAfter)
    }

    val cycleSeconds: Int get() = inhale + hold + exhale + holdAfter
}

/* ---- Düşünce Kaydı (BDT yeniden çerçeveleme) ---- */

data class ThoughtRecord(
    val id: String,
    val situation: String = "",
    val automaticThought: String = "",
    val emotion: String = "",
    val emotionIntensity: Int = 50,        // 0..100
    val evidenceFor: String = "",
    val evidenceAgainst: String = "",
    val balancedThought: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

/* ---- Şükran Defteri (baskısız, seri yok) ---- */

data class GratitudeEntry(
    val id: String,
    val items: List<String>,               // 3 yönlendirmeli kart
    val timestamp: LocalDateTime = LocalDateTime.now(),
)

/* ---- Zaman Kapsülü ---- */

data class TimeCapsule(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val openOn: LocalDate,
    val sealed: Boolean = false,
) {
    fun isOpenable(today: LocalDate = LocalDate.now()): Boolean =
        sealed && !today.isBefore(openOn)
}
