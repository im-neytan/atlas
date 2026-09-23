package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.Bl4ckBackground
import com.example.ui.theme.Bl4ckDockActiveIcon
import com.example.ui.theme.Bl4ckDockActivePill
import com.example.ui.theme.Bl4ckDockBackground
import com.example.ui.theme.Bl4ckDockBorder
import com.example.ui.theme.Bl4ckDockInactiveIcon
import com.example.ui.theme.Bl4ckError
import com.example.ui.theme.Bl4ckGlassBorder
import com.example.ui.theme.Bl4ckGlassSurfaceLight
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.Bl4ckWarning
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.atualizarSims()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicita as permissões telefônicas fundamentais para leitura multi-SIM e discagem USSD
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_NUMBERS
            )
        )

        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(viewModel = viewModel)

                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    }
                }
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
    val simCards by viewModel.simCards.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()

    val isScheduleModalOpen by viewModel.isScheduleModalOpen.collectAsStateWithLifecycle()
    val isDefinicoesModalOpen by viewModel.isDefinicoesModalOpen.collectAsStateWithLifecycle()
    val isEngineRunning by viewModel.isEngineRunning.collectAsStateWithLifecycle()
    val currentCountdown by viewModel.currentCountdown.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavigationTabItem(
            title = "Início",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
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
            // Header Moderno 2026 inspirado na referência visual IMG_8485.png
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo com Anel Orbital e Brilho Futurista
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0C1018))
                            .border(
                                1.5.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0x3310B981),
                                        Color(0xFF818CF8),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo Oficial",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Bem-vindo,",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Bl4ckTextSecondary,
                            letterSpacing = 0.2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BL4CK SYSTEM",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Bl4ckTextPrimary,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = " \uD83D\uDC4B",
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }

                // Ícone de Notificações / Definições com Indicador de Status
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Bl4ckGlassSurfaceLight)
                        .border(1.dp, Bl4ckGlassBorder, CircleShape)
                        .clickable { viewModel.openDefinicoesModal() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Definições e Status",
                        tint = Bl4ckTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    // Ponto indicador de alerta/conexão
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .clip(CircleShape)
                            .background(
                                when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> Bl4ckPrimary
                                    ConnectionStatus.CONNECTING -> Bl4ckWarning
                                    ConnectionStatus.DISCONNECTED -> Bl4ckError
                                }
                            )
                    )
                }
            }
        },
        bottomBar = {
            // Floating Dock translúcido estilo 2026 inspirado na referência visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Bl4ckDockBackground,
                    shape = RoundedCornerShape(34.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Bl4ckDockBorder),
                    shadowElevation = 18.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = pagerState.currentPage == index
                            if (isSelected) {
                                // Item Ativo: Cápsula branca arredondada com ícone escuro
                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Bl4ckDockActivePill)
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                        .padding(horizontal = 20.dp)
                                        .testTag("nav_tab_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.selectedIcon,
                                        contentDescription = item.title,
                                        tint = Bl4ckDockActiveIcon,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                // Item Inativo: Ícone minimalista com área de toque ampla
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                        .testTag("nav_tab_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.badgeCount != null) {
                                        BadgedBox(
                                            badge = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(Bl4ckPrimary)
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = item.unselectedIcon,
                                                contentDescription = item.title,
                                                tint = Bl4ckDockInactiveIcon,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = item.unselectedIcon,
                                            contentDescription = item.title,
                                            tint = Bl4ckDockInactiveIcon,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                        onAlternarParaSim = { slot -> viewModel.alternarParaSim(slot) },
                        onAbrirConfiguracoesSistema = { viewModel.abrirConfiguracoesSistemaSim() },
                        onConectarWebSocket = { viewModel.conectarWebSocket(it) },
                        onAlternarConexao = { viewModel.alternarConexao() },
                        onSimularComando = { viewModel.simularComandoRemoto(it) },
                        onAtualizarLimiteManual = { slot, lim, rest ->
                            viewModel.atualizarLimite(slot, lim, rest)
                        },
                        simCards = simCards,
                        onAtualizarSims = { viewModel.servidorManager.atualizarInformacoesSims() },
                        onSalvarNumeroSim = { slot, num -> viewModel.salvarNumeroSim(slot, num) }
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
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int?
)

