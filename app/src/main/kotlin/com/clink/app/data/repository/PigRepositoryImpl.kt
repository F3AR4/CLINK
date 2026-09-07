package com.clink.app.data.repository

import com.clink.app.data.local.dao.PigDao
import com.clink.app.data.local.entity.PigEntity
import com.clink.app.domain.model.Money
import com.clink.app.domain.model.Pig
import com.clink.app.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PigRepositoryImpl @Inject constructor(
    private val pigDao: PigDao
) : PigRepository {

    override fun getAllPigs(): Flow<List<Pig>> {
        return pigDao.getAllPigs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPigById(id: Long): Flow<Pig?> {
        return pigDao.getPigById(id).map { it?.toDomain() }
    }

    override suspend fun getPigByIdOnce(id: Long): Pig? {
        return pigDao.getPigByIdOnce(id)?.toDomain()
    }

    override suspend fun createPig(pig: Pig): Long {
        return pigDao.insertPig(PigEntity.fromDomain(pig))
    }

    override suspend fun updatePig(pig: Pig) {
        pigDao.updatePig(PigEntity.fromDomain(pig))
    }

    override suspend fun updateBalance(pigId: Long, newBalance: Money) {
        pigDao.updateBalance(id = pigId, balancePaise = newBalance.paise)
    }

    override suspend fun deletePig(pigId: Long) {
        pigDao.deletePig(pigId)
    }
}
