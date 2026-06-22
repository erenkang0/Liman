package com.liman.app.data.repository

import com.liman.app.data.crypto.CryptoManager
import com.liman.app.data.model.Contact
import com.liman.app.data.model.EmotionalWeather
import com.liman.app.data.model.GratitudeEntry
import com.liman.app.data.model.JournalEntry
import com.liman.app.data.model.JournalFont
import com.liman.app.data.model.Memory
import com.liman.app.data.model.StyleSpan
import com.liman.app.data.model.VoiceNote
import com.liman.app.data.model.MoodEntry
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.MoodIntensity
import com.liman.app.data.model.MoodTrigger
import com.liman.app.data.model.RelationshipType
import com.liman.app.data.model.ThoughtRecord
import com.liman.app.data.model.TimeCapsule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Tek kaynaklı, bellek-içi veri deposu (uygulama yaşam döngüsü boyunca tekil).
 * Günlük gövdeleri [CryptoManager] ile cihaz içinde şifreli tutulur.
 *
 * Üretimde bu katmanın altına Room + SQLCipher / şifreli dosya kalıcılığı
 * eklenir; UI sözleşmesi (StateFlow'lar) aynı kalır.
 */
class LimanRepository(
    private val crypto: CryptoManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {

    /* ----------------------------- Ruh hali ----------------------------- */
    private val _moods = MutableStateFlow(emptyList<MoodEntry>())
    val moods: StateFlow<List<MoodEntry>> = _moods.asStateFlow()

    fun addMood(
        face: MoodFace,
        intensity: MoodIntensity = MoodIntensity.MODERATE,
        triggers: List<MoodTrigger> = emptyList(),
        note: String = "",
    ) {
        val entry = MoodEntry(
            id = newId(),
            face = face,
            intensity = intensity,
            triggers = triggers,
            note = note,
        )
        _moods.update { (listOf(entry) + it).sortedByDescending { m -> m.timestamp } }
    }

    fun moodFor(date: LocalDate): MoodEntry? =
        _moods.value.filter { it.date == date }.maxByOrNull { it.timestamp }

    /* ----------------------------- Günlük ------------------------------ */
    // İçeride şifreli gövde ile saklanır.
    private val _storedJournals = MutableStateFlow(emptyList<JournalEntry>())

    /** UI'a çözülmüş (düz metin) olarak sunulan günlük akışı. */
    val journals: StateFlow<List<JournalEntry>> = _storedJournals
        .map { list -> list.map { it.copy(body = safeDecrypt(it.body)) } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun addJournal(
        title: String,
        body: String,
        spans: List<StyleSpan> = emptyList(),
        font: JournalFont = JournalFont.SERIF,
        moodFace: MoodFace? = null,
        voice: VoiceNote? = null,
        photos: List<String> = emptyList(),
    ) {
        val entry = JournalEntry(
            id = newId(),
            title = title,
            body = crypto.encrypt(body),
            spans = spans,
            font = font,
            moodFace = moodFace,
            voice = voice,
            photos = photos.take(5),
        )
        _storedJournals.update { (listOf(entry) + it).sortedByDescending { j -> j.timestamp } }
    }

    fun deleteJournal(id: String) {
        _storedJournals.update { list -> list.filterNot { it.id == id } }
    }

    /* ----------------------------- Bağlar ------------------------------ */
    private val _contacts = MutableStateFlow(emptyList<Contact>())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    fun contact(id: String): Contact? = _contacts.value.firstOrNull { it.id == id }

    fun upsertContact(contact: Contact) {
        _contacts.update { list ->
            if (list.any { it.id == contact.id }) list.map { if (it.id == contact.id) contact else it }
            else list + contact
        }
    }

    fun addContact(
        name: String,
        relationship: RelationshipType,
        birthday: LocalDate?,
        weather: EmotionalWeather,
    ) {
        upsertContact(
            Contact(
                id = newId(),
                name = name,
                relationship = relationship,
                birthday = birthday,
                weather = weather,
                lastContact = LocalDate.now(),
            )
        )
    }

    fun deleteContact(id: String) {
        _contacts.update { list -> list.filterNot { it.id == id } }
    }

    fun logContact(id: String, date: LocalDate = LocalDate.now()) {
        _contacts.update { list -> list.map { if (it.id == id) it.copy(lastContact = date) else it } }
    }

    fun addMemory(
        contactId: String,
        title: String,
        note: String,
        photos: List<String> = emptyList(),
        voice: VoiceNote? = null,
    ) {
        val memory = Memory(
            id = newId(),
            title = title,
            note = note,
            photos = photos.take(5),
            voice = voice,
        )
        _contacts.update { list ->
            list.map { if (it.id == contactId) it.copy(memories = listOf(memory) + it.memories) else it }
        }
    }

    /* --------------------------- Şükran defteri ------------------------- */
    private val _gratitude = MutableStateFlow(emptyList<GratitudeEntry>())
    val gratitude: StateFlow<List<GratitudeEntry>> = _gratitude.asStateFlow()

    fun addGratitude(items: List<String>) {
        val clean = items.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return
        _gratitude.update { listOf(GratitudeEntry(newId(), clean)) + it }
    }

    /* --------------------------- Zaman kapsülü -------------------------- */
    private val _capsules = MutableStateFlow(emptyList<TimeCapsule>())
    val capsules: StateFlow<List<TimeCapsule>> = _capsules.asStateFlow()

    fun addCapsule(title: String, body: String, openOn: LocalDate): String {
        val capsule = TimeCapsule(newId(), title, body, openOn = openOn, sealed = false)
        _capsules.update { listOf(capsule) + it }
        return capsule.id
    }

    fun sealCapsule(id: String) {
        _capsules.update { list -> list.map { if (it.id == id) it.copy(sealed = true) else it } }
    }

    /* --------------------------- Düşünce kaydı -------------------------- */
    private val _thoughtRecords = MutableStateFlow<List<ThoughtRecord>>(emptyList())
    val thoughtRecords: StateFlow<List<ThoughtRecord>> = _thoughtRecords.asStateFlow()

    fun addThoughtRecord(record: ThoughtRecord) {
        _thoughtRecords.update { listOf(record.copy(id = newId())) + it }
    }

    /* ------------------------------ Yardımcı ---------------------------- */
    private fun safeDecrypt(value: String): String =
        runCatching { crypto.decrypt(value) }.getOrDefault(value)

    private fun newId() = UUID.randomUUID().toString()
}
