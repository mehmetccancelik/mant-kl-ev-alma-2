package com.evanaliz.accessibility

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Ekran Tarayıcı
 * 
 * AccessibilityNodeInfo ağacını recursive depth-first traversal ile tarar
 * ve aday metinleri toplar.
 * 
 * ╔════════════════════════════════════════════════════════════════════════════╗
 * ║ TARAMA STRATEJİSİ                                                          ║
 * ╠════════════════════════════════════════════════════════════════════════════╣
 * ║ 1. Recursive DFS traversal                                                 ║
 * ║ 2. Her node için text + contentDescription kontrol                         ║
 * ║ 3. Görünür olmayan node'ları atla                                          ║
 * ║ 4. Veri bulununca dur (early exit)                                         ║
 * ║ 5. Hafif ve interrupt edilebilir                                           ║
 * ╚════════════════════════════════════════════════════════════════════════════╝
 */
object ScreenTraverser {

    /**
     * Tarama yapılandırması
     */
    object Config {
        /**
         * Maksimum düğüm sayısı (performans koruması)
         */
        const val MAX_NODE_COUNT = 500

        /**
         * Maksimum derinlik
         */
        const val MAX_DEPTH = 30

        /**
         * Yeterli veri bulununca dur
         */
        const val STOP_ON_SUFFICIENT_DATA = true

        /**
         * Yeterli veri sayısı (early exit için)
         */
        const val SUFFICIENT_DATA_COUNT = 5
    }

    /**
     * Ekranı tara ve aday metinleri topla.
     * 
     * @param rootNode Kök düğüm (rootInActiveWindow)
     * @return Bulunan aday metinlerin listesi
     */
    fun traverse(rootNode: AccessibilityNodeInfo?): List<String> {
        if (rootNode == null) return emptyList()

        val results = mutableListOf<String>()
        var nodeCount = 0

        // Recursive traversal
        traverseNode(
            node = rootNode,
            results = results,
            nodeCount = { nodeCount++ },
            depth = 0
        )

        return results.distinct() // Tekrarları kaldır
    }

    /**
     * Tek bir düğümü ve alt düğümlerini tara (recursive).
     */
    private fun traverseNode(
        node: AccessibilityNodeInfo,
        results: MutableList<String>,
        nodeCount: () -> Int,
        depth: Int
    ): Boolean { // true = devam et, false = dur

        // Güvenlik kontrolleri
        if (nodeCount() > Config.MAX_NODE_COUNT) return false
        if (depth > Config.MAX_DEPTH) return false
        if (Config.STOP_ON_SUFFICIENT_DATA && results.size >= Config.SUFFICIENT_DATA_COUNT) {
            return false
        }

        // Görünürlük kontrolü
        if (!isNodeVisible(node)) {
            return true // Görünmez, ama devam et
        }

        // Bu düğümün metnini kontrol et
        extractTextFromNode(node)?.let { text ->
            if (TextDetectionRules.isCandidateText(text)) {
                val cleaned = TextDetectionRules.cleanText(text)
                if (cleaned.isNotBlank()) {
                    results.add(cleaned)
                }
            }
        }

        // Alt düğümleri tara
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue

            val shouldContinue = traverseNode(
                node = child,
                results = results,
                nodeCount = nodeCount,
                depth = depth + 1
            )

            // Düğümü geri dönüştür (memory leak önleme)
            try {
                child.recycle()
            } catch (e: Exception) {
                // Zaten recycle edilmiş olabilir
            }

            if (!shouldContinue) return false
        }

        return true
    }

    /**
     * Düğümden metin çıkar.
     * 
     * Öncelik: text > contentDescription
     */
    private fun extractTextFromNode(node: AccessibilityNodeInfo): String? {
        // Önce text'i dene
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let {
            return it
        }

        // Sonra contentDescription
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let {
            return it
        }

        return null
    }

    /**
     * Düğüm görünür mü?
     */
    private fun isNodeVisible(node: AccessibilityNodeInfo): Boolean {
        // isVisibleToUser kontrolü
        return node.isVisibleToUser
    }
}
