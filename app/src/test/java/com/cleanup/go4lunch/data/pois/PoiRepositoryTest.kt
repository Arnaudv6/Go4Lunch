package com.cleanup.go4lunch.data.pois

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cleanup.go4lunch.MainApplication
import com.cleanup.go4lunch.utils.Constants.Companion.POI_ENTITY
import com.cleanup.go4lunch.utils.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PoiRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    // mock PoiRepository constructor parameters for getPoiRepository() fun bellow
    private val applicationMock = mockk<MainApplication>()
    private val poiDaoMock = mockk<PoiDao>()
    private val poiRetrofitMock = mockk<PoiRetrofit>()

    @Before
    fun setUp() {
        coJustRun { poiDaoMock.nukePOIS() }
        coEvery { poiDaoMock.getPoiById(any()) }.returns(POI_ENTITY)
        coEvery { poiDaoMock.getPoiEntities() }.returns(flowOf(emptyList()))  // Todo Nino: coEvery, ici?
    }

    @Test
    fun getCachedPOIsListFlow() {
    }

    @Test
    fun getPoiById() = testCoroutineRule.runBlockingTest {  // todo Nino: Blocking, vraiment?
        // given
        val repository = getPoiRepository()

        // when
        val result =
            repository.getPoiById(POI_ENTITY.id)  // todo Nino, la je suis border pour l'ID?

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
    fun clearCache() {
        // given
        val repository = getPoiRepository()

        // when
        coEvery { repository.clearCache() }

        // then
        coVerify(exactly = 1) { poiDaoMock.nukePOIS() }
    }


    private fun getPoiRepository() = PoiRepository(
        application = applicationMock,
        poiRetrofit = poiRetrofitMock,
        poiDao = poiDaoMock
    )


}