package com.codekatana.passwordgen.brains

import android.util.Log
import com.codekatana.passwordgen.EzPassApplication
import java.io.*
import java.util.*
import kotlin.collections.HashMap

private const val cacheFileName = "words.bin"

class WordBank {

    private val cachedFile = File(EzPassApplication.context.filesDir, cacheFileName).also {
        if (!it.exists()) it.createNewFile()
    }
    var wordEntries = HashMap<String, WordEntry>()

    init {
        if (cachedFile.length() > 0) {
            Log.d(javaClass.simpleName, "Cachefile Size - ${cachedFile.length()}")
            ObjectInputStream(FileInputStream(cachedFile)).use { ois ->
                wordEntries = ois.readObject() as HashMap<String, WordEntry>
                Log.d(javaClass.simpleName, "Entries in file - ${wordEntries.size}")
            }
        }
    }

    fun getWords(count: Int): List<String> {
        val result = mutableListOf<String>()
        val rand = Random()
        val wordsInCache = wordEntries.keys.toList()
        for (i in 1..count) {
            val word = wordsInCache[rand.nextInt(wordEntries.size)]
            result.add(word)
            wordEntries[word]?.used = Date()
        }
        saveWords()
        return result
    }

    private fun saveWords() {
        Log.d(javaClass.simpleName, "Writing ${wordEntries.size} entries to file")
        ObjectOutputStream(FileOutputStream(cachedFile, false)).use {
            it.writeObject(wordEntries)
            it.flush()
        }
    }

    fun saveNewWords(words: Set<String>) {

        /*
        Purge old words in the order last used, last created if the count is more than 10,000
        Purge count = words.size - words subset wordEntries
         */

        if (wordEntries.size > 10_000) {
            // TODO purge old stuff
        }
        words.forEach {
            wordEntries[it] = WordEntry(it, Date(), null)
        }
        saveWords()
    }

}

data class WordEntry(val word: String, val created: Date, var used: Date?) : Serializable
