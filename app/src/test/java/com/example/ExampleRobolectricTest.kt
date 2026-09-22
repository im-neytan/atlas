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

  @Test
  fun `test 1GB corresponds to 1024MB and operator response truncated to 70 chars`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.network.ServidorManager.getInstance(context)

    // Verifica que 1GB equivale a 1024MB e 2GB a 2048MB
    org.junit.Assert.assertEquals("1024", manager.extrairMegasPuro("1GB"))
    org.junit.Assert.assertEquals("1024", manager.extrairMegasPuro("1 GB"))
    org.junit.Assert.assertEquals("2048", manager.extrairMegasPuro("2GB"))
    org.junit.Assert.assertEquals("500", manager.extrairMegasPuro("500 MB"))

    // Verifica que a resposta da operadora nunca ultrapassa 70 caracteres
    val respostaLonga = "Transferiste com sucesso 1024MB para 841234567. O teu saldo de megas atual e de 5000MB validos ate 30 dias. Obrigado pela preferencia Vodacom."
    val respostaLimpa = manager.limparRespostaOperadora(respostaLonga)
    org.junit.Assert.assertTrue(respostaLimpa.length <= 70)
  }
}
