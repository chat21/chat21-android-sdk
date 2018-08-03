# CHANGELOG

## v.: 1.0.11 b.: 39
- close group - ArchivedConversationListFragment
- delete conversation - ArchivedConversationListFragment
- added /utils/httpmanager
- toggle conversation read status
- added archived conversations activity 

## v.: 1.0.10 b.: 38
- fixed remove conversation - ConversationsHandler, ConversationsListener, BSArchivedConversationsListFragmentLongPress, ArchivedConversationListFragment
- fixed auth expiring issue - ChatSplashActivity, ChatAuthentication

## v.: 1.0.9 b.: 37
- designed method to open chat with group directly - ChatUI
- changed saveOrUpdateGroupInMemory method's scope - GroupsSyncronizer
- designed system's message - SystemViewHolder, MessageListAdapter, row_system, ConversationMessagesHandler, Message

## v.: 1.0.8.1 b.: 36
- fixed item divider for chat group list- ChatGroupsListFragment, ItemDecoration
- fixed item divider for chat group member list- GroupAdminPanelActivity, ItemDecoration
- fixed crash on contact search - ContactListAdapter, ContactsListFragment

## v.: 1.0.8 b.: 35
- fixed groups members

## v.: 1.0.7 b.: 34
- fixed findById user with concurrent arraylist - ContactsSynchronizer

## v.: 1.0.6 b.: 33
- updated attachments metadata
- update firebase dependencies resolution (from 11.6.0 to 11.8.0)
- multi device instance support
- fixed retrieving user - PublicProfileActivity, MessageListActivity

## v.: 1.0.5 b.: 32
- show user id if username is not available
- fixed autologin after signup - ChatLoginActivity
- fixed finish activity on group creation - AddMemberToChatGroupActivity
- fixed members label visibility issue - GroupPanelAdminActivity
- fixed null label when create a new group - ConversationListAdapter
- fixed input panel visibility when group is created - ArchivedConversationListFragment

## v.: 1.0.4 b.: 31
- fixed contact syncronizer

## v.: 1.0.3 b.: 30
- designed login with Email and Password within ChatManager
- fixed logout
- fixed empty conversation list label position
- modularized contacts fragment
- modularized chat groups fragment

## v.: 1.0.2 b.: 29
- fixed conversation list item decorator
- changed max lines within row_conversation from 2 to 1

## v.: 1.0.1 b.: 28
- improved layouts
- fixed dimens
- conversations item decorator
- contacts item decorator
- groups item decorator
- group members item decorator
- fixed timestamp for conversations
- fixed timestamp for messages
- fixed timestamp for last online

## v.: 1.0 b.: 27
- full refactoring

## 0.10.2
- rimosso supporto MultiDex - 59f1003
- fix Glide context - 13a13c9
- fix padding message row_recipient.xml - 69f1b98
- fix padding message row_sender.xml - 69f1b98
- fix toolbar ghost click in MessageListActivity.class - 833e54b
- fix conflitto multi-dipendenze in /chat/build.gradle - 59f1003
- fix toolbar flickering in MessageListActivity.class - bb83101

## 0.10.1
- rimosso supporto vectorDrawables (crash placeholder glide su pre-lollipop)

## 0.10
- cambiato package da it.smart21.android.xxx a chat21.android.xxx
- corretto errore conversWith
- corretto sender layout timestamp
- rinominato Chat in  /core/ChatManager
- Chat.Authentication è stato spostato in /core/ChatAuthentication
- Chat.Configuration è stato spostato in  /core/ChatConfiguration
- semplificato ChatConfiguration
- semplificato ChatManager

## 0.9
- aggiunto layout giorno corrente come whatsapp
- migliorato layout/row_sender 
- migliorato layout/row_recipient
- nuova modalità di inizializzazione
- bugfix crash per lista contatti nulla
- supporto al multidex application integrato in Chat
- aggiunto layout nessun contatto in ContactListActivity
- aggiunto layout nessun contatto in AddMembersActivity


## 0.8.3
- bugfix conversation id sbagliato
- bugfix menu crea gruppo
- integrato pannello emoji direttamente in chat

## 0.8.2
- aggiunto timestamp a users in fase di login
- bugfix crash per immagini remote di google foto

