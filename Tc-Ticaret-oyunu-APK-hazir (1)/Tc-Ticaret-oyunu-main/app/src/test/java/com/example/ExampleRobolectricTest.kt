package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HoldingRepository
import com.example.model.PricingStrategy
import com.example.model.Sector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var repository: HoldingRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("holding_sim_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        repository = HoldingRepository(context)
        repository.resetGame("Test Holding")
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Holding TR", appName)
    }

    @Test
    fun `test initial state starting capital is 50000 TL`() {
        val state = repository.gameState.value
        assertEquals(50_000.0, state.cash, 0.001)
        assertEquals(0, state.companies.size)
        assertEquals(1, state.economy.day)
    }

    @Test
    fun `test found first market company`() {
        val success = repository.foundCompany("İlk Süpermarketim", Sector.MARKET)
        assertTrue(success)

        val state = repository.gameState.value
        assertEquals(0.0, state.cash, 0.001)
        assertEquals(1, state.companies.size)
        assertEquals(Sector.MARKET, state.companies[0].sector)
    }

    @Test
    fun `test advance day generates profit and increments day`() {
        repository.foundCompany("İlk Süpermarketim", Sector.MARKET)
        repository.advanceDay()

        val state = repository.gameState.value
        assertEquals(2, state.economy.day)
        assertTrue(state.cash > 0.0) // Earned daily profit
    }

    @Test
    fun `test stock trading buy and sell`() {
        // Give cash for test
        repository.depositToBank(0.0)
        // Advance with company to accumulate cash
        repository.foundCompany("Test Market", Sector.MARKET)
        for (i in 1..30) {
            repository.advanceDay()
        }

        val initialCash = repository.gameState.value.cash
        assertTrue(initialCash > 20_000.0)

        // Buy 10 THYIL shares
        val stock = repository.gameState.value.stocks.first { it.ticker == "THYIL" }
        val buyCost = stock.currentPrice * 10
        val buySuccess = repository.buyStock("THYIL", 10)
        assertTrue(buySuccess)

        var updatedStock = repository.gameState.value.stocks.first { it.ticker == "THYIL" }
        assertEquals(10, updatedStock.ownedShares)
        assertEquals(initialCash - buyCost, repository.gameState.value.cash, 0.01)

        // Sell 5 shares
        val sellSuccess = repository.sellStock("THYIL", 5)
        assertTrue(sellSuccess)
        updatedStock = repository.gameState.value.stocks.first { it.ticker == "THYIL" }
        assertEquals(5, updatedStock.ownedShares)
    }

    @Test
    fun `test real time live price ticking and sector stocks presence`() {
        val stocks = repository.gameState.value.stocks
        // Verify all 8 sectors + ETF have representative stocks
        assertTrue(stocks.size >= 16)
        assertTrue(stocks.any { it.sector == Sector.MARKET })
        assertTrue(stocks.any { it.sector == Sector.RESTAURANT })
        assertTrue(stocks.any { it.sector == Sector.LOGISTICS })
        assertTrue(stocks.any { it.sector == Sector.MANUFACTURING })
        assertTrue(stocks.any { it.sector == Sector.CONSTRUCTION })
        assertTrue(stocks.any { it.sector == Sector.TECHNOLOGY })
        assertTrue(stocks.any { it.sector == Sector.TOURISM })
        assertTrue(stocks.any { it.sector == Sector.FINANCE })

        val initialBimPrice = stocks.first { it.ticker == "BIMAS" }.currentPrice

        // Tick real-time prices multiple times
        repository.tickLiveMarketPrices()
        repository.tickLiveMarketPrices()

        val updatedStocks = repository.gameState.value.stocks
        val updatedBim = updatedStocks.first { it.ticker == "BIMAS" }
        assertTrue(updatedBim.liveTicks.size >= 2)
        assertTrue(updatedBim.volume >= 3_000_000L)
    }
}
