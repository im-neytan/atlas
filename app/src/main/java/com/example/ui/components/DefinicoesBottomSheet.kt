package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.Bl4ckBorderSubtle
import com.example.ui.theme.Bl4ckPrimary
import com.example.ui.theme.Bl4ckSecondary
import com.example.ui.theme.Bl4ckSurface
import com.example.ui.theme.Bl4ckSurfaceVariant
import com.example.ui.theme.Bl4ckTextMuted
import com.example.ui.theme.Bl4ckTextPrimary
import com.example.ui.theme.Bl4ckTextSecondary
import com.example.ui.theme.Bl4ckWarning
import com.example.util.AppSettingsManager
import com.example.util.SystemServicesHelper
import com.example.util.TtsPronunciadorHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinicoesBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = AppSettingsManager.getInstance(context)
    val ttsHelper = TtsPronunciadorHelper.getInstance(context)

    // Estados de Permissões
    var isAccessibilityActive by remember { mutableStateOf(false) }
    var isCallGranted by remember { mutableStateOf(false) }
    var isPhoneStateGranted by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        isAccessibilityActive = SystemServicesHelper.isAccessibilityServiceEnabled(context)
        isCallGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        isPhoneStateGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val phoneStateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    val notificacoesGerais by settingsManager.notificacoesGerais.collectAsState()
    val notificarReceberPedido by settingsManager.notificarReceberPedido.collectAsState()
    val notificarIniciarTransferencia by settingsManager.notificarIniciarTransferencia.collectAsState()
    val notificarFinalizarTransferencia by settingsManager.notificarFinalizarTransferencia.collectAsState()
    val notificarErro by settingsManager.notificarErro.collectAsState()
    val pronunciamentoInicial by settingsManager.pronunciamentoInicial.collectAsState()
    val pronunciamentoFinal by settingsManager.pronunciamentoFinal.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val allPermissionsGranted = isAccessibilityActive && isCallGranted && isPhoneStateGranted

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Bl4ckSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho da Folha de Definições
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF072B1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Definições",
                            tint = Bl4ckPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Definições do Sistema",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Bl4ckTextPrimary
                        )
                        Text(
                            text = "Notificações, Permissões & Áudio",
                            fontSize = 12.sp,
                            color = Bl4ckTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Bl4ckSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Bl4ckTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // SEÇÃO 1: PERMISSÕES DO SISTEMA (DESAPARECEM AO SEREM ATIVADAS)
            // ==========================================
            Text(
                text = "PERMISSÕES DO SISTEMA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Bl4ckPrimary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (allPermissionsGranted) {
                // Quando todas estão ativas, exibe badge limpa e elegante
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF06281B))
                        .border(1.dp, Bl4ckPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Bl4ckPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Todas as Permissões Ativas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckTextPrimary
                            )
                            Text(
                                text = "Acessibilidade, chamadas telefônicas e leitura de SIM liberados",
                                fontSize = 11.sp,
                                color = Bl4ckTextMuted
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Permissão 1: Serviço de Acessibilidade (Leitor USSD) - Só aparece se não permitida!
                    if (!isAccessibilityActive) {
                        PermissaoItemCard(
                            titulo = "Serviço de Leitura USSD",
                            descricao = "Necessário para ler diálogos e fechar pop-ups da operadora",
                            botaoTexto = "Ativar",
                            icone = Icons.Default.AccessibilityNew,
                            onClick = {
                                SystemServicesHelper.openAccessibilitySettings(context)
                            }
                        )
                    }

                    // Permissão 2: Chamada Telefônica (*162#) - Só aparece se não permitida!
                    if (!isCallGranted) {
                        PermissaoItemCard(
                            titulo = "Fazer Chamadas Diretas",
                            descricao = "Necessário para discar o código *162# automaticamente",
                            botaoTexto = "Permitir",
                            icone = Icons.Default.Call,
                            onClick = {
                                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                            }
                        )
                    }

                    // Permissão 3: Leitura de SIM Cards & Redes - Só aparece se não permitida!
                    if (!isPhoneStateGranted) {
                        PermissaoItemCard(
                            titulo = "Detecção de SIM Cards & Rede",
                            descricao = "Necessário para identificar chips e operadoras ativas",
                            botaoTexto = "Permitir",
                            icone = Icons.Default.PhoneAndroid,
                            onClick = {
                                phoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SEÇÃO 2: NOTIFICAÇÕES
            // ==========================================
            Text(
                text = "DEFINIÇÕES DE NOTIFICAÇÕES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Bl4ckPrimary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Card Switch Geral de Notificações
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bl4ckSurfaceVariant)
                    .border(1.dp, if (notificacoesGerais) Bl4ckPrimary.copy(alpha = 0.5f) else Bl4ckBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (notificacoesGerais) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (notificacoesGerais) Bl4ckPrimary else Bl4ckTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ativar Notificações Geral",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckTextPrimary
                            )
                            Text(
                                text = "Habilita alertas no sistema e notificações operacionais",
                                fontSize = 11.sp,
                                color = Bl4ckTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = notificacoesGerais,
                        onCheckedChange = { settingsManager.setNotificacoesGerais(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Bl4ckPrimary,
                            uncheckedThumbColor = Bl4ckTextMuted,
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            // Sub-opções de Notificações
            if (notificacoesGerais) {
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Bl4ckSurfaceVariant)
                        .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Column {
                        DefinicaoItemRow(
                            titulo = "Ao receber pedido",
                            descricao = "Alerta quando um novo pedido entra na fila",
                            checked = notificarReceberPedido,
                            onCheckedChange = { settingsManager.setNotificarReceberPedido(it) }
                        )

                        HorizontalDivider(color = Bl4ckBorderSubtle)

                        DefinicaoItemRow(
                            titulo = "Ao iniciar transferência",
                            descricao = "Alerta no momento do disparo do código *162#",
                            checked = notificarIniciarTransferencia,
                            onCheckedChange = { settingsManager.setNotificarIniciarTransferencia(it) }
                        )

                        HorizontalDivider(color = Bl4ckBorderSubtle)

                        DefinicaoItemRow(
                            titulo = "Ao finalizar transferência",
                            descricao = "Alerta o resultado final e resposta da operadora",
                            checked = notificarFinalizarTransferencia,
                            onCheckedChange = { settingsManager.setNotificarFinalizarTransferencia(it) }
                        )

                        HorizontalDivider(color = Bl4ckBorderSubtle)

                        DefinicaoItemRow(
                            titulo = "Em caso de erro",
                            descricao = "Alerta falhas no SIM, desconexão ou limites",
                            checked = notificarErro,
                            onCheckedChange = { settingsManager.setNotificarErro(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SEÇÃO 3: PRONUNCIAMENTO POR VOZ
            // ==========================================
            Text(
                text = "PRONUNCIAMENTO POR VOZ (NARRADOR)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Bl4ckPrimary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Pronunciamento Inicial
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bl4ckSurfaceVariant)
                    .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (pronunciamentoInicial) Bl4ckPrimary else Bl4ckTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ativar pronunciamento inicial",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckTextPrimary
                            )
                            Text(
                                text = "Anuncia a transferência ao iniciar a operação",
                                fontSize = 11.sp,
                                color = Bl4ckTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = pronunciamentoInicial,
                        onCheckedChange = { settingsManager.setPronunciamentoInicial(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Bl4ckPrimary,
                            uncheckedThumbColor = Bl4ckTextMuted,
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pronunciamento Final
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Bl4ckSurfaceVariant)
                    .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (pronunciamentoFinal) Bl4ckPrimary else Bl4ckTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ativar pronunciamento final",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Bl4ckTextPrimary
                            )
                            Text(
                                text = "Anuncia a conclusão ao finalizar a operação",
                                fontSize = 11.sp,
                                color = Bl4ckTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = pronunciamentoFinal,
                        onCheckedChange = { settingsManager.setPronunciamentoFinal(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Bl4ckPrimary,
                            uncheckedThumbColor = Bl4ckTextMuted,
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de Testar Pronunciamento de Voz
            OutlinedButton(
                onClick = {
                    ttsHelper.narrar("Transferência de quinhentos megas concluída com sucesso.")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Bl4ckPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ouvir Exemplo de Pronunciamento",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // SEÇÃO 4: SOBRE A BL4CK_SOLUTIONS
            // ==========================================
            SobreCorporacaoSection()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Bl4ckPrimary,
                    contentColor = Color(0xFF051C13)
                )
            ) {
                Text(
                    text = "Salvar e Fechar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SobreCorporacaoSection() {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Bl4ckPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "SOBRE A BL4CK_SOLUTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckPrimary,
                    letterSpacing = 0.8.sp
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                tint = Bl4ckTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Bl4ckSurfaceVariant)
                    .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header com Logo Oficial e Identidade Corporativa
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo BL4CK_SOLUTIONS",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, Bl4ckPrimary.copy(alpha = 0.5f), CircleShape)
                        )

                        Column {
                            Text(
                                text = "BL4CK_SOLUTIONS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Bl4ckTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Tecnologia & Soluções Digitais",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Bl4ckSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Bl4ckBorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Textos Oficiais
                    Text(
                        text = "A BL4CK_SOLUTIONS é uma corporação de tecnologia e soluções digitais, fundada em 2026 e sediada em Maputo, Moçambique.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Somos especializados em desenvolvimento de software, automação de processos, plataformas de streaming e soluções digitais sob medida.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Nosso compromisso é fornecer tecnologia de qualidade, com segurança, desempenho e suporte dedicado.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metadados Corporativos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF09121B))
                            .border(1.dp, Bl4ckBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Bl4ckPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Contacto: info@bl4cksolutions.com",
                                    fontSize = 11.sp,
                                    color = Bl4ckTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Bl4ckPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Localização: Maputo, Moçambique",
                                    fontSize = 11.sp,
                                    color = Bl4ckTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Bl4ckPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Fundação: 2026",
                                    fontSize = 11.sp,
                                    color = Bl4ckTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Bl4ckPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Versão do App: 1.0 (Build 1)",
                                    fontSize = 11.sp,
                                    color = Bl4ckTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Copyright oficial formatado
                    Text(
                        text = "© 2026 BL4CK_SOLUTIONS. Todos os direitos reservados.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Bl4ckTextMuted,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissaoItemCard(
    titulo: String,
    descricao: String,
    botaoTexto: String,
    icone: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Bl4ckSurfaceVariant)
            .border(1.dp, Bl4ckWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Bl4ckWarning,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Bl4ckTextPrimary
                )
                Text(
                    text = descricao,
                    fontSize = 11.sp,
                    color = Bl4ckTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Bl4ckWarning,
                contentColor = Color(0xFF0F172A)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = botaoTexto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DefinicaoItemRow(
    titulo: String,
    descricao: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Bl4ckTextPrimary
            )
            Text(
                text = descricao,
                fontSize = 11.sp,
                color = Bl4ckTextMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Bl4ckPrimary,
                uncheckedThumbColor = Bl4ckTextMuted,
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