## 0.8.1
- bugfix recipient metadata non esistente
- bugfix crash tablet ( IllegalStateException: Can not perform this action after onSaveInstanceState)
- firebase disk persistence
- aggiunta classe per scegliere un gruppo
- bugfix lettura conversazione (se il mittente è l'utente loggato viene aggiornata direttamente)
- bugfix strings - traduzioni

## 0.8
- cancellazione conversazione
- bottom sheet allegati (immagini e documenti)
- progress per carimento conversazioni
- progress per carimento i miei gruppi

## 0.7
- aggiornate dipendenze google
- bugfix messageListActivity displayName
- bugfix conversationListAdapter displayName
- bugfix aggiornamento stato in conversazione
- bugfix timestamp upload
- bugfix public profile displayName
- bugfix groupMembersListAdapter displayName
- progettato pulsante "i miei gruppi" nell'elenco delle conversazioni
- progettato facebook group grid adapter (non in uso)

## 0.6.1
- rimosso _GROUP dall'id della conversazione
- bugfix back MessageListActivity
- bugfix bold
- rimossi chat settings deprecati

## 0.6
- new firebase data schema
- dialog conferma upload immagine

## 0.5.1
- bugfix logged user
- bugfix contentProvider real path
- added background to conversation
- designed network change broadcast receiver
- designed notwork change observer
- removed unused gladle dependencies
- added setOnMessageClickListener method

## 0.5
- messageDAO per disaccoppiare firebase dalla chat
- nodeDAO per disaccoppiare firebase dalla chat
- error view per quando di viene rimossi da un gruppo
- bugfix logged user
- bugfix contentProvider real path
- added background to conversation

## 0.4.2
- rimosse risorse "dimens" inutilizzate
- aggiornate risorse "dimens"
- rimossi SenderViewHolder/RecipientViewHolder senza immagini e relativi settings
- progress per le immagini inviate/ricevute via chat
- rimosso "floating_contextual_menu"
- bugfix layout notifica (dimensione testo delle notifiche foreground coerente con quello delle notifiche in background)
- bugifx lastOnline nel presenceManager
- è possibile allegare solo immagini locali (rimosso drive)
- previsto background chat
- gli utenti rimossi dal gruppo non possono più leggere i messaggi
- bugfix layout sender/recipient

## 0.4.1
- bugfix rendering immagine destinatario
- bugfix: a partire dal pannello di amministrazione di un gruppo un utente non può più avviare una conversazione con se stesso
- aggiunta progress dialog durante il logout
- bugfix crash owner per group == null
- bugfix username troncato
- aumentato il numero di linee da 1 a 2 per il "last_received_text" nell'elenco delle conversazioni
- migliorato layout elenco conversazioni
- bugfix messaggio "you have been added to group"
- bugfix backpress
- nascosto il pulsante "invia" se il messaggio di testo è composto solo da "\n" and "\r" (uno o più)

## 0.4
- rendering url immagini in MessageListActivity
- bugfix crash MessageListActivity per group == null
- bugfix crash GroupAdminPanelActivity per group == null
- bugfix crash PresenceHandler timestamp == null
- bugfix crash PublicProfileActivity timestamp == null
- bugfix crash CreateGroupActivity menuItem == null
- bugfix crash MessageUtils - updateNodesFromGroupMessage: addListenerForSingleValueEvent cambiato in addValueEventListener
- bugfix crash notifica push in background
- bugfix crash GroupAdminPanelActivity initCreatedByOn contact list
- bugfix crash Chat.getInstance().getXXX() == null, dove getXXX() è uno dei metodi statici per la navigazione di firebase
- bugfix layout diverso tra notifica foreground e notifica background in seguito a una regressione dovuta al cloud code (modificato per far ricevere le notifiche su ios)
- placeholder utente in formato vettoriale
- migliorato placeholder PublicProfileActivity
- bugfix placeholder UserProfileImage in ConversationListAdapter
- bugfix elenco contatti toolbar in MessageListActivity
- aggiornato firebaseUI da 0.6.2 a 2.0.1
- rimosso metodo setToolbar()

## 0.3
- rimozione utente da gruppo
- avvia conversazione privata con utente del gruppo
- bugfix conversationId per gruppo
- allega immagini
- aumentato minSdk minSdkVersion 16 a 19
- presence manager (stato, ultimo accesso, logout)
- bugfix placeholder immagine utente / gruppo in MessageListActivity
- alleggerita user profile activity (UserProfileActivity diventa UserProfileActivity)
- membri del gruppo nel sotto-titolo della toolbar in MessageListaActivity

## 0.2.4
- bugfix crash decodifica conversazione

## 0.2.3
- gestita eccezione certificati ssl
- aggiungi membro nel pannello di amministrazione del gruppo
- bottom sheet fragment nel pannello di amministrazione del gruppo
- i dati del nodo /users/tenant/tenant_userId/instanceId è stato spostato ora vengono
  scritti nel nodo /tenantUsers/tenant-userId/instanceId

## 0.2.2
- gestite notifiche push per i gruppi
- nuova icona gruppi

## 0.2.1
- bugfix lista membri in pannello amministrazione gruppo
- icone vettoriali ad alta definizione
- bugfix rimozione contatto (aggiungi membri)
- bugfix scroll lista conversazioni quando si aggiunge un nuovo gruppo

## 0.2.0
- aggiornamento buildtools e librerie android alla versione 26
- migliorata selezione contatti del gruppo
- aggiunto pulsante "crea gruppo" in elenco contatti
- migliorato pannello amministrazione gruppo

## 0.1.0
- supporto ai gruppi

## 0.0.3
- rifattorizzata classe "Chat"
- aggiunti listeners per click su contatto
- il tenant può essere impostato in fase di configurazione
- cambiato placeholder utente
- aggiunto log firebase
- rimosso parseSDK
- bugfix username in messagelistactivity da notifica
- gestione notifiche push con app in foreground
- bugfix tenant on refresh firebase token

## 0.0.2
- aggiornamento sdk firebase
- rifattorizato modulo conversations
- rifattorizato modulo messages
- rifattorizata classe utils
- aggiunto supporto click sui link
- rifattorizato modulo contacts
- aggiunti gruppi
- aggiunto click sul messaggio
- bugfix interfaccia utente
- bugfix crash per layout multipli
- cambiato avatar utente
- rimosse icone non usate

## 0.0.1
- creata lista dei contatti
- creata lista delle conversazioni
- creata lista dei messaggi
- creata funzione per inviare i messaggi
- aggiunta impostazione per abilitare il back nella toolbar
- progettatto l'utente
- migrazione da username a userid
- corretto layout mittente/destinatario
- aggiunta traduzione italiana
- corretto invio di messaggio vuoto(ora è più possibile)
- corretto nome app nella schermata dell'elenco delle conversazioni
- aggiunto layout quando non c'è nessuna conversazione
- bugfix manifest
- allineamento di Conversation.class e Message.class con i metadati di iOS
- creata interfaccia per gli utenti
- grassetto per i nuovi messaggi
- bugfix aggiornamento "is_new" per le conversazioni (adesso aggiorna solo questo metadato e non tutta la conversazione)
- cambiata icona per nuova conversazione
- corretta visualizzazione per layout senza conversazioni
- aggiunta funzione per inviare le notifiche
- bugfix il campo "username" di IUser e ChatUser è diventato FullName
- bugfix invio fullName nelle notifiche
- bugfix crash per le notifiche in arrivo
- nuove icone bubble mittente/destinatario
- esternalizzati colore bubble mittente/destinatario
- esternalizzati colore del testo bubble mittente/destinatario
- aggiunta configirazione per abilitare/disabilitare l'immagine del mittente nella conversazione
- bugfix testo notifiche
- migliorato layout edittext messaggio
- aggiunta doppia singola
- aggiunta doppia spunta
- aggiunta configirazione per abilitare/disabilitare l'immagine del destinatario nella conversazione e nella toolbar
- aggiunto pulsante "enter" invece che il pulsante "emoji" nella tastiera
- bugfix icona quadrata (random) del destinatario nella toolbar
- gestione click sull'immagine di profilo del destinatario
- aggiunta configurazione per scegliere l'activity inerente al profilo utente
- aggiunta configurazione per scegliere l'activity inerente alla lista di contatti
- aggiunto floating contextual menu sul click dei messaggi
- cambiato package
- aggiunta impostazione per avviare la chat direttamente sulla schermata della conversazione
- aggiunta configurazione per scegliere il parse server
- aggiunta configurazione per avviare la chat da un fragment
- aggiunta configurazione per avviare la chat da un fragment
- progettato il profilo utente
- aggiunta back arrow nel profilo utente
- aggiunto invio di dati custom nei messaggi