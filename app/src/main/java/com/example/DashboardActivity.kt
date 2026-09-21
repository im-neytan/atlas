package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.ConnectionStatus
import com.example.ui.DashboardViewModel
import com.example.ui.components.AgendarTransferenciaBottomSheet
import com.example.ui.components.DefinicoesBottomSheet
import com.example.ui.screens.ControleSimsScreen
import com.example.ui.screens.FilaScreen
import com.example.ui.screens.HistoricoScreen
import com.example.ui.screens.OverviewDashboardScreen
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckBorder
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val pedidos by viewModel.pedidosFila.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalFilaCount.collectAsStateWithLifecycle()
    val aguardandoCount by viewModel.aguardandoCount.collectAsStateWithLifecycle()
    val emProcessamentoCount by viewModel.emProcessamentoCount.collectAsStateWithLifecycle()

    val historicoList by viewModel.historicoList.collectAsStateWithLifecycle()
    val concluidosCount = historicoList.count { it.status == "CONCLUIDO" }
    val falhasCount = historicoList.count { it.status == "FALHA" }
    val ultimoHistorico = historicoList.firstOrNull()

    val activeSim by viewModel.activeSim.collectAsStateWithLifecycle()
    val sim1Info by viewModel.sim1Info.collectAsStateWithLifecycle()
    val sim2Info by viewModel.sim2Info.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val lastLogMessage by viewModel.lastLogMessage.collectAsStateWithLifecycle()

    val isScheduleModalOpen by viewModel.isScheduleModalOpen.collectAsStateWithLifecycle()
    val isDefinicoesModalOpen by viewModel.isDefinicoesModalOpen.collectAsStateWithLifecycle()
    val isEngineRunning by viewModel.isEngineRunning.collectAsStateWithLifecycle()
    val currentCountdown by viewModel.currentCountdown.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavigationTabItem(
            title = "Dashboard",
            selectedIcon = Icons.Filled.SpaceDashboard,
            unselectedIcon = Icons.Outlined.SpaceDashboard,
            badgeCount = null
        ),
        NavigationTabItem(
            title = "Fila",
            selectedIcon = Icons.AutoMirrored.Filled.ViewList,
            unselectedIcon = Icons.AutoMirrored.Outlined.ViewList,
            badgeCount = if (aguardandoCount > 0) aguardandoCount else null
        ),
        NavigationTabItem(
            title = "Histórico",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History,
            badgeCount = if (historicoList.isNotEmpty()) historicoList.size else null
        ),
        NavigationTabItem(
            title = "SIMs",
            selectedIcon = Icons.Filled.SimCard,
            unselectedIcon = Icons.Outlined.SimCard,
            badgeCount = null
        )
    )

    Scaffold(
        containerColor = Bl4ckBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BL4CK SYSTEM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.2.sp,
                        color = Bl4ckTextPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openDefinicoesModal() },
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Definições",
                            tint = Bl4ckTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Bl4ckSurface
                )
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = Bl4ckBorderSubtle,
                    thickness = 1.dp
                )
                NavigationBar(
                    containerColor = Bl4ckSurface,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = pagerState.currentPage == index
                        val currentIcon = if (isSelected) item.selectedIcon else item.unselectedIcon
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                if (item.badgeCount != null) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = Bl4ckPrimary,
                                                contentColor = Color(0xFF0F172A)
                                            ) {
                                                Text(
                                                    text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = currentIcon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(23.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = currentIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    letterSpacing = 0.2.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Bl4ckPrimary,
                                selectedTextColor = Bl4ckPrimary,
                                indicatorColor = Bl4ckPrimary.copy(alpha = 0.12f),
                                unselectedIconColor = Bl4ckTextMuted,
                                unselectedTextColor = Bl4ckTextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OverviewDashboardScreen(
                        totalFila = totalCount,
                        aguardandoCount = aguardandoCount,
                        emProcessamentoCount = emProcessamentoCount,
                        concluidosCount = concluidosCount,
                        falhasCount = falhasCount,
                        activeSim = activeSim,
                        sim1Info = sim1Info,
                        sim2Info = sim2Info,
                        isEngineRunning = isEngineRunning,
                        currentCountdown = currentCountdown,
                        connectionStatus = connectionStatus,
                        ultimoHistorico = ultimoHistorico,
                        onAgendarClick = { viewModel.openScheduleModal() },
                        onAlternarMotor = { viewModel.alternarMotorFila() },
                        onProcessarProximo = { viewModel.processarProximo() },
                        onVerFilaClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onVerHistoricoClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(2) }
                        },
                        onAlternarSim = { viewModel.alternarSim() }
                    )
                    1 -> FilaScreen(
                        pedidos = pedidos,
                        totalCount = totalCount,
                        aguardandoCount = aguardandoCount,
                        emProcessamentoCount = emProcessamentoCount,
                        isEngineRunning = isEngineRunning,
                        currentCountdown = currentCountdown,
                        onAgendarClick = { viewModel.openScheduleModal() },
                        onAlternarMotor = { viewModel.alternarMotorFila() },
                        onProcessarProximo = { viewModel.processarProximo() },
                        onLimparFila = { viewModel.limparFila() },
                        onExcluirPedido = { viewModel.excluirPedido(it) }
                    )
                    2 -> HistoricoScreen(
                        historicoList = historicoList,
                        onLimparHistorico = { viewModel.limparHistorico() }
                    )
                    3 -> ControleSimsScreen(
                        activeSim = activeSim,
                        sim1Info = sim1Info,
                        sim2Info = sim2Info,
                        connectionStatus = connectionStatus,
                        serverUrl = serverUrl,
                        onAlternarSim = { viewModel.alternarSim() },
                        onConectarWebSocket = { viewModel.conectarWebSocket(it) },
                        onAlternarConexao = { viewModel.alternarConexao() },
                        onSimularComando = { viewModel.simularComandoRemoto(it) },
                        onAtualizarLimiteManual = { slot, lim, rest ->
                            viewModel.atualizarLimite(slot, lim, rest)
                        }
                    )
                }
            }
        }
    }

    if (isScheduleModalOpen) {
        AgendarTransferenciaBottomSheet(
            sheetState = sheetState,
            onDismiss = { viewModel.closeScheduleModal() },
            onConfirm = { numero, megas ->
                viewModel.agendarTransferencia(numero, megas)
            }
        )
    }

    if (isDefinicoesModalOpen) {
        DefinicoesBottomSheet(
            onDismissRequest = { viewModel.closeDefinicoesModal() }
        )
    }
}

private data class NavigationTabItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int?
)
