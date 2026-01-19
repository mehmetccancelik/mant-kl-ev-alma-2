package com.evanaliz.floating

/**
 * Durum Geçiş Olayları
 * 
 * State machine'i tetikleyen olaylar.
 */
sealed class FloatingUiEvent {
    
    /**
     * Kullanıcı widget'a tıkladı (sürükleme değil)
     */
    object Click : FloatingUiEvent()
    
    /**
     * Veri başarıyla alındı
     */
    object DataReceived : FloatingUiEvent()
    
    /**
     * Sonuç gösterimi kapatıldı
     */
    object ResultDismissed : FloatingUiEvent()
    
    /**
     * Hata oluştu veya timeout
     */
    object Error : FloatingUiEvent()
    
    /**
     * Manuel sıfırlama
     */
    object Reset : FloatingUiEvent()
}

/**
 * Durum Geçiş Sonucu
 * 
 * State machine'in bir geçiş sonrası durumu.
 */
data class StateTransitionResult(
    /**
     * Yeni durum
     */
    val newState: FloatingUiState,
    
    /**
     * Geçiş başarılı mı?
     */
    val transitionAllowed: Boolean,
    
    /**
     * Açıklama mesajı (debug için)
     */
    val message: String
)

/**
 * Yüzen Arayüz Durum Makinesi
 * 
 * Floating widget'ın durumlarını ve geçişlerini yöneten deterministik state machine.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ TASARIM PRENSİPLERİ                                                        ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. Deterministik - Aynı durum + olay = Aynı sonuç                          ║
 * ║ 2. Tek yönlü geçişler - Atlamak YASAK                                      ║
 * ║ 3. Hata toleranslı - Her hata IDLE'a döner                                 ║
 * ║ 4. Finans mantığından bağımsız                                             ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
class FloatingUiStateMachine {

    /**
     * Mevcut durum
     */
    var currentState: FloatingUiState = FloatingUiState.IDLE
        private set

    /**
     * Durum değişikliği dinleyicisi
     */
    var onStateChanged: ((oldState: FloatingUiState, newState: FloatingUiState) -> Unit)? = null

    /**
     * Olay işle ve geçiş yap.
     * 
     * @param event Tetikleyici olay
     * @return Geçiş sonucu
     */
    fun processEvent(event: FloatingUiEvent): StateTransitionResult {
        val oldState = currentState
        
        val result = when (currentState) {
            FloatingUiState.IDLE -> handleIdleState(event)
            FloatingUiState.PROCESSING -> handleProcessingState(event)
            FloatingUiState.SUCCESS -> handleSuccessState(event)
            FloatingUiState.RESULT_DISPLAY -> handleResultDisplayState(event)
        }
        
        if (result.transitionAllowed && result.newState != oldState) {
            currentState = result.newState
            onStateChanged?.invoke(oldState, result.newState)
        }
        
        return result
    }

    /**
     * IDLE durumunda olay işleme
     * 
     * İzin verilen: IDLE → PROCESSING (Click ile)
     */
    private fun handleIdleState(event: FloatingUiEvent): StateTransitionResult {
        return when (event) {
            is FloatingUiEvent.Click -> StateTransitionResult(
                newState = FloatingUiState.PROCESSING,
                transitionAllowed = true,
                message = "IDLE → PROCESSING: Veri çekme başlatıldı"
            )
            is FloatingUiEvent.Reset -> StateTransitionResult(
                newState = FloatingUiState.IDLE,
                transitionAllowed = true,
                message = "IDLE: Zaten IDLE durumunda"
            )
            else -> StateTransitionResult(
                newState = FloatingUiState.IDLE,
                transitionAllowed = false,
                message = "IDLE: Geçersiz olay - $event"
            )
        }
    }

    /**
     * PROCESSING durumunda olay işleme
     * 
     * İzin verilen: PROCESSING → SUCCESS (DataReceived ile)
     * Hata durumu: PROCESSING → IDLE (Error/Reset ile)
     */
    private fun handleProcessingState(event: FloatingUiEvent): StateTransitionResult {
        return when (event) {
            is FloatingUiEvent.DataReceived -> StateTransitionResult(
                newState = FloatingUiState.SUCCESS,
                transitionAllowed = true,
                message = "PROCESSING → SUCCESS: Veri başarıyla alındı"
            )
            is FloatingUiEvent.Error, is FloatingUiEvent.Reset -> StateTransitionResult(
                newState = FloatingUiState.IDLE,
                transitionAllowed = true,
                message = "PROCESSING → IDLE: Hata veya sıfırlama"
            )
            else -> StateTransitionResult(
                newState = FloatingUiState.PROCESSING,
                transitionAllowed = false,
                message = "PROCESSING: Geçersiz olay - $event"
            )
        }
    }

    /**
     * SUCCESS durumunda olay işleme
     * 
     * İzin verilen: SUCCESS → RESULT_DISPLAY (Click ile)
     */
    private fun handleSuccessState(event: FloatingUiEvent): StateTransitionResult {
        return when (event) {
            is FloatingUiEvent.Click -> StateTransitionResult(
                newState = FloatingUiState.RESULT_DISPLAY,
                transitionAllowed = true,
                message = "SUCCESS → RESULT_DISPLAY: Sonuç gösteriliyor"
            )
            is FloatingUiEvent.Reset -> StateTransitionResult(
                newState = FloatingUiState.IDLE,
                transitionAllowed = true,
                message = "SUCCESS → IDLE: Manuel sıfırlama"
            )
            else -> StateTransitionResult(
                newState = FloatingUiState.SUCCESS,
                transitionAllowed = false,
                message = "SUCCESS: Geçersiz olay - $event"
            )
        }
    }

    /**
     * RESULT_DISPLAY durumunda olay işleme
     * 
     * İzin verilen: RESULT_DISPLAY → IDLE (ResultDismissed ile)
     */
    private fun handleResultDisplayState(event: FloatingUiEvent): StateTransitionResult {
        return when (event) {
            is FloatingUiEvent.ResultDismissed, is FloatingUiEvent.Reset -> StateTransitionResult(
                newState = FloatingUiState.IDLE,
                transitionAllowed = true,
                message = "RESULT_DISPLAY → IDLE: Sonuç kapatıldı"
            )
            else -> StateTransitionResult(
                newState = FloatingUiState.RESULT_DISPLAY,
                transitionAllowed = false,
                message = "RESULT_DISPLAY: Geçersiz olay - $event"
            )
        }
    }

    /**
     * Durumu zorla IDLE'a sıfırla.
     * 
     * Hata kurtarma ve temizlik için kullanılır.
     */
    fun forceReset() {
        val oldState = currentState
        currentState = FloatingUiState.IDLE
        if (oldState != FloatingUiState.IDLE) {
            onStateChanged?.invoke(oldState, FloatingUiState.IDLE)
        }
    }
}
