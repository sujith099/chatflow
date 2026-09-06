package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.ChatViewModel
import com.example.data.Message
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val conversations by viewModel.conversations.collectAsState()
    val activeConvId by viewModel.activeConversationId.collectAsState()
    val messagesMap by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val aiSummary by viewModel.aiSummary.collectAsState()
    val smartReplies by viewModel.smartReplies.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    val conversation = conversations.find { it.conversationId == activeConvId }
    val messages = messagesMap[activeConvId].orEmpty()

    var textInput by remember { mutableStateOf("") }
    var showAiPanel by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var replyMessageId by remember { mutableStateOf<Message?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        if (conversation?.photoURL?.isNotEmpty() == true) {
                            AsyncImage(
                                model = conversation.photoURL,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(conversation?.name?.take(1) ?: "C", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(conversation?.name ?: "Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Online", fontSize = 12.sp, color = Color(0xFF4CAF50))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showAiPanel = !showAiPanel
                        if (showAiPanel) viewModel.summarizeActiveChat()
                    }) {
                        Icon(Icons.Default.SmartToy, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { /* Call feature placeholder */ }) {
                        Icon(Icons.Default.Phone, contentDescription = "Call")
                    }
                    IconButton(onClick = { /* Video call placeholder */ }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                // AI Smart Replies
                if (smartReplies.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(smartReplies) { reply ->
                            AssistChip(
                                onClick = {
                                    viewModel.sendMessage(reply)
                                },
                                label = { Text(reply, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                // Reply banner if replying
                if (replyMessageId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Replying to ${replyMessageId?.senderName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(replyMessageId?.text ?: "", fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { replyMessageId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Reply", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Composer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttachMenu = !showAttachMenu }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                if (editingMessageId != null) {
                                    viewModel.editMessage(editingMessageId!!, textInput)
                                    editingMessageId = null
                                } else {
                                    viewModel.sendMessage(
                                        text = textInput,
                                        replyTo = replyMessageId?.messageId,
                                        replyText = replyMessageId?.text
                                    )
                                    replyMessageId = null
                                }
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ChatWallpaper(modifier = Modifier.fillMaxSize())
            Column(modifier = Modifier.fillMaxSize()) {
                // AI Assistant Panel
                if (showAiPanel) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gemini AI Summary", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                IconButton(onClick = { showAiPanel = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isAiLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            } else {
                                Text(aiSummary.ifEmpty { "Click summarize to analyze conversation." }, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Message List
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Start the conversation 👋", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                isMe = message.senderId == currentUser?.uid,
                                onReply = { replyMessageId = message },
                                onEdit = {
                                    editingMessageId = message.messageId
                                    textInput = message.text
                                },
                                onDelete = { forEveryone -> viewModel.deleteMessage(message.messageId, forEveryone) },
                                onReact = { emoji -> viewModel.addReaction(message.messageId, emoji) },
                                onPin = { viewModel.pinMessage(message.messageId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Boolean) -> Unit,
    onReact: (String) -> Unit,
    onPin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Card(
                modifier = Modifier.clickable { showMenu = !showMenu },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isMe) {
                        Text(
                            text = message.senderName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Reply reference if any
                    if (message.replyText != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Text(message.replyText, fontSize = 11.sp, maxLines = 1, color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (message.type == "image" && message.fileURL.isNotEmpty()) {
                        AsyncImage(
                            model = message.fileURL,
                            contentDescription = "Image",
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else if (message.type == "file") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(message.fileName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.text,
                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.edited) {
                            Text("edited ", fontSize = 10.sp, color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                        Text(
                            text = "10:42 AM",
                            fontSize = 10.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓✓", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Reactions display
            if (message.reactions.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    message.reactions.values.toSet().forEach { emoji ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Reply") }, onClick = { showMenu = false; onReply() }, leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null) })
                DropdownMenuItem(text = { Text("Pin Message") }, onClick = { showMenu = false; onPin() }, leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) })
                DropdownMenuItem(text = { Text("React 👍") }, onClick = { showMenu = false; onReact("👍") })
                DropdownMenuItem(text = { Text("React ❤️") }, onClick = { showMenu = false; onReact("❤️") })
                DropdownMenuItem(text = { Text("React 😂") }, onClick = { showMenu = false; onReact("😂") })
                if (isMe) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) })
                    DropdownMenuItem(text = { Text("Delete for Everyone") }, onClick = { showMenu = false; onDelete(true) }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) })
                }
                DropdownMenuItem(text = { Text("Delete for Me") }, onClick = { showMenu = false; onDelete(false) }, leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) })
            }
        }
    }
}

@Composable
fun ChatWallpaper(modifier: Modifier = Modifier) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    Box(modifier = modifier.background(surfaceVariant)) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_chat_wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alpha = 0.15f
        )
    }
}
