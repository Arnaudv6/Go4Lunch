package com.freemyip.go4lunch.data.pois

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.freemyip.go4lunch.MainApplication
import com.freemyip.go4lunch.utils.Constants.Companion.POI_ENTITY
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PoiRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // mock PoiRepository constructor parameters for getPoiRepository() fun bellow
    private val applicationMock = mockk<MainApplication>()
    private val poiDaoMock = mockk<PoiDao>()
    private val poiRetrofitMock = mockk<PoiRetrofit>()

    @Before
    fun setUp() {
        coJustRun { poiDaoMock.nukePOIS() }
        coEvery { poiDaoMock.getPoiById(POI_ENTITY.id) }.returns(POI_ENTITY)
        every { poiDaoMock.getPoiEntities() }.returns(flowOf(emptyList()))
    }

    @Test
    fun getCachedPOIsListFlow() {
    }

    @Test
    fun getPoiById() = runTest {
        // given
        val repository = getPoiRepository()

        // when
        val result = repository.getPoiById(POI_ENTITY.id)

        // then
        assertEquals(POI_ENTITY, result)
        coVerify(exactly = 1) { poiDaoMock.getPoiById(any()) }
    }

    @Test
    fun fetchPOIsInBoundingBox() {
    }

    @Test
    fun fetchPOIsInList() {
    }

    @Test
    fun clearCache() = runTest {
        // given
        val repository = getPoiRepository()

        // when
        repository.clearCache()

        // then
        coVerify(exactly = 1) { poiDaoMock.nukePOIS() }
    }


    private fun getPoiRepository() = PoiRepository(
        application = applicationMock,
        poiRetrofit = poiRetrofitMock,
        poiDao = poiDaoMock,
        mockk() // TODO Arnaud
    )


}