/*
 * Copyright (c)2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.unscramble.ui.test

import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import com.example.android.unscramble.data.getUnscrambledWord
import com.example.android.unscramble.ui.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {
    private val viewModel = GameViewModel()

    @Test
    fun gameViewModel_Initialization_FirstWordLoaded() {
        /**
         *  Warning: This way to retrieve the uiState works because MutableStateFlow is used. In the
         *  upcoming units you will learn about advanced usages of StateFlow that creates a stream
         *  of data and you need to react to handle the stream. For those scenarios you will write
         *  unit tests using different methods/approaches. This applies to all the usages of
         *  viewModel.uiState.value in this class.
         **/
        val gameUiState = viewModel.uiState.value
        val unScrambledWord = getUnscrambledWord(gameUiState.currentScrambledWord)

        // Nyatakan bahwa kata saat ini diacak.
        assertNotEquals(unScrambledWord, gameUiState.currentScrambledWord)
        // Nyatakan bahwa jumlah kata saat ini disetel ke 1.
        assertTrue(gameUiState.currentWordCount == 1)
        // Nyatakan bahwa awalnya skornya adalah 0.
        assertTrue(gameUiState.score == 0)
        // Tegaskan bahwa tebakan kata yang salah adalah salah.
        assertFalse(gameUiState.isGuessedWordWrong)
        // Menyatakan bahwa permainan belum berakhir.
        assertFalse(gameUiState.isGameOver)
    }

    @Test
    fun gameViewModel_IncorrectGuess_ErrorFlagSet() {
        // Diberi kata yang salah sebagai input
        val incorrectPlayerWord = "and"

        viewModel.updateUserGuess(incorrectPlayerWord)
        viewModel.checkUserGuess()

        val currentGameUiState = viewModel.uiState.value
        // Nyatakan bahwa skor tidak berubah
        assertEquals(0, currentGameUiState.score)
        // Nyatakan bahwa metode checkUserGuess() memperbarui isGuessedWordWrong dengan benar
        assertTrue(currentGameUiState.isGuessedWordWrong)
    }

    @Test
    fun gameViewModel_CorrectWordGuessed_ScoreUpdatedAndErrorFlagUnset() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)

        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()
        currentGameUiState = viewModel.uiState.value

        // Nyatakan bahwa metode checkUserGuess() memperbarui isGuessedWordWrong diperbarui dengan benar.
        assertFalse(currentGameUiState.isGuessedWordWrong)
        // Nyatakan bahwa skor diperbarui dengan benar.
        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
    }

    @Test
    fun gameViewModel_WordSkipped_ScoreUnchangedAndWordCountIncreased() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)

        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()
        currentGameUiState = viewModel.uiState.value
        val lastWordCount = currentGameUiState.currentWordCount
        // Nyatakan bahwa skor tetap tidak berubah setelah kata dilewati.
        viewModel.skipWord()
        currentGameUiState = viewModel.uiState.value

        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
        // Nyatakan bahwa jumlah kata bertambah 1 setelah kata dilewati.
        assertEquals(lastWordCount + 1, currentGameUiState.currentWordCount)
    }

    @Test
    fun gameViewModel_AllWordsGuessed_UiStateUpdatedCorrectly() {
        var expectedScore = 0
        var currentGameUiState = viewModel.uiState.value
        var correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)

        repeat(MAX_NO_OF_WORDS) {
            expectedScore += SCORE_INCREASE
            viewModel.updateUserGuess(correctPlayerWord)
            viewModel.checkUserGuess()
            currentGameUiState = viewModel.uiState.value
            correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
            // Nyatakan bahwa setelah setiap jawaban yang benar, skor diperbarui dengan benar.
            assertEquals(expectedScore, currentGameUiState.score)
        }
        // Nyatakan bahwa setelah semua pertanyaan dijawab, jumlah kata saat ini adalah yang terbaru.
        assertEquals(MAX_NO_OF_WORDS, currentGameUiState.currentWordCount)
        // Nyatakan bahwa setelah 10 pertanyaan terjawab, permainan berakhir.
        assertTrue(currentGameUiState.isGameOver)
    }

    companion object {
        private const val SCORE_AFTER_FIRST_CORRECT_ANSWER = SCORE_INCREASE
    }
}
