package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.CardType
import com.whitefang.stepsofbabylon.domain.model.OwnedCard
import com.whitefang.stepsofbabylon.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCardRepository : CardRepository {

    private var nextId = 1
    val cards = MutableStateFlow<List<OwnedCard>>(emptyList())

    override fun observeAllCards(): Flow<List<OwnedCard>> = cards
    override fun observeEquippedCards(): Flow<List<OwnedCard>> = cards.map { it.filter { c -> c.isEquipped } }

    override suspend fun addCard(type: CardType): Long {
        val id = nextId++
        cards.update { it + OwnedCard(id, type, 1, false) }
        return id.toLong()
    }

    override suspend fun upgradeCard(id: Int, newLevel: Int) {
        cards.update { list -> list.map { if (it.id == id) it.copy(level = newLevel) else it } }
    }

    override suspend fun equipCard(id: Int) {
        cards.update { list -> list.map { if (it.id == id) it.copy(isEquipped = true) else it } }
    }

    override suspend fun unequipCard(id: Int) {
        cards.update { list -> list.map { if (it.id == id) it.copy(isEquipped = false) else it } }
    }

    override suspend fun deleteCard(id: Int) {
        cards.update { list -> list.filter { it.id != id } }
    }
}
