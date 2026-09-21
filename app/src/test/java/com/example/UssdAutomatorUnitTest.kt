package com.example

import com.example.network.ServidorManager
import com.example.service.UssdAccessibilityService
import org.junit.Assert.assertEquals
import org.junit.Test

class UssdAutomatorUnitTest {

    @Test
    fun testUssdSequenceStepsEnum() {
        // Verifica se os passos essenciais do fluxo interativo estão modelados corretamente
        val steps = UssdAccessibilityService.Step.values()
        assertEquals(true, steps.contains(UssdAccessibilityService.Step.WAITING_STEP_8))
        assertEquals(true, steps.contains(UssdAccessibilityService.Step.WAITING_STEP_2))
        assertEquals(true, steps.contains(UssdAccessibilityService.Step.WAITING_STEP_MEGAS))
        assertEquals(true, steps.contains(UssdAccessibilityService.Step.WAITING_STEP_NUMERO))
        assertEquals(true, steps.contains(UssdAccessibilityService.Step.WAITING_FINAL_RESPONSE))
    }

    @Test
    fun testMegasExtractionFormat() {
        // Extração de megas limpos
        val raw1 = "500 MB"
        val digits1 = raw1.replace(Regex("[^0-9]"), "").trim()
        assertEquals("500", digits1)

        val raw2 = "1000"
        val digits2 = raw2.replace(Regex("[^0-9]"), "").trim()
        assertEquals("1000", digits2)
    }

    @Test
    fun testVodacomNumberFormatting() {
        // Limpeza de número Vodacom MZ
        var num = "+258 84 123 4567".replace(Regex("[^0-9]"), "").trim()
        if (num.startsWith("258") && num.length > 9) {
            num = num.removePrefix("258")
        }
        assertEquals("841234567", num)
    }

    @Test
    fun testStepCaptureStructureAndFlowReset() {
        // Inicializa o fluxo e valida que o histórico reseta limpo e pronto para registrar respostas
        UssdAccessibilityService.iniciarFluxoPreciso(megas = "500", numero = "841234567")
        assertEquals(UssdAccessibilityService.Step.WAITING_STEP_8, UssdAccessibilityService.currentActiveStep.value)
        assertEquals(0, UssdAccessibilityService.getRespostasPorEtapa().size)
        assertEquals("", UssdAccessibilityService.getUltimaRespostaOperadora())

        UssdAccessibilityService.cancelarFluxo()
        assertEquals(UssdAccessibilityService.Step.IDLE, UssdAccessibilityService.currentActiveStep.value)
    }
}
