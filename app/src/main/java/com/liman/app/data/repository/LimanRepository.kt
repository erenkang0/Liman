package com.liman.app.data.repository

import com.liman.app.data.crypto.CryptoManager
import com.liman.app.data.local.LimanStore
import com.liman.app.data.model.Contact
import com.liman.app.data.model.EntryKind
import com.liman.app.data.model.GratitudeEntry
import com.liman.app.data.model.JournalEntry
import com.liman.app.data.model.JournalFont
import com.liman.app.data.model.Mention
import com.liman.app.data.model.NotebookEntry
import com.liman.app.data.model.StyleSpan
import com.liman.app.data.model.VoiceNote
import com.liman.app.data.model.MoodEntry
import com.liman.app.data.model.MoodFace
import com.liman.app.data.model.MoodIntensity
import com.liman.app.data.model.MoodTrigger
import com.liman.app.data.model.ClientStatus
import com.liman.app.data.model.RelationshipType
import com.liman.app.data.model.RiskLevel
import com.liman.app.data.model.ThoughtRecord
import com.liman.app.data.model.TimeCapsule
import com.liman.app.data.model.TreatmentGoal
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/**
 * Tek kaynaklı veri deposu. Akışlar bellek-içi [MutableStateFlow]'larda tutulur;
 * her değişiklik [LimanStore] ile cihazda **kalıcı + şifreli** olarak saklanır.
 * Uygulama açılışında [LimanStore]'dan geri yüklenir — böylece kapat/aç'ta veri
 * (kişiler, defterler, ruh hali, günlük) silinmez.
 */
class LimanRepository(
    private val crypto: CryptoManager,
    private val store: LimanStore? = null,
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
        persist()
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
        tags: List<String> = emptyList(),
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
            tags = tags,
        )
        _storedJournals.update { (listOf(entry) + it).sortedByDescending { j -> j.timestamp } }
        persist()
    }

    fun deleteJournal(id: String) {
        _storedJournals.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    /* ----------------------------- Kişiler ------------------------------ */
    private val _contacts = MutableStateFlow(emptyList<Contact>())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    fun contact(id: String): Contact? = _contacts.value.firstOrNull { it.id == id }

    fun upsertContact(contact: Contact) {
        _contacts.update { list ->
            if (list.any { it.id == contact.id }) list.map { if (it.id == contact.id) contact else it }
            else list + contact
        }
        persist()
    }

    /** Yeni danışan/kişi ekler ve oluşturulan kaydın id'sini döndürür. */
    fun addContact(
        name: String,
        title: String = "",
        bio: String = "",
        relationship: RelationshipType = RelationshipType.FRIEND,
        birthday: LocalDate? = null,
        tags: List<String> = emptyList(),
        closeness: Int = 3,
        risk: RiskLevel = RiskLevel.NONE,
        status: ClientStatus = ClientStatus.ACTIVE,
        intakeDate: LocalDate? = null,
        accentColorArgb: Int? = null,
    ): String {
        val id = newId()
        upsertContact(
            Contact(
                id = id,
                name = name,
                title = title,
                bio = bio,
                relationship = relationship,
                birthday = birthday,
                tags = tags,
                closeness = closeness,
                risk = risk,
                status = status,
                intakeDate = intakeDate,
                accentColorArgb = accentColorArgb,
                lastContact = null,
            )
        )
        return id
    }

    fun deleteContact(id: String) {
        _contacts.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    fun logContact(id: String, date: LocalDate = LocalDate.now()) {
        _contacts.update { list -> list.map { if (it.id == id) it.copy(lastContact = date) else it } }
        persist()
    }

    /** Kişinin defterine yeni bir kayıt (serbest not ya da yapılandırılmış seans) ekler. */
    fun addEntry(
        contactId: String,
        kind: EntryKind = EntryKind.NOTE,
        title: String = "",
        text: String = "",
        subjective: String = "",
        objective: String = "",
        assessment: String = "",
        plan: String = "",
        durationMin: Int? = null,
        feeling: MoodFace? = null,
        photos: List<String> = emptyList(),
        voice: VoiceNote? = null,
        tags: List<String> = emptyList(),
        mentions: List<Mention> = emptyList(),
    ) {
        val entry = NotebookEntry(
            id = newId(),
            kind = kind,
            title = title,
            text = text,
            subjective = subjective,
            objective = objective,
            assessment = assessment,
            plan = plan,
            durationMin = durationMin,
            feeling = feeling,
            photos = photos.take(5),
            voice = voice,
            tags = tags,
            mentions = mentions,
        )
        _contacts.update { list ->
            list.map { if (it.id == contactId) it.copy(entries = listOf(entry) + it.entries) else it }
        }
        persist()
    }

    /* ----------------------- Tedavi planı hedefleri --------------------- */
    fun addGoal(contactId: String, text: String) {
        val goal = TreatmentGoal(id = newId(), text = text)
        _contacts.update { list ->
            list.map { if (it.id == contactId) it.copy(goals = it.goals + goal) else it }
        }
        persist()
    }

    fun toggleGoal(contactId: String, goalId: String) {
        _contacts.update { list ->
            list.map { c ->
                if (c.id != contactId) c
                else c.copy(goals = c.goals.map { if (it.id == goalId) it.copy(done = !it.done) else it })
            }
        }
        persist()
    }

    fun deleteGoal(contactId: String, goalId: String) {
        _contacts.update { list ->
            list.map { c ->
                if (c.id != contactId) c else c.copy(goals = c.goals.filterNot { it.id == goalId })
            }
        }
        persist()
    }

    fun deleteEntry(contactId: String, entryId: String) {
        _contacts.update { list ->
            list.map {
                if (it.id == contactId) it.copy(entries = it.entries.filterNot { e -> e.id == entryId })
                else it
            }
        }
        persist()
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

    /* ----------------------------- Kalıcılık ---------------------------- */
    init {
        if (store != null) {
            scope.launch {
                val snapshot = store.load()
                if (snapshot.moods.isNotEmpty()) _moods.value = snapshot.moods
                if (snapshot.journals.isNotEmpty()) _storedJournals.value = snapshot.journals
                if (snapshot.contacts.isNotEmpty()) _contacts.value = snapshot.contacts
            }
        }
    }

    private fun persist() {
        val s = store ?: return
        scope.launch {
            s.save(
                LimanStore.Snapshot(
                    moods = _moods.value,
                    journals = _storedJournals.value,
                    contacts = _contacts.value,
                )
            )
        }
    }

    /* ------------------------------ Yardımcı ---------------------------- */
    private fun safeDecrypt(value: String): String =
        runCatching { crypto.decrypt(value) }.getOrDefault(value)

    private fun newId() = UUID.randomUUID().toString()
}
