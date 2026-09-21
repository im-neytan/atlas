package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BL4CK SYSTEM", appName)
  }

  @Test
  fun `test ServidorManager queue insertion and sim toggle`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.network.ServidorManager.getInstance(context)
    
    val initialSim = manager.activeSim.value
    manager.alternarSimAtivo()
    val toggledSim = manager.activeSim.value
    org.junit.Assert.assertNotEquals(initialSim, toggledSim)

    val db = com.example.data.db.Bl4ckDatabase.getInstance(context)
    db.pedidoFilaDao().insert(
        com.example.data.model.PedidoFila(
            displayId = "#BLK-9999",
            numeroDestino = "+244923111222",
            megas = "500 MB",
            status = "AGUARDANDO"
        )
    )
    val list = db.pedidoFilaDao().getAll()
    org.junit.Assert.assertTrue(list.isNotEmpty())
  }
}
